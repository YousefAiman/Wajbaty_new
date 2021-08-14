package com.developers.wajbaty.Models;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;

public class PartneredRestaurantModel extends Observable {

    public static final int TYPE_FAVORITE = 1, RESTAURANT_ID_CODE = 2;

    private String restaurantId;

    public PartneredRestaurantModel(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public PartneredRestaurantModel() {
    }


    public void addRestaurantToFirebase(boolean skipped, Intent intent, LinkedHashMap<String, Map<String, Object>> scheduleMap) {

        final String restaurantId = UUID.randomUUID().toString();

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        final DocumentReference restaurantDocumentRef =
                firestore.collection("PartneredRestaurant")
                        //.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        .document(restaurantId);

        final Map<String, Object> firestoreRestaurantMap = new HashMap<>();

        firestoreRestaurantMap.put("ID", restaurantId);


        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestoreRestaurantMap.put("ownerUid", currentUid);
        firestoreRestaurantMap.put("averageRating", 0f);

        final Map<String, Object> addressMap = (Map<String, Object>) intent.getSerializableExtra("addressMap");

        final LatLng latLng = (LatLng) addressMap.get("latLng");

        if (latLng != null) {
            final String hash = GeoFireUtils.getGeoHashForLocation(
                    new GeoLocation(latLng.latitude, latLng.longitude));

//            final Map<String, Object> locationMap = new HashMap<>();
            firestoreRestaurantMap.put("geohash", hash);
            firestoreRestaurantMap.put("lat", latLng.latitude);
            firestoreRestaurantMap.put("lng", latLng.longitude);
//            firestoreRestaurantMap.put("coordinates", locationMap);

        }

        firestoreRestaurantMap.put("countryCode", addressMap.get("countryCode"));
        firestoreRestaurantMap.put("address", addressMap.get("address"));
        firestoreRestaurantMap.put("fullAddress", addressMap.get("fullAddress"));


        final Bundle imagesBundle = intent.getBundleExtra("imagesBundle");

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(restaurantId);

        final List<UploadTask> uploadTasks = new ArrayList<>();

        final List<Task<Uri>> uriTasks = new ArrayList<>();


        if (imagesBundle.containsKey("mainImage")) {

            final UploadTask uploadTask =
                    storageReference.child("Restaurant_Main_Image").putFile(imagesBundle.getParcelable("mainImage"));

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    uriTasks.add(taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            firestoreRestaurantMap.put("mainImage", uri.toString());
                        }
                    }));

                }
            });

            uploadTasks.add(uploadTask);


        } else if (imagesBundle.containsKey("mainImageURL")) {
            firestoreRestaurantMap.put("mainImage", imagesBundle.getString("mainImageURL"));
        }

        final List<String> bannerImages = new ArrayList<>();

        if (imagesBundle.containsKey("bannerImages")) {

            final List<Uri> uriImages = imagesBundle.getParcelableArrayList("bannerImages");


            final StorageReference bannerRef = storageReference.child("Restaurant_Banner_Image");

            if (uriImages != null && !uriImages.isEmpty()) {

                for (int i = 0; i < uriImages.size(); i++) {

                    final UploadTask bannerUploadTask =
                            bannerRef.child("Banner_Image_" + i).putFile(uriImages.get(i));

                    bannerUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                uriTasks.add(task.getResult().getStorage().getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                Log.d("uploadRestaurant", "finished getting uri");
                                                bannerImages.add(uri.toString());

                                            }
                                        }));
                            }
                        }
                    });


                    uploadTasks.add(bannerUploadTask);
                }

            }
        }
        final List<String> albumImages = new ArrayList<>();

        if (imagesBundle.containsKey("albumImages")) {

            final List<Uri> uriImages = imagesBundle.getParcelableArrayList("albumImages");

            final StorageReference albumRef = storageReference.child("Restaurant_Album_Image");

            for (int i = 0; i < uriImages.size(); i++) {

                final UploadTask bannerUploadTask =
                        albumRef.child("Album_Image_" + i).putFile(uriImages.get(i));

                bannerUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            uriTasks.add(task.getResult().getStorage().getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            Log.d("uploadRestaurant", "finished getting uri");
                                            albumImages.add(uri.toString());

                                        }
                                    }));
                        }
                    }
                });

                uploadTasks.add(bannerUploadTask);
            }
        }

//        firestoreRestaurantMap.put("mainImage", );


        final Bundle infoBundle = intent.getBundleExtra("infoBundle");

        String name = infoBundle.getString("name");
        firestoreRestaurantMap.put("name", name);
        firestoreRestaurantMap.put("keyWords", Arrays.asList(name.toLowerCase().split(" ")));
        firestoreRestaurantMap.put("description", infoBundle.getString("description"));
        final String category = infoBundle.getString("category");
        firestoreRestaurantMap.put("category", category);

        final List<Task<Void>> tasksList = new ArrayList<>();

        final CollectionReference restaurantList =
                restaurantDocumentRef.collection("Lists");


        final HashMap<String, List<String>> serviceOptionsMap = new HashMap<>();
        serviceOptionsMap.put("ServiceOptions", infoBundle.getStringArrayList("selectedServiceOptions"));
        tasksList.add(restaurantList.document("ServiceOptions").set(serviceOptionsMap));

        if (infoBundle.containsKey("selectedAdditionalServices")) {

            final HashMap<String, List<String>> additionalServicesMap = new HashMap<>();

            additionalServicesMap.put("AdditionalServices", infoBundle.getStringArrayList("selectedAdditionalServices"));

            tasksList.add(restaurantList.document("AdditionalServices").set(additionalServicesMap));
        }

        if (infoBundle.containsKey("addedContactInfoMap")) {
            tasksList.add(restaurantList.document("ContactInformation")
                    .set(infoBundle.getSerializable("addedContactInfoMap")));
        }

        if (infoBundle.containsKey("addedSocialMediaSitesMap")) {
            tasksList.add(restaurantList.document("SocialMediaLinks")
                    .set(infoBundle.getSerializable("addedSocialMediaSitesMap")));
        }

        if (!skipped) {
            tasksList.add(restaurantList.document("Schedule").set(scheduleMap));
        }

        tasksList.add(
                firestore.collection("GeneralOptions").document("Categories")
                        .collection("Categories").document(category)
                        .update("count", FieldValue.increment(1)));


        Tasks.whenAllComplete(uploadTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                Tasks.whenAllComplete(uriTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {

                        if (!bannerImages.isEmpty()) {
                            firestoreRestaurantMap.put("bannerImages", bannerImages);
                        }

                        if (!albumImages.isEmpty()) {
                            firestoreRestaurantMap.put("albumImages", albumImages);
                        }

                        tasksList.add(restaurantDocumentRef.set(firestoreRestaurantMap, SetOptions.merge()));

                        tasksList.add(firestore.collection("Users").document(
                                currentUid).update("myRestaurantID", restaurantId));

                        Tasks.whenAllComplete(tasksList).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                                boolean allTasksSuccessful = true;

                                for (Task<?> completetTasks : tasksList) {

                                    if (!completetTasks.isSuccessful()) {

                                        allTasksSuccessful = false;

                                        Log.d("uploadRestaurant", "failed at " + tasksList.indexOf(completetTasks) + ": " + completetTasks.getException());

                                    } else {
                                        Log.d("uploadRestaurant", "is successfull: " + completetTasks.toString());

                                    }

                                }

                                HashMap<String, Object> restaurantResult = new HashMap<>();
                                restaurantResult.put("result", allTasksSuccessful);
                                restaurantResult.put("id", restaurantId);
                                setChanged();
                                notifyObservers(restaurantResult);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("uploadRestaurant", "failed: " + e.getMessage());

                                setChanged();
                                notifyObservers(e.getMessage());

                            }
                        });

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("uploadRestaurant", "failed: " + e.getMessage());
                setChanged();
                notifyObservers(e.getMessage());

            }
        });

    }


    public void favRestaurant(boolean alreadyFav) {

        if (restaurantId == null)
            return;


        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        FirebaseFirestore.getInstance().collection("Users").
                document(currentUid).collection("Favorites")
                .document("FavoriteRestaurants")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if (snapshot.exists()) {
                    addToFav(snapshot.getReference(), alreadyFav);
                } else {

                    final HashMap<String, Object> favMap = new HashMap<>();
                    favMap.put("FavoriteRestaurants", new ArrayList<>());

                    snapshot.getReference().set(favMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            addToFav(snapshot.getReference(), false);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("ttt", "failed to add snapshot: " + e.getMessage());
                        }
                    });
                }

            }
        });

    }


    private void addToFav(DocumentReference reference, boolean alreadyFav) {

        final HashMap<Integer, Object> resultMap = new HashMap<>();

        reference.update("FavoriteRestaurants", alreadyFav ? FieldValue.arrayRemove(restaurantId) :
                FieldValue.arrayUnion(restaurantId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        FirebaseFirestore.getInstance().collection("PartneredRestaurant")
                                .document(restaurantId).update("favCount", FieldValue.increment(alreadyFav ? -1 : 1))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        resultMap.put(TYPE_FAVORITE, true);
                                        resultMap.put(RESTAURANT_ID_CODE, restaurantId);
                                        setChanged();
                                        notifyObservers(resultMap);

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                resultMap.put(TYPE_FAVORITE, e.getMessage());
                setChanged();
                notifyObservers(resultMap);
            }
        });

    }
}
