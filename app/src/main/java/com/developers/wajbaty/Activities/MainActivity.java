package com.developers.wajbaty.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.developers.wajbaty.Models.DeliveryDriver;
import com.developers.wajbaty.Models.User;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantLocationActivity;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Services.MyFirebaseMessaging;
import com.developers.wajbaty.Utils.GeocoderUtil;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.developers.wajbaty.Utils.LocationRequester;
import com.developers.wajbaty.Utils.WifiUtil;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.Serializable;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationRequester.LocationRequestAction,
        GeocoderUtil.GeocoderResultListener
//        , View.OnClickListener
{

    private static final int TO_SLIDER_ACTIVITY = 1, TO_CONNECTION_ACTIVITY = 2,
            TO_HOME_ACTIVITY = 3, TO_WELCOME_ACTIVITY = 4, TO_MESSAGING_ACTIVITY = 5;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                    LocationRequester locationRequester = new LocationRequester(MainActivity.this, MainActivity.this);
                    locationRequester.getCurrentLocation();

                } else {
                    Toast.makeText(this,
                            "You need to grant location access permission in order " +
                                    "to show nearby restaurants!", Toast.LENGTH_SHORT).show();
                }
            });
//    int lastClicked;
    private int targetActivity;
    private int userType;
    private FirebaseUser currentUser;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("startupTime", "start time: " + System.currentTimeMillis());
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

//        findViewById(R.id.normalUserBtn).setOnClickListener(this);
//        findViewById(R.id.driverUserBtn).setOnClickListener(this);
//        findViewById(R.id.restaurantAdminUserBtn).setOnClickListener(this);

        directUserToAppropriateActivity();
    }

    private void directUserToAppropriateActivity() {

        if (WifiUtil.isConnectedToInternet(this)) {

            final SharedPreferences sharedPreferences =
                    getSharedPreferences("Wajbaty", Context.MODE_PRIVATE);

            if (!sharedPreferences.contains("notFirstTime")) {

                targetActivity = TO_SLIDER_ACTIVITY;
                requestLocation();

            } else if (currentUser != null) {

                if (getIntent().hasExtra("messagingBundle")) {
                    targetActivity = TO_MESSAGING_ACTIVITY;
                } else {
                    targetActivity = TO_HOME_ACTIVITY;
                }

                requestLocation();
            } else {

                targetActivity = TO_WELCOME_ACTIVITY;
                requestLocation();

            }
        } else {

            startConnectionActivity();

        }

    }

    @Override
    public void locationFetched(LatLng latLng) {
        Log.d("ttt", "gotten latLng in main: " + latLng.latitude + "-" + latLng.longitude);
        GeocoderUtil.getLocationAddress(this, latLng, this);
    }

    @Override
    public void addressFetched(Map<String, Object> addressMap) {

//        LatLng latLng = (LatLng) addressMap.get("latLng");
//        Log.d("ttt", "gotten latLng in main from geocoder: " + latLng.latitude + "-" + latLng.longitude);

        switch (targetActivity) {

            case TO_SLIDER_ACTIVITY:

                startActivity(new Intent(this, SliderActivity.class)
                        .putExtra("addressMap", (Serializable) addressMap));
                finish();
                break;

            case TO_HOME_ACTIVITY:
            case TO_MESSAGING_ACTIVITY:

                startTargetActivity(addressMap);

                break;

            case TO_WELCOME_ACTIVITY:

                startActivity(new Intent(this, WelcomeActivity.class)
                        .putExtra("addressMap", (Serializable) addressMap));
                finish();

                break;
        }

//        if(lastClicked == 1){
//
//            FirebaseAuth.getInstance().signInWithEmailAndPassword("customer@gmail.com","123456").
//                    addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                        @Override
//                        public void onSuccess(AuthResult authResult) {
//
//                            startMessagingService();
//
//                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//                                @Override
//                                public void onSuccess(String s) {
//
//                                    FirebaseFirestore.getInstance().collection("Users")
//                                            .document(authResult.getUser().getUid())
//                                            .update("cloudMessagingToken",s).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            startActivity(new Intent(MainActivity.this, HomeActivity.class)
//                                                    .putExtra("userType",User.TYPE_CUSTOMER)
//                                                    .putExtra("addressMap", (Serializable) addressMap));
//
//                                            finish();
//                                        }
//                                    });
//
//                                }
//                            });
//
//;
//
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//
//                    FirebaseAuth.getInstance().createUserWithEmailAndPassword("customer@gmail.com","123456")
//                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                                @Override
//                                public void onSuccess(AuthResult authResult) {
//
//                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//                                        @Override
//                                        public void onSuccess(String s) {
//
//                                            User user = new User(authResult.getUser().getUid(),
//                                                    "new customer",
//                                                    authResult.getUser().getEmail(),
//                                                    "",
//                                                    "https://firebasestorage.googleapis.com/v0/b/wajbatytestproject.appspot.com/o/b60dc8bd-4756-4f9f-b7a2-04c92c97167d%2F17f7d916-6344-462f-a9dd-cb56e0a34091%2FmenuItemImage_0?alt=media&token=8c5480e3-447f-4b8c-bc2f-ae85a30e663a",
//                                                    (String) addressMap.get("countryCode"),
//                                                    s,
//                                                    User.TYPE_CUSTOMER);
//
//
//                                            FirebaseFirestore.getInstance().collection("Users")
//                                                    .document(authResult.getUser().getUid())
//                                                    .set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//
//                                                    startMessagingService();
//
//                                                                    startActivity(new Intent(MainActivity.this, HomeActivity.class)
//                                                                            .putExtra("userType",User.TYPE_CUSTOMER)
//                                                                            .putExtra("addressMap", (Serializable) addressMap));
//
//                                                                    finish();
//
//                                                }
//                                            });
//
//
//                                        }
//                                    });
//
////              FirebaseFirestore.getInstance().collection("Users")
////                    .document(authResult.getUser().getUid())
////                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
////                @Override
////                public void onSuccess(DocumentSnapshot snapshot) {
////
////                    if(snapshot.exists()){
////
////                        if(snapshot.getLong("type") == User.TYPE_ADMIN){
////
////                            final RestaurantAdmin admin = snapshot.toObject(RestaurantAdmin.class);
////
////                            GlobalVariables.setCurrentRestaurantId(admin.getAdministratingRestaurants().get(0));
////
////                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
////
////                        }else{
////
////                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
////
////                        }
////
////                    }
////
////                }
////            });
//
//                                }
//                            });
//                }
//            });
//
//
//
//        }else if(lastClicked == 2){
//
//            FirebaseAuth.getInstance().signInWithEmailAndPassword("AhmedAli@gmail.com","123456").
//                    addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                        @Override
//                        public void onSuccess(AuthResult authResult) {
//
//                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//                                @Override
//                                public void onSuccess(String s) {
//
//                                    FirebaseFirestore.getInstance().collection("Users")
//                                            .document(authResult.getUser().getUid())
//                                            .update("cloudMessagingToken",s).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            startActivity(new Intent(MainActivity.this, HomeActivity.class)
//                                                    .putExtra("userType",User.TYPE_DELIVERY)
//                                                    .putExtra("addressMap", (Serializable) addressMap));
//
//                                            finish();
//                                        }
//                                    });
//
//                                }
//                            });
//
////                  FirebaseFirestore.getInstance().collection("Users")
////                    .document(authResult.getUser().getUid())
////                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
////                @Override
////                public void onSuccess(DocumentSnapshot snapshot) {
////
////                    if(snapshot.exists()){
////
////                            final RestaurantAdmin admin = snapshot.toObject(RestaurantAdmin.class);
////
////                            GlobalVariables.setCurrentRestaurantId(admin.getAdministratingRestaurants().get(0));
////
////                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
////
////                    }
////
////                }
////            });
//
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//
//                            FirebaseAuth.getInstance().createUserWithEmailAndPassword("AhmedAli@gmail.com","123456")
//                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                                        @Override
//                                        public void onSuccess(AuthResult authResult) {
//
//                                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//                                                @Override
//                                                public void onSuccess(String s) {
//
//
//                                                    DeliveryDriver deliveryDriver =
//                                                            new DeliveryDriver(
//                                                                    authResult.getUser().getUid(),
//                                                                    "Ahmed Ali",
//                                                                    "AhmedAli@gmail.com",
//                                                                    "",
//                                                                    "https://firebasestorage.googleapis.com/v0/b/wajbatytestproject.appspot.com/o/images%2Fways-to-successfully-manage-a-fleet-of-restaurant-food-delivery-drivers-2-1024x683.jpg?alt=media&token=1673fa36-cc5e-404b-8499-dbb3edd6787e" ,
//                                                                    (String) addressMap.get("countryCode"),
//                                                                    s,
//                                                                    User.TYPE_DELIVERY,
//                                                                    0,
//                                                                    null,
//                                                                    DeliveryDriver.STATUS_AVAILABLE,
//                                                                    new GeoPoint(latLng.latitude,latLng.longitude),
//                                                                    GeoFireUtils.getGeoHashForLocation(
//                                                                            new GeoLocation(latLng.latitude,latLng.longitude)
//                                                                    )
//                                                            );
//
//
//                                                    FirebaseFirestore.getInstance().collection("Users")
//                                                            .document(authResult.getUser().getUid())
//                                                            .set(deliveryDriver).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//
//                                                            startMessagingService();
//                                                            startActivity(new Intent(MainActivity.this, HomeActivity.class)
//                                                                    .putExtra("userType",User.TYPE_DELIVERY)
//                                                                    .putExtra("addressMap", (Serializable) addressMap));
//
//                                                            finish();
//
//                                                        }
//                                                    });
//
//
//                                                }
//                                            });
//
//
//
//                                        }
//                                    });
//                        }
//                    });
//        }else if(lastClicked == 3){
//
//            String email = "testRestaurant@gmail.com";
//
//            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,"123456").
//                    addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                        @Override
//                        public void onSuccess(AuthResult authResult) {
//
//                            FirebaseFirestore.getInstance().collection("Users")
//                                    .document(authResult.getUser().getUid())
//                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                                    if(documentSnapshot.exists()){
//
//                                        startMessagingService();
//
//                                        GlobalVariables.setCurrentRestaurantId(documentSnapshot.getString("myRestaurantID"));
//                                        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//                                            @Override
//                                            public void onSuccess(String s) {
//
//                                                FirebaseFirestore.getInstance().collection("Users")
//                                                        .document(authResult.getUser().getUid())
//                                                        .update("cloudMessagingToken",s).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        startActivity(new Intent(MainActivity.this, HomeActivity.class)
//                                                                .putExtra("userType",User.TYPE_ADMIN)
//                                                                .putExtra("addressMap", (Serializable) addressMap));
//
//                                                        finish();
//                                                    }
//                                                });
//
//                                            }
//                                        });
//
//                                    }
//
//                                }
//                            });
//
//
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//
//                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,"123456")
//                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                                        @Override
//                                        public void onSuccess(AuthResult authResult) {
//
//                                            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//                                                @Override
//                                                public void onSuccess(String s) {
//
//                                                    User userInfo =
//                                                            new User(authResult.getUser().getUid(),
//                                                                    "restaurant admin",
//                                                                    authResult.getUser().getEmail(),
//                                                                    "",
//                                                                    null,
//                                                                    (String) addressMap.get("countryCode"),
//                                                                    s,
//                                                                    User.TYPE_ADMIN);
//
//
//                                                    FirebaseFirestore.getInstance().collection("Users")
//                                                            .document(authResult.getUser().getUid())
//                                                            .set(userInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                        @Override
//                                                        public void onSuccess(Void aVoid) {
//
//                                                            finish();
//                                                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
//
//                                                        }
//                                                    });
//
//
//                                                }
//                                            });
//
//                                        }
//                                    });
//                        }
//                    });
//        }

    }

    @Override
    public void addressFetchFailed(String errorMessage) {

    }

    private void requestLocation() {

        final String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, locationPermission)
                    == PackageManager.PERMISSION_GRANTED) {

                LocationRequester locationRequester = new LocationRequester(MainActivity.this,
                        MainActivity.this);
                locationRequester.getCurrentLocation();
            } else {

                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {

            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

        }

    }

//    private void toRestaurantAcitvity(FirebaseUser currentUser,Map<String, Object> addressMap){
//
//        FirebaseFirestore.getInstance().collection("PartneredRestaurant")
//                .whereEqualTo("ownerUid",currentUser.getUid())
//                .limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot snapshots) {
//
//                if(snapshots!=null && !snapshots.isEmpty()){
//
//                    startMessagingService();
//
//                    final String currentRestaurantId = snapshots.getDocuments().get(0).getId();
//
//                    GlobalVariables.setCurrentRestaurantId(currentRestaurantId);
//                    startActivity(new Intent(MainActivity.this, HomeActivity.class)
//                            .putExtra("userType",userType)
//                            .putExtra("addressMap", (Serializable) addressMap));
//                    finish();
//                }
//
//            }
//        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()){
//                    finish();
//                    startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
//
//                }
//            }
//        });
//
//    }

    private void startMessagingService() {
        startService(new Intent(MainActivity.this, MyFirebaseMessaging.class));
    }


    private void startTargetActivity(Map<String, Object> addressMap) {

        Log.d("ttt", "start taget acitivty");

        startService(new Intent(this, MyFirebaseMessaging.class));

        FirebaseFirestore.getInstance().collection("Users")
                .document(currentUser.getUid())
                .get().addOnSuccessListener(snapshot -> {

            if (snapshot.exists()) {
                userType = snapshot.getLong("type").intValue();

                Log.d("ttt", "userType: " + userType);

                if (targetActivity == TO_HOME_ACTIVITY) {

                    startMessagingService();

                    Intent intent = new Intent(MainActivity.this, HomeActivity.class)
                            .putExtra("userType", userType)
                            .putExtra("addressMap", (Serializable) addressMap);

                    if (userType == User.TYPE_ADMIN) {

                        if (snapshot.contains("myRestaurantID")) {

                            String restaurantId = snapshot.getString("myRestaurantID");

                            if (restaurantId == null || restaurantId.isEmpty()) {

                                startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));

                            } else {
                                GlobalVariables.setCurrentRestaurantId(restaurantId);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            startActivity(new Intent(MainActivity.this, RestaurantLocationActivity.class));
                        }

                    } else {
                        startActivity(intent);
                        finish();
                    }

//                    startHomeActivity(addressMap);

                } else if (targetActivity == TO_MESSAGING_ACTIVITY) {

                    startActivity(new Intent(MainActivity.this, MessagingActivity.class)
                            .putExtra("userType", userType)
                            .putExtra("messagingBundle",
                                    getIntent().getBundleExtra("messagingBundle")));

                    finish();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, WelcomeActivity.class)
                        .putExtra("addressMap", (Serializable) addressMap));
                finish();
            }
        });
    }

    private void startHomeActivity(Map<String, Object> addressMap) {

        Intent intent = new Intent(MainActivity.this, MessagingActivity.class)
                .putExtra("userType", userType)
                .putExtra("addressMap", (Serializable) addressMap);

        if (userType == User.TYPE_ADMIN) {


        } else {
            startActivity(intent);
            finish();

        }

    }


    private void startConnectionActivity() {

        activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                    if (result.getResultCode() == ConnectionActivity.CONNECTION_RESULT) {
                        directUserToAppropriateActivity();
                    }

                });

        activityResultLauncher.launch(new Intent(MainActivity.this, ConnectionActivity.class));

    }

//    @Override
//    public void onClick(View v) {
//
//        if (v.getId() == R.id.normalUserBtn) {
//
//            lastClicked = 1;
//            requestLocation();
//
//        } else if (v.getId() == R.id.driverUserBtn) {
//            lastClicked = 2;
//            requestLocation();
//
//        } else if (v.getId() == R.id.restaurantAdminUserBtn) {
//            lastClicked = 3;
//            requestLocation();
//
//        }
//
//    }

}