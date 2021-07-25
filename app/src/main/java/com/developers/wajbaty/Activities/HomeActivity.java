package com.developers.wajbaty.Activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.developers.wajbaty.Customer.Activities.CartActivity;
import com.developers.wajbaty.Customer.Activities.FavoriteActivity;
import com.developers.wajbaty.Customer.Fragments.HomeFragment;
import com.developers.wajbaty.Customer.Fragments.NearbyRestaurantsFragment;
import com.developers.wajbaty.DeliveryDriver.Fragments.DriverDeliveriesFragment;
import com.developers.wajbaty.Fragments.MessagesFragment;
import com.developers.wajbaty.Models.User;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantActivity;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Services.MyFirebaseMessaging;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.developers.wajbaty.Utils.LocationListenerUtil;
import com.developers.wajbaty.Utils.LocationRequester;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        LocationListenerUtil.LocationChangeObserver
{

    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10,
            MIN_UPDATE_DISTANCE = 10;

    //views
    private DrawerLayout homeDrawerLayout;
    private Toolbar homeToolbar;
    private FrameLayout homeFrameLayout;
    private BottomNavigationView homeBottomNavigationView;
    private NavigationView homeNavigationView;
    private Button navigationLogoutBtn;
    private Map<String, Object> addressMap;
    private int userType;


   // driver location
    private Location currentLocation;
    private String currentGeoHash;
    private DocumentReference driverRef;

//    private boolean requestingLocationUpdates;
//    private LocationRequest locationRequest;
//    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");

        LatLng latLng = (LatLng) addressMap.get("latLng");
        Log.d("ttt", "gotten latLng in home intent: " + latLng.latitude + "-" + latLng.longitude);

        getViews();

        setupMenus();

        setClickListeners();


        if (userType == User.TYPE_CUSTOMER) {

            homeBottomNavigationView.setSelectedItemId(R.id.show_home_action);
            replaceFragment(HomeFragment.newInstance(addressMap));

        } else if (userType == User.TYPE_DELIVERY) {

            driverRef = FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            checkAndRequestPermissions();

            homeBottomNavigationView.setSelectedItemId(R.id.show_deliveries_action);
            replaceFragment(DriverDeliveriesFragment.newInstance(addressMap));


        }

    }


    private void getViews() {
        homeDrawerLayout = findViewById(R.id.homeDrawerLayout);
        homeToolbar = findViewById(R.id.homeToolbar);
        homeFrameLayout = findViewById(R.id.homeFrameLayout);
        homeBottomNavigationView = findViewById(R.id.homeBottomNavigationView);
        homeNavigationView = findViewById(R.id.homeNavigationView);
        navigationLogoutBtn = findViewById(R.id.navigationLogoutBtn);

//        new int[homeBottomNavigationView];
//
//        BottomNavigationMenuView bottomNavigationViews = (BottomNavigationMenuView) homeBottomNavigationView[];
//
//        if (bottomNavigationMenuView.isNotEmpty()) {
//            bottomNavigationMenuView[TAB_INDEX].setBackgroundResource(R.drawable.shape_refresh)
//        }
//
//        homeBottomNavigationView.getMenu().findItem(R.id.show_restaurant_action).set
    }

    private void setupMenus() {

        userType = getIntent().getIntExtra("userType", 0);

        int menu;

        switch (userType) {

            default:

                menu = R.menu.customer_bottom_navigation_menu;

                break;
            case User.TYPE_ADMIN:

                menu = R.menu.restaurant_bottom_navigation_menu;

                break;
            case User.TYPE_DELIVERY:

                menu = R.menu.driver_bottom_navigation_menu;

                break;
        }

        homeNavigationView.inflateMenu(R.menu.customer_side_nav_menu);

        homeBottomNavigationView.getMenu().clear();
        homeBottomNavigationView.inflateMenu(menu);

    }

    private void replaceFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction()
                .replace(homeFrameLayout.getId(), fragment).commit();

    }

    private void checkAndRequestPermissions(){

        boolean permissionsGranted;

        @SuppressLint("InlinedApi") final String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };


        if (LocationRequester.areLocationPermissionsEnabled(this)) {

            LocationListenerUtil.getInstance().addLocationChangeObserver(this);
            LocationListenerUtil.getInstance().startListening(this);


//            checkLocationSettings();

        } else {


            ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    new ActivityResultCallback<Map<String, Boolean>>() {
                        @Override
                        public void onActivityResult(Map<String, Boolean> result) {

                            boolean allAccepted = true;

                            for (String permission : result.keySet()) {
                                if (!result.get(permission)) {
                                    allAccepted = false;
                                    break;
                                }
                            }

                            if (allAccepted) {
                                LocationListenerUtil.getInstance().addLocationChangeObserver(HomeActivity.this);
                                LocationListenerUtil.getInstance().startListening(HomeActivity.this);
                            }
                        }
                    });

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                requestPermissionLauncher.launch(permissions);

            }else{

                requestPermissionLauncher.launch(new String[]{permissions[0],permissions[1]});

            }

        }

    }

    private void setClickListeners() {

        homeToolbar.setNavigationOnClickListener(v -> homeDrawerLayout.openDrawer(GravityCompat.START));
        homeBottomNavigationView.setOnItemSelectedListener(this);
        homeNavigationView.setNavigationItemSelectedListener(this);
        navigationLogoutBtn.setOnClickListener(this);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        final int itemId = item.getItemId();

        if (itemId == R.id.show_Messages_action) {

            replaceFragment(new MessagesFragment());

            return true;
        } else if (itemId == R.id.show_restaurant_action) {

            return true;

        }
//        else if (itemId == R.id.show_restaurants_action) {
//
//            return true;
//        }

        else if (itemId == R.id.show_home_action) {

            if (homeBottomNavigationView.getSelectedItemId() != R.id.show_home_action) {
                replaceFragment(HomeFragment.newInstance(addressMap));
                return true;
            }
        } else if (itemId == R.id.show_map_action) {


            if (homeBottomNavigationView.getSelectedItemId() != R.id.show_map_action) {
                replaceFragment(NearbyRestaurantsFragment.newInstance(addressMap));
                return true;
            }
        } else if (itemId == R.id.show_deliveries_action) {

            if (userType == User.TYPE_DELIVERY) {

                replaceFragment(DriverDeliveriesFragment.newInstance(addressMap));

            }

            return true;
        } else if (itemId == R.id.show_profile_action) {

            return true;
        } else if (itemId == R.id.show_restaurant_info_action) {

            closeDrawer();

            startActivity(new Intent(this, RestaurantActivity.class)
                    .putExtra("ID", GlobalVariables.getCurrentRestaurantId())
                    .putExtra("currency", (String) addressMap.get("currency")));

            return true;
        } else if (itemId == R.id.show_cart_action) {

            closeDrawer();
            startActivity(new Intent(this, CartActivity.class)
                    .putExtra("addressMap", (Serializable) addressMap));

            return true;
        } else if (itemId == R.id.show_favorite_action) {

            closeDrawer();
            startActivity(new Intent(this, FavoriteActivity.class));

            return true;
        } else if (itemId == R.id.show_settings_action) {

            return true;
        } else if (itemId == R.id.show_about_us_action) {

            return true;
        } else if (itemId == R.id.show_privacy_policy_action) {

            return true;
        }

        return false;
    }

    private void closeDrawer() {
        homeDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == navigationLogoutBtn.getId()) {

            FirebaseAuth.getInstance().signOut();

//            getPackageManager().setComponentEnabledSetting(
//                    new ComponentName(HomeActivity.this, MyFirebaseMessaging.class),
//                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                    PackageManager.DONT_KILL_APP);

            finish();

        }
    }

    @Override
    public void onBackPressed() {

        if (homeDrawerLayout.isDrawerOpen(GravityCompat.START)) {

            closeDrawer();

        } else {

            super.onBackPressed();

        }
    }

//    @Override
//    public Location onLocationChanged(@NonNull Location location) {
//
//        Log.d("ttt","lat: "+location.getLatitude()+" lng:"+
//                location.getLongitude());
//
//
//        if(currentLocation == null){
//            Log.d("ttt","currentLocation == null");
//            currentGeoHash = GeoFireUtils.getGeoHashForLocation(
//                    new GeoLocation(location.getLatitude(), location.getLongitude()));
//        }
//
//
//        if (currentLocation != null &&
//                currentLocation.distanceTo(location) < MIN_UPDATE_DISTANCE) {
//            return null;
//        }
//
//        Log.d("ttt", "new location is: " +
//                location.getLatitude() + "-" + location.getLongitude());
//
//
//        driverRef.update(
//                "currentGeoPoint",
//                new GeoPoint( location.getLatitude(), location.getLongitude())).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//
//                Log.d("ttt","updated driver location");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//                Log.d("ttt",e.getMessage());
//
//            }
//        });
//
//
//        String hash = GeoFireUtils.getGeoHashForLocation(
//                new GeoLocation(location.getLatitude(), location.getLongitude()));
//
//        Log.d("ttt","currentGeoHash: "+currentGeoHash);
//        Log.d("ttt","new hash: "+hash);
//
//
//        if(!hash.equals(currentGeoHash)){
//
//            currentGeoHash = hash;
//
//            driverRef.update("geohash",hash).addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//
//                    Log.d("ttt","updated driver geohash");
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//
//                    Log.d("ttt",e.getMessage());
//
//                }
//            });
//        }
//
//
//        currentLocation = location;
//
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (requestingLocationUpdates) {
//            startLocationUpdates();
//        }
//
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopLocationUpdates();
//    }

//    private void startLocationUpdates() {
//
//        fusedLocationClient.requestLocationUpdates(locationRequest,
//                locationCallback,
//                Looper.getMainLooper());
//
//    }

//    private void stopLocationUpdates() {
//        fusedLocationClient.removeLocationUpdates(locationCallback);
//    }

//    private void checkLocationSettings() {
//
//        final LocationRequest locationRequest = LocationRequest.create().
//                setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
//                .setInterval(10000).setFastestInterval(5000);
//
//        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                .addLocationRequest(locationRequest);
//
//        LocationServices.getSettingsClient(this)
//                .checkLocationSettings(builder.build())
//                .addOnSuccessListener(locationSettingsResponse -> {
//                    Log.d("ttt", "location is enabled");
//
//                    LocationManager locationManager = (LocationManager)
//                            getSystemService(Context.LOCATION_SERVICE);
//
//                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        checkAndRequestPermissions();
//                        return;
//                    }
//
//                    locationManager.requestLocationUpdates(
//                            LocationManager.GPS_PROVIDER,
//                            5000,
//                            10,
//                            HomeActivity.this);
//
//                }).addOnFailureListener(e -> {
//            if (e instanceof ResolvableApiException) {
//                Log.d("ttt", "location is not enabled");
//                try {
//                    // Show the dialog by calling startResolutionForResult(),
//                    // and check the result in onActivityResult().
//                    ResolvableApiException resolvable = (ResolvableApiException) e;
//                    resolvable.startResolutionForResult(HomeActivity.this,
//                            REQUEST_CHECK_SETTINGS);
//                } catch (IntentSender.SendIntentException sendEx) {
//                    // Ignore the error.
//                }
//            }
//        });
//
//    }

    @Override
    public void notifyObservers(Location location) {

                Log.d("ttt","lat: "+location.getLatitude()+" lng:"+
                location.getLongitude());


        if(currentLocation == null){
            Log.d("ttt","currentLocation == null");
            currentGeoHash = GeoFireUtils.getGeoHashForLocation(
                    new GeoLocation(location.getLatitude(), location.getLongitude()));
        }


        if (currentLocation != null &&
                currentLocation.distanceTo(location) < MIN_UPDATE_DISTANCE) {
            return;
        }

        Log.d("ttt", "new location is: " +
                location.getLatitude() + "-" + location.getLongitude());


        driverRef.update(
                "currentGeoPoint",
                new GeoPoint( location.getLatitude(), location.getLongitude())).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Log.d("ttt","updated driver location");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("ttt",e.getMessage());

            }
        });


        String hash = GeoFireUtils.getGeoHashForLocation(
                new GeoLocation(location.getLatitude(), location.getLongitude()));

        Log.d("ttt","currentGeoHash: "+currentGeoHash);
        Log.d("ttt","new hash: "+hash);


        if(!hash.equals(currentGeoHash)){

            currentGeoHash = hash;

            driverRef.update("geohash",hash).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Log.d("ttt","updated driver geohash");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d("ttt",e.getMessage());

                }
            });
        }


        currentLocation = location;

    }

//
//    @Override
//    public void onProviderDisabled(@NonNull String provider) {
//
//        Log.d("ttt","provider disabled: "+provider);
//    }
//
//    @Override
//    public void onProviderEnabled(@NonNull String provider) {
//
//        Log.d("ttt","provider enabled: "+provider);
//
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//        Log.d("ttt","status changed for provider: "+provider + " to: "+
//                status);
//
//
//    }
}