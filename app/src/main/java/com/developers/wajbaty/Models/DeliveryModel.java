package com.developers.wajbaty.Models;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.developers.wajbaty.BuildConfig;
import com.developers.wajbaty.Utils.CloudMessagingNotificationsSender;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

public class DeliveryModel extends Observable {

    public static final int DELIVERY_REQUEST_SUCCESS = 1, DELIVERY_REQUEST_FAILED = 2,
            DELIVERY_DRIVER_NOT_FOUND = 3,
            DELIVERY_DRIVERS_NOTIFIED = 4,
            DELIVERY_DRIVER_ACCEPTED_DELIVERY = 5,
            DRIVER_DELIVERY_REQUEST = 6, DRIVER_DELIVERY_REQUEST_ACCEPTED = 7, DRIVER_DELIVERY_REQUEST_DENIED = 8,
            DELIVERY_STARTED = 9;

    private final Delivery delivery;
    private final DocumentReference deliveryRef;
    private final FirebaseFirestore firestore;
    private final Context context;
    private final String currentUID;
    private ListenerRegistration acceptanceSnapshotListener;
    public DeliveryModel(Delivery delivery, Context context) {
        this.delivery = delivery;
        this.context = context;

        firestore = FirebaseFirestore.getInstance();
        deliveryRef = firestore.collection("Deliveries")
                .document(delivery.getID());
        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public ListenerRegistration getAcceptanceSnapshotListener() {
        return acceptanceSnapshotListener;
    }

    public void requestDelivery(HashMap<String, Object> locationMap, ArrayList<CartItem> cartItems) {

        deliveryRef.set(delivery).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                firestore.collection("Users").document(currentUID)
                        .update("currentDelivery.currentDeliveryID", delivery.getID(),
                                "currentDelivery.status", Delivery.STATUS_PENDING);


                Log.d("ttt", "delivery added");

                final CollectionReference deliveryCartRef = deliveryRef.collection("CartItems");
                final CollectionReference restaurantRef = FirebaseFirestore.getInstance()
                        .collection("PartneredRestaurant");

                final List<Task<?>> tasks = new ArrayList<>();

                final HashMap<String, List<CartItem>> restaurantCartItemsMap = new HashMap<>();

                for (CartItem cartItem : cartItems) {

                    tasks.add(deliveryCartRef.document(cartItem.getItemId()).set(cartItem));

                    final String restaurantID = cartItem.getRestaurantID();

                    if (restaurantID == null)
                        return;

                    if (restaurantCartItemsMap.containsKey(restaurantID)) {
                        restaurantCartItemsMap.get(restaurantID).add(cartItem);
                    } else {
                        final List<CartItem> restaurantCartItems = new ArrayList<>();
                        restaurantCartItems.add(cartItem);
                        restaurantCartItemsMap.put(restaurantID, restaurantCartItems);
                    }
                }

                for (String restaurantID : restaurantCartItemsMap.keySet()) {

                    final List<CartItem> restaurantCartItems = restaurantCartItemsMap.get(restaurantID);

                    if (restaurantCartItems == null)
                        return;

                    float totalCost = 0;
                    for (CartItem cartItem : restaurantCartItems) {
                        totalCost += cartItem.getPrice();
                    }

                    final String orderID = UUID.randomUUID().toString();

                    final RestaurantOrder restaurantOrder = new RestaurantOrder(
                            orderID, delivery.getOrderTimeInMillis(), delivery.getDriverID(),
                            RestaurantOrder.TYPE_PENDING, totalCost, delivery.getCurrency(),
                            restaurantCartItems.size());

                    Log.d("ttt", "restaurantID: " + restaurantID);
                    Log.d("ttt", "orderID: " + orderID);
                    if (restaurantID != null) {

                        final DocumentReference restaurantOrderRef =
                                restaurantRef.document(restaurantID)
                                        .collection("MealsOrders").document(orderID);
                        tasks.add(restaurantOrderRef.set(restaurantOrder));
                        final CollectionReference orderCartRef =
                                restaurantOrderRef.collection("Cart");

                        for (CartItem cartItem : restaurantCartItems) {
                            tasks.add(orderCartRef.document(cartItem.getItemId()).set(cartItem));
                        }

                    }

                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {

                        Log.d("ttt", "");
                        for (Task<?> task1 : tasks) {
                            if (!task1.isSuccessful()) {

                                deleteDelivery(DELIVERY_REQUEST_FAILED, "uploading all cart items to delivery failed");

//                                deliveryRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        notifyError(DELIVERY_REQUEST_FAILED,"uploading all cart items to delivery failed");
//                                    }
//                                });
                                return;
                            }
                        }

                        notifyDeliveryDrivers(locationMap);

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                notifyError(DELIVERY_REQUEST_FAILED, e.getMessage());

            }
        });

    }

    public void deleteDelivery(int errorCode, String errorMessage) {


        deliveryRef.collection("CartItems")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if (snapshots != null && !snapshots.isEmpty()) {
//                                    List<Task<Void>> deletedTasks = new ArrayList<>();
//
//                                    deletedTasks
                    for (DocumentSnapshot snapshot : snapshots) {
                        snapshot.getReference().delete();
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });

        deliveryRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                firestore.collection("Users").document(currentUID)
                        .update("currentDelivery", FieldValue.delete());

            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (errorCode != 0 && errorMessage != null) {
                    notifyError(errorCode, errorMessage);
                }
            }
        });

    }

    private void notifyDeliveryDrivers(HashMap<String, Object> locationMap) {

        final GeoLocation center = new GeoLocation(
                delivery.getLat(),
                delivery.getLng());

        final List<GeoQueryBounds> geoQueryBounds =
                GeoFireUtils.getGeoHashQueryBounds(center, 10 * 1000);

        final Query driversQuery =
                firestore.collection("Users")
                        .whereEqualTo("countryCode", locationMap.get("countryCode"))
                        .whereEqualTo("status", DeliveryDriver.STATUS_AVAILABLE)
                        .orderBy("geohash");

        List<Task<QuerySnapshot>> driverTasks = new ArrayList<>();

        for (GeoQueryBounds b : geoQueryBounds) {
            Query query = driversQuery.startAt(b.startHash).endAt(b.endHash);
            driverTasks.add(query.get());
        }

        firestore.collection("Users").document(delivery.getRequesterID())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Log.d("ttt", "gotten requester info");

                CloudMessagingNotificationsSender.Data data =
                        new CloudMessagingNotificationsSender.Data(
                                delivery.getRequesterID(),
                                "Delivery Request",
                                documentSnapshot.getString("name") + " ordered delivery",
                                documentSnapshot.getString("imageURL"),
                                delivery.getID(),
                                CloudMessagingNotificationsSender.Data.TYPE_DELIVERY_REQUEST);


                Tasks.whenAllComplete(driverTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {

                        Log.d("ttt", "all driver tasks complete");

                        boolean noResult = true;

                        for (Task<QuerySnapshot> driverTask : driverTasks) {

                            Log.d("ttt", "driverTask.isSuccessful(): " + driverTask.isSuccessful());

                            if (driverTask.isSuccessful() && driverTask.getResult() != null && !driverTask.getResult().isEmpty()) {

                                Log.d("ttt", "drivers found in task");

                                for (DocumentSnapshot snapshot : driverTask.getResult()) {

                                    Log.d("ttt", "driver found: " + snapshot.getId());

                                    CloudMessagingNotificationsSender.sendNotification(snapshot.getId(), data);
                                }

                                noResult = false;
                            } else if (!driverTask.isSuccessful()) {

                                if (driverTask.getException() != null &&
                                        driverTask.getException().getMessage() != null) {

                                    Log.d("ttt", "driver error: " + driverTask.getException().getMessage());

                                }

                            }
                        }

                        listenForDriverDeliveryAcceptance();

                        setChanged();
                        notifyObservers(DELIVERY_DRIVERS_NOTIFIED);


//                        if(noResult){
//
//                            deleteDelivery(DELIVERY_DRIVER_NOT_FOUND,"No drivers found in your range");
//
//                        }else{
//                            notifyObservers(DELIVERY_DRIVERS_NOTIFIED);
//                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ttt", "failed to get offers: " + e.getMessage());

                        listenForDriverDeliveryAcceptance();

                        setChanged();
                        notifyError(DELIVERY_DRIVER_NOT_FOUND, e.getMessage());

                    }
                });

            }
        });
//        Tasks.whenAllComplete(driverTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
//            @Override
//            public void onComplete(@NonNull Task<List<Task<?>>> task) {
//
//                for(Task<QuerySnapshot> driverTask: driverTasks){
//
//                    if(driverTask.isSuccessful() && driverTask.getResult()!=null
//                    && !driverTask.getResult().isEmpty()){
//
//                        for(DocumentSnapshot snapshot:driverTask.getResult()){
//
//                            firestore.collection("Users").document(snapshot.getId())
//                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                                    CloudMessagingNotificationsSender.Data data =
//                                            new CloudMessagingNotificationsSender.Data(
//                                                    delivery.getRequesterID(),
//                                                    "Delivery Request",
//                                                    documentSnapshot.getString("name") + " ordered delivery",
//                                                    documentSnapshot.getString("imageUrl"),
//                                                    delivery.getID(),
//                                                    CloudMessagingNotificationsSender.Data.TYPE_DELIVERY_REQUEST);
//
//
//                                    Tasks.whenAllComplete(driverTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<List<Task<?>>> task) {
//
//                                            boolean allFailed = true;
//
//                                            for (Task<QuerySnapshot> driverTask : driverTasks) {
//                                                if(driverTask.isSuccessful() && driverTask.getResult()!=null && !driverTask.getResult().isEmpty()){
//
//                                                    for(DocumentSnapshot snapshot:driverTask.getResult()){
//                                                        CloudMessagingNotificationsSender.sendNotification(snapshot.getId(), data);
//                                                    }
//
//                                                    allFailed = false;
//                                                }
//                                            }
//
//                                            if(allFailed){
//                                                notifyError(DELIVERY_DRIVER_NOT_FOUND,"No drivers found in your range");
//                                            }else{
//                                                setChanged();
//                                                notifyObservers(DELIVERY_DRIVERS_NOTIFIED);
//
//                                            }
//
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Log.d("ttt","failed to get offers: "+e.getMessage());
//                                            notifyError(DELIVERY_DRIVER_NOT_FOUND,e.getMessage());
//
//                                        }
//                                    });
//
//                                }
//                            });
//                        }
//
//                    }
//                }
//
//
//            }
//        });
//
//        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    }


    public void listenForDriverDeliveryAcceptance() {

        acceptanceSnapshotListener = deliveryRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            String lastDriverID = null;

            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d("ttt", "delivery changed");
                if (value != null) {

                    Log.d("ttt", "(value!=null");
                    if (value.contains("proposingDriverMap")) {
                        Log.d("ttt", "(!foundDriverRequest && value.contains(\"proposingDriverMap\")");
                        HashMap<String, Object> proposingMap = (HashMap<String, Object>) value.get("proposingDriverMap");

                        if (proposingMap == null) {
                            return;
                        }

                        if (proposingMap.containsKey("driverID")) {
                            Log.d("ttt", "roposingMap.containsKey(\"driverID\")");

                            final String driverID = (String) proposingMap.get("driverID");

                            if (driverID == null)
                                return;

                            if (lastDriverID != null && lastDriverID.equals(driverID))
                                return;

//                           final HashMap<Integer,Object> driverDeliveryRequest = new HashMap<>();
//                           driverDeliveryRequest.put(DRIVER_DELIVERY_REQUEST, driverID);

                            final Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".driverRequest");
                            intent.putExtra("driverID", driverID);
                            intent.putExtra("delivery", delivery);
                            context.sendBroadcast(intent);
                            Log.d("ttt", "context.sendBroadcast(intent)");
//                           setChanged();
//                           notifyObservers(driverDeliveryRequest);

                            lastDriverID = driverID;
                        } else {
                            Log.d("ttt", "else");
                        }

                    } else {
                        Log.d("ttt", "foundDriverRequest = false");
                    }

                    if (value.contains("status")) {

                        int status = value.getLong("status").intValue();

                        if (status == Delivery.STATUS_ACCEPTED) {

                            delivery.setStatus(Delivery.STATUS_ACCEPTED);
                        } else if (status == Delivery.STATUS_USER_DENIED_APPROVAL) {
                            lastDriverID = null;
                        }

//                       Delivery.InProgressDelivery inProgressDelivery =
//                               (Delivery.InProgressDelivery) delivery;
//
//                       inProgressDelivery.setDriverID(value.getString("driverID"));
//
//                        setChanged();
//                        notifyObservers(inProgressDelivery);

                    }

                } else {
                    Log.d("ttt", "value == null");
                }

            }
        });

    }

    private void notifyError(int key, String error) {
        final HashMap<Integer, Object> resultMap = new HashMap<>();
        resultMap.put(key, error);
        setChanged();
        notifyObservers(resultMap);
    }


    public void acceptDriverRequest() {

        deliveryRef.update("proposingDriverMap.status", true,
                "proposingDriverMap.hasDecided", true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        firestore.collection("Users").document(currentUID)
                                .update("currentDelivery.status", Delivery.STATUS_ACCEPTED)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        setChanged();
                                        notifyObservers(DELIVERY_STARTED);

//                                        setChanged();
//                                        notifyObservers(DRIVER_DELIVERY_REQUEST_ACCEPTED);

                                    }
                                });

                    }
                });

//            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        firestore.collection("Users").document(currentUid)
//                .update("currentDeliveryId",delivery.getID())
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                        firestore.collection("Deliveries").document(delivery.getID())
//                                .update("status",Delivery.STATUS_ACCEPTED,
//                                        "driverID", currentUid)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//
//                                        setChanged();
//                                        notifyObservers(DRIVER_DELIVERY_REQUEST_ACCEPTED);
//
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//
//                                hideProgressFragment();
//
//                                deliveryInfoStartDeliveryBtn.setClickable(true);
//
//                                setChanged();
//                                notifyObservers(DRIVER_DELIVERY_REQUEST_ACCEPTED);
//
//
//                                firestore.collection("DeliveryDrivers").document(currentUid)
//                                        .update("currentDeliveryId",null);
//
//                            }
//                        });
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//                hideProgressFragment();
//                deliveryInfoStartDeliveryBtn.setClickable(true);
//
//            }
//        });

    }

    public void refuseDriverRequest() {

        deliveryRef.update("proposingDriverMap.status", false)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

//                deliveryRef.update("proposingDriverMap", FieldValue.delete())
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                        deleteDelivery(DRIVER_DELIVERY_REQUEST_DENIED,"");
//                    }
//                });

                    }
                });

    }

}
