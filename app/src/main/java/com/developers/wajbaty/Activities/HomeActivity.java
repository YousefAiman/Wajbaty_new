package com.developers.wajbaty.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.developers.wajbaty.BuildConfig;
import com.developers.wajbaty.Customer.Activities.CartActivity;
import com.developers.wajbaty.Customer.Activities.CustomerDeliveryMapActivity;
import com.developers.wajbaty.Customer.Activities.CustomerProfileActivity;
import com.developers.wajbaty.Customer.Activities.FavoriteActivity;
import com.developers.wajbaty.Customer.Fragments.DeliveryDriverInfoFragment;
import com.developers.wajbaty.Customer.Fragments.HomeFragment;
import com.developers.wajbaty.Customer.Fragments.MenuItemsFragment;
import com.developers.wajbaty.Customer.Fragments.NearbyRestaurantsFragment;
import com.developers.wajbaty.DeliveryDriver.Fragments.DriverDeliveriesFragment;
import com.developers.wajbaty.Fragments.MessagesFragment;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.Models.DeliveryDriver;
import com.developers.wajbaty.Models.DeliveryModel;
import com.developers.wajbaty.Models.Notification;
import com.developers.wajbaty.Models.User;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantActivity;
import com.developers.wajbaty.PartneredRestaurant.Fragments.RestaurantOrdersFragment;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Services.LocationService;
import com.developers.wajbaty.Services.MyFirebaseMessaging;
import com.developers.wajbaty.Utils.BadgeUtil;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.developers.wajbaty.Utils.LocationListenerUtil;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class HomeActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        LocationService.LocationChangeObserver {


    private Map<String, Object> addressMap;
    private long userType;


    //views
    private DrawerLayout homeDrawerLayout;
    private Toolbar homeToolbar;
    private FrameLayout homeFrameLayout;
    private BottomNavigationView homeBottomNavigationView;
    private NavigationView homeNavigationView;
    private Button navigationLogoutBtn;
    private ImageView headerUserImageIv;
    private TextView headerUsernameTv, notificationBadgeTv;

    // driver location
    private Location currentLocation;
    private String currentGeoHash;
    private DocumentReference userRef;
    private DocumentReference currentDriverDeliveryRef;
    private ListenerRegistration userSnapshotListener;

    private Intent service;

    private ServiceConnection serviceConnection;

    private List<ListenerRegistration> snapshotListeners;


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d("startupTime", "end time: " + System.currentTimeMillis());

        final Intent intent = getIntent();

        if (intent != null) {

            if (intent.hasExtra("addressMap")) {
                addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");
            }

            if (intent.hasExtra("userType")) {
                userType = getIntent().getIntExtra("userType", 0);
                Log.d("ttt", "userType: " + userType);

            }

        } else {
            finish();
            return;
        }


        LatLng latLng = (LatLng) addressMap.get("latLng");
        Log.d("ttt", "gotten latLng in home intent: " + latLng.latitude + "-" + latLng.longitude);

        getViews();

        setupMenus();

        setClickListeners();


        userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        listenToNotifications();

        if (userType == User.TYPE_CUSTOMER) {

//            homeToolbar.inflateMenu(R.menu.customer_home_menu);
            replaceFragment(HomeFragment.newInstance(addressMap), "HomeFragment");
            homeBottomNavigationView.setSelectedItemId(R.id.show_home_action);

            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if (documentSnapshot.exists()) {

                        if (documentSnapshot.contains("imageURL")) {
                            String imageUrl = documentSnapshot.getString("imageURL");

                            if (imageUrl != null && !imageUrl.isEmpty()) {

                                loadHeaderUserImage(imageUrl);

                            }
                        }

                        if (documentSnapshot.contains("name")) {
                            String name = documentSnapshot.getString("name");

                            if (name != null && !name.isEmpty()) {
                                headerUsernameTv.setText(name);
                            }
                        }

                    }

                }
            });

            registerReceiver(new DeliveryDriverRequestReceiver(),
                    new IntentFilter(BuildConfig.APPLICATION_ID + ".driverRequest"));
//            userSnapshotListener = userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                boolean isInitial = true;
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//
//                    if(value != null){
//
//                        if(isInitial){
//
//                            if(value.contains("imageURL")){
//                                String imageUrl = value.getString("imageURL");
//
//                                if(imageUrl!=null && !imageUrl.isEmpty()){
//                                    Picasso.get().load(imageUrl).fit().centerCrop().into(headerUserImageIv);
//                                }
//                            }
//
//                            if(value.contains("name")){
//                                String name = value.getString("name");
//
//                                if(name!=null && !name.isEmpty()){
//                                    headerUsernameTv.setText(name);
//                                }
//                            }
//
//                        }
//
//                    }else{
//
//
//                    }
//
//                }
//            });

//            replaceFragment(HomeFragment.newInstance(addressMap));
//            homeBottomNavigationView.setSelectedItemId(R.id.show_home_action);

        } else if (userType == User.TYPE_DELIVERY) {

            userSnapshotListener = userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                private boolean isInitial = true;
                private long currentStatus;

                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                    if (value != null) {

                        if (value.contains("currentDeliveryID")) {

                            String currentDeliveryId = value.getString("currentDeliveryID");

                            if (currentDeliveryId != null && !currentDeliveryId.isEmpty()) {
                                currentDriverDeliveryRef = FirebaseFirestore.getInstance()
                                        .collection("Deliveries").document(currentDeliveryId);

                            } else {
                                currentDriverDeliveryRef = null;
                            }

                        } else {

                            currentDriverDeliveryRef = null;

                        }

                        if (isInitial) {

                            if (value.contains("imageURL")) {
                                String imageUrl = value.getString("imageURL");

                                if (imageUrl != null && !imageUrl.isEmpty()) {

                                    loadHeaderUserImage(imageUrl);
                                }
                            }
                            if (value.contains("name")) {
                                String name = value.getString("name");

                                if (name != null && !name.isEmpty()) {
                                    headerUsernameTv.setText(name);
                                }
                            }

                            if (value.contains("status")) {
                                currentStatus = value.getLong("status");


                                if (currentStatus == DeliveryDriver.STATUS_AVAILABLE ||
                                        currentStatus == DeliveryDriver.STATUS_DELIVERING) {
                                    startLocationService();
                                }
//                                else if(status == DeliveryDriver.STATUS_UNAVAILABLE){
//                                    stopService();
//                                }
                            }

                            isInitial = false;
                        } else {

                            if (value.contains("status")) {
                                long status = value.getLong("status");

                                if ((status == DeliveryDriver.STATUS_AVAILABLE &&
                                        currentStatus != DeliveryDriver.STATUS_AVAILABLE)
                                        || (status == DeliveryDriver.STATUS_DELIVERING &&
                                        currentStatus != DeliveryDriver.STATUS_DELIVERING)) {

                                    startLocationService();

                                } else if (status == DeliveryDriver.STATUS_UNAVAILABLE
                                        && currentStatus != DeliveryDriver.STATUS_UNAVAILABLE) {
                                    stopService();
                                }
                                currentStatus = status;
                            }

                        }


                    }

                }
            });

//            checkAndRequestPermissions();

            homeBottomNavigationView.setSelectedItemId(R.id.show_deliveries_action);
            replaceFragment(DriverDeliveriesFragment.newInstance(addressMap), "DriverDeliveriesFragment");


        } else {

            FirebaseFirestore.getInstance().collection("PartneredRestaurant")
                    .document(GlobalVariables.getCurrentRestaurantId())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (snapshot.exists()) {

                        loadHeaderUserImage(snapshot.getString("mainImage"));

                        headerUsernameTv.setText(snapshot.getString("name"));
                    }
                }
            });

        }

    }

    private void loadHeaderUserImage(String imageUrl) {

        final CircularProgressDrawable progressDrawable = new CircularProgressDrawable(HomeActivity.this);
        progressDrawable.setColorSchemeColors(getResources().getColor(R.color.orange));
        progressDrawable.setStyle(CircularProgressDrawable.LARGE);

        Picasso.get().load(imageUrl).fit().centerCrop().placeholder(progressDrawable).into(headerUserImageIv);

    }


    private void getViews() {
        homeDrawerLayout = findViewById(R.id.homeDrawerLayout);
        homeToolbar = findViewById(R.id.homeToolbar);
        homeFrameLayout = findViewById(R.id.homeFrameLayout);
        homeBottomNavigationView = findViewById(R.id.homeBottomNavigationView);
        homeNavigationView = findViewById(R.id.homeNavigationView);
        navigationLogoutBtn = findViewById(R.id.navigationLogoutBtn);

        View headerView = homeNavigationView.getHeaderView(0);

        headerUserImageIv = headerView.findViewById(R.id.headerUserImageIv);
        headerUsernameTv = headerView.findViewById(R.id.headerUsernameTv);


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

        int bottomMenu;
        int sideMenu;

        int userTypeInt = ((Long) userType).intValue();
        switch (userTypeInt) {

            default:

                bottomMenu = R.menu.customer_bottom_navigation_menu;
                sideMenu = R.menu.customer_side_nav_menu;

                break;
            case User.TYPE_ADMIN:

                bottomMenu = R.menu.restaurant_bottom_navigation_menu;
                sideMenu = R.menu.restaurant_side_nav_menu;
                break;
            case User.TYPE_DELIVERY:

                bottomMenu = R.menu.driver_bottom_navigation_menu;
                sideMenu = R.menu.driver_side_nav_menu;
                break;
        }

        homeBottomNavigationView.getMenu().clear();
        homeBottomNavigationView.inflateMenu(bottomMenu);

        homeNavigationView.inflateMenu(sideMenu);

        notificationBadgeTv = (TextView) LayoutInflater.from(this).inflate(R.layout.badge_counter_layout, null);
        homeNavigationView.getMenu().findItem(R.id.show_notifications_action).setActionView(notificationBadgeTv);


    }

    private void replaceFragment(Fragment fragment, String tag) {

        getSupportFragmentManager().beginTransaction()
                .replace(homeFrameLayout.getId(), fragment, tag).commit();

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

        if (itemId == R.id.show_myProfile_action) {
            closeDrawer();
            startActivity(new Intent(this, CustomerProfileActivity.class));
            return true;
        } else if (itemId == R.id.show_Messages_action) {

            if (homeBottomNavigationView.getSelectedItemId() != R.id.show_Messages_action) {
                replaceFragment(new MessagesFragment(), "MessagesFragment");
            }

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
                replaceFragment(HomeFragment.newInstance(addressMap), "HomeFragment");
                return true;
            }
        } else if (itemId == R.id.show_Menu_items_action) {

            if (homeBottomNavigationView.getSelectedItemId() != R.id.show_Menu_items_action) {

                String region = null;
//
//                if(addressMap.containsKey("adminArea")){
//                    region = (String) addressMap.get("adminArea");
//                }else if(addressMap.containsKey("village")){
//                    region = (String) addressMap.get("village");
//                } else if (addressMap.containsKey("region")) {
//                    region = (String) addressMap.get("region");
//                } else if (addressMap.containsKey("city")) {
//                    region = (String) addressMap.get("city");
//                } else if (addressMap.containsKey("county")) {
//                    region = (String) addressMap.get("county");
//                }

                if (addressMap.containsKey("adminArea")) {
                    region = (String) addressMap.get("adminArea");
                } else if (addressMap.containsKey("region")) {
                    region = (String) addressMap.get("region");
                } else if (addressMap.containsKey("county")) {
                    region = (String) addressMap.get("county");
                } else if (addressMap.containsKey("city")) {
                    region = (String) addressMap.get("city");
                }

                replaceFragment(MenuItemsFragment.newInstance(region), "MenuItemsFragment");

                return true;
            }
        } else if (itemId == R.id.show_map_action) {


            if (homeBottomNavigationView.getSelectedItemId() != R.id.show_map_action) {
                replaceFragment(NearbyRestaurantsFragment.newInstance(addressMap), "NearbyRestaurantsFragment");
                return true;
            }
        } else if (itemId == R.id.show_deliveries_action) {

            if (userType == User.TYPE_DELIVERY && homeBottomNavigationView.getSelectedItemId() != R.id.show_deliveries_action) {

                replaceFragment(DriverDeliveriesFragment.newInstance(addressMap), "DriverDeliveriesFragment");

            }

            return true;
        } else if (itemId == R.id.show_profile_action) {

            return true;
        } else if (itemId == R.id.show_orders_action) {

            if (homeBottomNavigationView.getSelectedItemId() != R.id.show_orders_action) {
                replaceFragment(RestaurantOrdersFragment.newInstance("fdas", "asda"), "ordersFragment");
            }

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
        } else if (itemId == R.id.show_notifications_action) {

            closeDrawer();
            startActivity(new Intent(this, NotificationsActivity.class));

            return true;
        }
//        else if (itemId == R.id.show_settings_action) {
//
//            return true;
//        }
        else if (itemId == R.id.show_about_us_action) {
            closeDrawer();
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;
        }
//        else if (itemId == R.id.show_privacy_policy_action) {
//
//            return true;
//        }

        return false;
    }

    private void closeDrawer() {
        homeDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == navigationLogoutBtn.getId()) {

            FirebaseAuth.getInstance().signOut();

            try {
                stopService(new Intent(this, MyFirebaseMessaging.class));

            } catch (SecurityException | IllegalStateException e) {
                if (e.getMessage() != null) {
                    Log.d("ttt", "can't stop serivce: " + e.getMessage());
                }
            }

            if (Build.VERSION.SDK_INT < 26) {
                BadgeUtil.clearBadge(this);
            }

            stopService();

            startActivity(new Intent(this, WelcomeActivity.class)
                    .putExtra("addressMap", (Serializable) addressMap));

            finish();

//            getPackageManager().setComponentEnabledSetting(
//                    new ComponentName(HomeActivity.this, MyFirebaseMessaging.class),
//                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                    PackageManager.DONT_KILL_APP);


        }
    }

    @Override
    public void onBackPressed() {

        if (homeDrawerLayout.isDrawerOpen(GravityCompat.START)) {

            closeDrawer();

        } else {

            final List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            String lastFragTag = null;

            if (!fragmentList.isEmpty()) {
                lastFragTag = fragmentList.get(fragmentList.size() - 1).getTag();
            }

            if (lastFragTag != null) {

                if (userType == User.TYPE_CUSTOMER && !lastFragTag.equals("HomeFragment")) {
                    replaceFragment(HomeFragment.newInstance(addressMap), "HomeFragment");
                    homeBottomNavigationView.setSelectedItemId(R.id.show_home_action);
                } else if (userType == User.TYPE_DELIVERY && !lastFragTag.equals("DriverDeliveriesFragment")) {
                    replaceFragment(DriverDeliveriesFragment.newInstance(addressMap), "DriverDeliveriesFragment");
                    homeBottomNavigationView.setSelectedItemId(R.id.show_deliveries_action);
                } else if (userType == User.TYPE_ADMIN && !lastFragTag.equals("HomeFragment")) {


                } else {
                    super.onBackPressed();
                }


            } else {
                super.onBackPressed();
            }

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
//        userRef.update(
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
//            userRef.update("geohash",hash).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        Log.d("ttt", "lat: " + location.getLatitude() + " lng:" +
                location.getLongitude());


        if (currentLocation == null) {
            Log.d("ttt", "currentLocation == null");
            currentGeoHash = GeoFireUtils.getGeoHashForLocation(
                    new GeoLocation(location.getLatitude(), location.getLongitude()));
        }

//
//        if (currentLocation != null &&
//                currentLocation.distanceTo(location) < MIN_UPDATE_DISTANCE) {
//            return;
//        }

        Log.d("ttt", "new location is: " +
                location.getLatitude() + "-" + location.getLongitude());


        userRef.update(
                "currentGeoPoint",
                new GeoPoint(location.getLatitude(), location.getLongitude())).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Log.d("ttt", "updated driver location");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("ttt", e.getMessage());

            }
        });

        if (currentDriverDeliveryRef != null) {
            currentDriverDeliveryRef.update("lat", location.getLatitude(),
                    "lng", location.getLongitude());
        }

        String hash = GeoFireUtils.getGeoHashForLocation(
                new GeoLocation(location.getLatitude(), location.getLongitude()));

        Log.d("ttt", "currentGeoHash: " + currentGeoHash);
        Log.d("ttt", "new hash: " + hash);


        if (!hash.equals(currentGeoHash)) {

            currentGeoHash = hash;

            userRef.update("geohash", hash).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Log.d("ttt", "updated driver geohash");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d("ttt", e.getMessage());

                }
            });
        }


        currentLocation = location;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (userSnapshotListener != null) {
            userSnapshotListener.remove();
        }

        if (snapshotListeners != null && !snapshotListeners.isEmpty()) {
            for (ListenerRegistration listenerRegistration : snapshotListeners) {
                listenerRegistration.remove();
            }
        }

    }

    private void startLocationService() {

        if (!LocationListenerUtil.isLocationServiceRunning(this)) {

            service = new Intent(this, LocationService.class);

            startService(service);

            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    Log.d("ttt", "onServiceConnected");
                    LocationService.LocationBinder locationBinder = (LocationService.LocationBinder) service;
                    LocationService locationService = locationBinder.getService();
                    locationService.addObserver(HomeActivity.this);

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("ttt", "onServiceDisconnected");
                }
            };

            bindService(service, serviceConnection, 0);

        }
    }

    private void stopService() {

//        if(locationService!=null){
//            locationService.stopForeground(true);
//        }

        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }

        if (service != null) {
            stopService(service);
        }
    }

    private void listenToNotifications() {

        if (snapshotListeners == null)
            snapshotListeners = new ArrayList<>();

        snapshotListeners.add(userRef.collection("Notifications")
                .whereEqualTo("seen", false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    private int notificationCount = 0, messageNotificationCount = 0;
                    private BadgeDrawable messageBadgeDrawable;

                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value == null)
                            return;

                        for (DocumentChange dc : value.getDocumentChanges()) {

                            int type = 0;

                            DocumentSnapshot documentSnapshot = dc.getDocument();
                            if (documentSnapshot.contains("type")) {
                                Long longType = documentSnapshot.getLong("type");
                                if (longType != null) {
                                    type = longType.intValue();
                                }
                            }

                            switch (dc.getType()) {
                                case ADDED:
                                    notificationCount++;

                                    if (type == Notification.TYPE_MESSAGE)
                                        messageNotificationCount++;

                                    break;
                                case REMOVED:
                                    notificationCount--;

                                    if (type == Notification.TYPE_MESSAGE)
                                        messageNotificationCount--;

                                    break;
                            }


                            if (notificationCount > 0) {

                                if (notificationBadgeTv.getVisibility() == View.INVISIBLE) {
                                    notificationBadgeTv.setVisibility(View.VISIBLE);
                                }

                                notificationBadgeTv.setText(String.valueOf(notificationCount));

                            } else {
                                if (notificationBadgeTv.getVisibility() == View.VISIBLE) {
                                    notificationBadgeTv.setVisibility(View.INVISIBLE);
                                }
                            }

                            if (messageNotificationCount > 0) {

                                if (messageBadgeDrawable == null) {
                                    messageBadgeDrawable = homeBottomNavigationView.getOrCreateBadge(R.id.show_Messages_action);
                                    messageBadgeDrawable.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.orange, null));
                                }

                                if (!messageBadgeDrawable.isVisible()) {
                                    messageBadgeDrawable.setVisible(true);
                                }
                                messageBadgeDrawable.setNumber(messageNotificationCount);
                            } else {
                                if (messageBadgeDrawable != null && messageBadgeDrawable.isVisible()) {
                                    messageBadgeDrawable.setVisible(false);
                                }
                            }
//                            badgeDrawable.setVisible(count != 0);
//                            badgeDrawable.setNumber(count);
                        }

                    }
                }));

    }

    public class DeliveryDriverRequestReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("ttt", "DeliveryDriverRequestReceiver received");

            if (intent.hasExtra("driverID") && intent.hasExtra("delivery")) {

                final String driverID = intent.getStringExtra("driverID");
                final Delivery delivery = (Delivery) intent.getSerializableExtra("delivery");

                if (driverID == null || driverID.isEmpty())
                    return;

                DeliveryModel model = new DeliveryModel(delivery, context);
                model.addObserver(new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {

                        if (arg instanceof Integer) {

                            int result = (int) arg;

                            switch (result) {

                                case DeliveryModel.DELIVERY_STARTED:

                                    startActivity(new Intent(HomeActivity.this, CustomerDeliveryMapActivity.class)
                                            .putExtra("delivery", delivery));

                                    break;

                            }
                        }

                    }
                });

                if (getSupportFragmentManager().isDestroyed())
                    return;

                DeliveryDriverInfoFragment.newInstance(driverID,
                        new DeliveryDriverInfoFragment.DeliveryListener() {
                            @Override
                            public void startDelivery() {

                                model.acceptDriverRequest();

//                            DriverDeliveryMapActivity

                                Toast.makeText(HomeActivity.this,
                                        "Delivery Started", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void cancelDelivery() {

                                model.refuseDriverRequest();

                                Toast.makeText(HomeActivity.this,
                                        "Delivery Cancelled", Toast.LENGTH_SHORT).show();

                            }
                        }).show(getSupportFragmentManager(), "DeliveryDriverInfo");
            }


            Log.d("ttt", "delivery driver request received from receiver");

        }
    }


}