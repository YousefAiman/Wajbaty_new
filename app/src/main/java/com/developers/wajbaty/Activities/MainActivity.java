package com.developers.wajbaty.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.developers.wajbaty.Models.DeliveryDriver;
import com.developers.wajbaty.Models.RestaurantAdmin;
import com.developers.wajbaty.Models.User;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantLocationActivity;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Services.MyFirebaseMessaging;
import com.developers.wajbaty.Utils.GeocoderUtil;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.developers.wajbaty.Utils.LocationRequester;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationRequester.LocationRequestAction,
        GeocoderUtil.GeocoderResultListener {

    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                    LocationRequester locationRequester = new LocationRequester(MainActivity.this,MainActivity.this);
                    locationRequester.getCurrentLocation();

                } else {
                    Toast.makeText(this,
                            "You need to grant location access permission in order " +
                                    "to show nearby restaurants!", Toast.LENGTH_SHORT).show();

                }
            });


    int lastClicked = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();

        Button normalUserBtn = findViewById(R.id.normalUserBtn);
        Button driverUserBtn = findViewById(R.id.driverUserBtn);
        Button restaurantAdminUserBtn = findViewById(R.id.restaurantAdminUserBtn);

        normalUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            lastClicked = 1;
//        if(currentUser!=null){

            requestLocation();
//            FirebaseFirestore.getInstance().collection("Users")
//                    .document(currentUser.getUid())
//                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot snapshot) {
//
//                    if(snapshot.exists()){
//
//                        if(snapshot.getLong("type") == User.TYPE_ADMIN){
//
//                            final RestaurantAdmin admin = snapshot.toObject(RestaurantAdmin.class);
//
//                            GlobalVariables.setCurrentRestaurantId(admin.getAdministratingRestaurants().get(0));
//
//                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
//
//                        }
//
//                    }
//
//                }
//            });

//        }else{
//
//            final ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
//            progressDialogFragment.setMessage("Creating new user");
//            progressDialogFragment.show(getSupportFragmentManager(),"progress");
//
//            auth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                @Override
//                public void onSuccess(AuthResult authResult) {
//
//                    final FirebaseUser user = authResult.getUser();
//                    final String userId = user.getUid();
//
//                    final HashMap<String,Object> userTestMap = new HashMap<>();
//
//                    userTestMap.put("ID",userId);
//                    userTestMap.put("type",1);
//
//                    FirebaseFirestore.getInstance().collection("Users")
//                            .document(userId).set(userTestMap).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            finish();
//                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
//
//                        }
//                    });
//                }
//            });
//
//        }

            }
        });

        driverUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lastClicked = 2;
                requestLocation();
            }
        });

        restaurantAdminUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lastClicked = 3;
                requestLocation();
            }
        });

    }

    private void requestLocation(){


        final String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, locationPermission)
                        == PackageManager.PERMISSION_GRANTED) {


            LocationRequester locationRequester = new LocationRequester(MainActivity.this,MainActivity.this);
            locationRequester.getCurrentLocation();


        }else{


            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        }

    }

    private void toRestaurantAcitvity(FirebaseUser currentUser,Map<String, Object> addressMap){

        FirebaseFirestore.getInstance().collection("PartneredRestaurant")
                .whereEqualTo("ownerUid",currentUser.getUid())
                .limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if(snapshots!=null && !snapshots.isEmpty()){

                    startMessagingService();

                    final String currentRestaurantId = snapshots.getDocuments().get(0).getId();

                    GlobalVariables.setCurrentRestaurantId(currentRestaurantId);
                    finish();
                    startActivity(new Intent(MainActivity.this, HomeActivity.class)
                    .putExtra("addressMap", (Serializable) addressMap));
                }

            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()){
                    finish();
                    startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));

                }
            }
        });

    }

    private void startMessagingService() {

        startService(new Intent(MainActivity.this, MyFirebaseMessaging.class));

//        getApplicationContext().getPackageManager().setComponentEnabledSetting(
//                new ComponentName(MainActivity.this.getApplicationContext(), MyFirebaseMessaging.class),
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);


    }

    @Override
    public void locationFetched(LatLng latLng) {

        Log.d("ttt","gotten latLng in main: "+latLng.latitude + "-"+latLng.longitude);
        GeocoderUtil.getLocationAddress(this,latLng,this);

    }

    @Override
    public void addressFetched(Map<String, Object> addressMap) {

        LatLng latLng = (LatLng) addressMap.get("latLng");
        Log.d("ttt","gotten latLng in main from geocoder: "+latLng.latitude + "-"+latLng.longitude);

        if(lastClicked == 1){

            FirebaseAuth.getInstance().signInWithEmailAndPassword("customer@gmail.com","123456").
                    addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            startMessagingService();

                            finish();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class)
                                    .putExtra("userType",User.TYPE_CUSTOMER)
                                    .putExtra("addressMap", (Serializable) addressMap));

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword("customer@gmail.com","123456")
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(String s) {

                                            User user = new User(authResult.getUser().getUid(),
                                                    "new customer",
                                                    authResult.getUser().getEmail(),
                                                    "",
                                                    "https://firebasestorage.googleapis.com/v0/b/wajbatytestproject.appspot.com/o/b60dc8bd-4756-4f9f-b7a2-04c92c97167d%2F17f7d916-6344-462f-a9dd-cb56e0a34091%2FmenuItemImage_0?alt=media&token=8c5480e3-447f-4b8c-bc2f-ae85a30e663a",
                                                    (String) addressMap.get("countryCode"),
                                                    s,
                                                    User.TYPE_CUSTOMER);


                                            FirebaseFirestore.getInstance().collection("Users")
                                                    .document(authResult.getUser().getUid())
                                                    .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    startMessagingService();

                                                    finish();
                                                    startActivity(new Intent(MainActivity.this, HomeActivity.class)
                                                            .putExtra("userType",User.TYPE_CUSTOMER)
                                                            .putExtra("addressMap", (Serializable) addressMap));

                                                }
                                            });


                                        }
                                    });

//              FirebaseFirestore.getInstance().collection("Users")
//                    .document(authResult.getUser().getUid())
//                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot snapshot) {
//
//                    if(snapshot.exists()){
//
//                        if(snapshot.getLong("type") == User.TYPE_ADMIN){
//
//                            final RestaurantAdmin admin = snapshot.toObject(RestaurantAdmin.class);
//
//                            GlobalVariables.setCurrentRestaurantId(admin.getAdministratingRestaurants().get(0));
//
//                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
//
//                        }else{
//
//                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
//
//                        }
//
//                    }
//
//                }
//            });

                                }
                            });
                }
            });



        }else if(lastClicked == 2){

            FirebaseAuth.getInstance().signInWithEmailAndPassword("newdriver@gmail.com","123456").
                    addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            startMessagingService();

                            finish();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class)
                                    .putExtra("userType",User.TYPE_DELIVERY)
                                    .putExtra("addressMap", (Serializable) addressMap));

//                  FirebaseFirestore.getInstance().collection("Users")
//                    .document(authResult.getUser().getUid())
//                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot snapshot) {
//
//                    if(snapshot.exists()){
//
//                            final RestaurantAdmin admin = snapshot.toObject(RestaurantAdmin.class);
//
//                            GlobalVariables.setCurrentRestaurantId(admin.getAdministratingRestaurants().get(0));
//
//                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
//
//                    }
//
//                }
//            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            FirebaseAuth.getInstance().createUserWithEmailAndPassword("newdriver@gmail.com","123456")
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {

                                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                @Override
                                                public void onSuccess(String s) {


                                                    DeliveryDriver deliveryDriver =
                                                            new DeliveryDriver(
                                                                    authResult.getUser().getUid(),
                                                                    "new driver",
                                                                    "driver@gmail.com",
                                                                    "",
                                                                    "https://firebasestorage.googleapis.com/v0/b/wajbatytestproject.appspot.com/o/5ea62558-cc45-4173-be5d-a0987505612d%2FRestaurant_Main_Image?alt=media&token=54418838-338e-4c5a-8f56-5b4d5778d038" ,
                                                                    (String) addressMap.get("countryCode"),
                                                                    s,
                                                                    User.TYPE_DELIVERY,
                                                                    0,
                                                                    null,
                                                                    DeliveryDriver.STATUS_AVAILABLE,
                                                                    new GeoPoint(latLng.latitude,latLng.longitude),
                                                                    GeoFireUtils.getGeoHashForLocation(
                                                                            new GeoLocation(latLng.latitude,latLng.longitude)
                                                                    )
                                                            );


                                                    FirebaseFirestore.getInstance().collection("Users")
                                                            .document(authResult.getUser().getUid())
                                                            .set(deliveryDriver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            startMessagingService();

                                                            finish();
                                                            startActivity(new Intent(MainActivity.this, HomeActivity.class)
                                                                    .putExtra("userType",User.TYPE_DELIVERY)
                                                                    .putExtra("addressMap", (Serializable) addressMap));

                                                        }
                                                    });


                                                }
                                            });



                                        }
                                    });
                        }
                    });
        }else if(lastClicked == 3){

            FirebaseAuth.getInstance().signInWithEmailAndPassword("restaurantAdmin@gmail.com","123456").
                    addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(authResult.getUser().getUid())
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    if(documentSnapshot.exists()){

                                        startMessagingService();

                                        GlobalVariables.setCurrentRestaurantId(
                                                ((List<String>)documentSnapshot.get("administratingRestaurants")).get(0));

                                        finish();

                                        startActivity(new Intent(MainActivity.this, HomeActivity.class)
                                                .putExtra("userType",User.TYPE_ADMIN)
                                                .putExtra("addressMap", (Serializable) addressMap));

                                    }

                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            FirebaseAuth.getInstance().createUserWithEmailAndPassword("restaurantAdmin@gmail.com","123456")
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {

                                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                                @Override
                                                public void onSuccess(String s) {

                                                    List<String> adminRestaurants = new ArrayList<>();

                                                    RestaurantAdmin restaurantAdmin =
                                                            new RestaurantAdmin(authResult.getUser().getUid(),
                                                                    "restaurant admin",
                                                                    authResult.getUser().getEmail(),
                                                                    "",
                                                                    "https://firebasestorage.googleapis.com/v0/b/wajbatytestproject.appspot.com/o/d9e4179d-56d9-4408-a7f7-5389158c3517%2FRestaurant_Main_Image?alt=media&token=c102983d-79d8-4657-9f40-ec846e127e92",
                                                                    (String) addressMap.get("countryCode"),
                                                                    s,
                                                                    User.TYPE_ADMIN,
                                                                    adminRestaurants);


                                                    FirebaseFirestore.getInstance().collection("Users")
                                                            .document(authResult.getUser().getUid())
                                                            .set(restaurantAdmin).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            finish();
                                                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));

                                                        }
                                                    });


                                                }
                                            });

                                        }
                                    });
                        }
                    });
        }

    }

    @Override
    public void addressFetchFailed(String errorMessage) {

    }
}