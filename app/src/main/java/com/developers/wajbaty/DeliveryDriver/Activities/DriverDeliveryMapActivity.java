package com.developers.wajbaty.DeliveryDriver.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Activities.MessagingActivity;
import com.developers.wajbaty.Adapters.DeliveryCourseAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.Models.DeliveryCourse;
import com.developers.wajbaty.Models.DeliveryDriver;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Services.LocationService;
import com.developers.wajbaty.Utils.DirectionsUtil;
import com.developers.wajbaty.Utils.LocationRequester;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DriverDeliveryMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, DeliveryCourseAdapter.DeliverCourseListener,
//        LocationListenerUtil.LocationChangeObserver ,
        LocationRequester.LocationRequestAction,
        LocationService.LocationChangeObserver,
        DirectionsUtil.DirectionsListeners,
        GoogleMap.OnMapClickListener {

    //location
    private static final int MIN_CHECKING_DISTANCE = 5;

    private Delivery delivery;
    private Location currentLocation;
    private HashMap<String, Marker> markerMap;
//    private Marker driverMarker;

    //map
    private GoogleMap map;

    //course
    private DeliveryCourseAdapter adapter;
    private ArrayList<DeliveryCourse> deliveryCourses;
    private ArrayList<DeliveryCourse> allDeliveryCourses;

    //views
    private ImageButton driverDeliveryBackIB, driverDeliveryCurrentLocationIB, driverDeliveryMessageIB;
    private RecyclerView driverDeliveryCourseRv;
    private Button driverDeliveryItemsBtn, driverDeliveryConfirmBtn;
    private ImageView driverDeliveryCourseArrowIv;
    private LinearLayout driverDeliveryLayout;

    //firestore
    private DocumentReference deliveryRef;

    private LocationRequester locationRequester;
    private ServiceConnection serviceConnection;
    private Location currentTargetDestinationLocation;
    private DeliveryCourse currentDeliveryCourse;
    private CollectionReference restaurantsOrderedRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_delivery_map);

        bindToLocationService();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driverDeliveryMap);

        if (mapFragment == null) {
            finish();
            return;
        }

        initializeObjects();

        mapFragment.getMapAsync(this);

        getViews();

        setUpListeners();

    }

    private void initializeObjects() {

        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra("delivery")) {
            delivery = (Delivery) intent.getSerializableExtra("delivery");

            if (intent.hasExtra("currentLocation")) {
//                LatLng latLng = (LatLng) intent.getSerializableExtra("currentLatLng");

                currentLocation = (Location) intent.getParcelableExtra("currentLocation");
                Log.d("ttt", "currentLocation: " + currentLocation.getLatitude() + "," + currentLocation.getLongitude());
            } else {
                requestLocation();
            }
        } else {
            finish();
            return;
        }

        deliveryRef = FirebaseFirestore.getInstance().collection("Deliveries")
                .document(delivery.getID());

        allDeliveryCourses = new ArrayList<>();
        deliveryCourses = new ArrayList<>();
        adapter = new DeliveryCourseAdapter(deliveryCourses, this);

    }

    private void getViews() {


        driverDeliveryBackIB = findViewById(R.id.driverDeliveryBackIB);
        driverDeliveryCurrentLocationIB = findViewById(R.id.driverDeliveryCurrentLocationIB);
        driverDeliveryMessageIB = findViewById(R.id.driverDeliveryMessageIB);
        driverDeliveryCourseRv = findViewById(R.id.driverDeliveryCourseRv);
        driverDeliveryItemsBtn = findViewById(R.id.driverDeliveryItemsBtn);
        driverDeliveryConfirmBtn = findViewById(R.id.driverDeliveryConfirmBtn);
        driverDeliveryCourseArrowIv = findViewById(R.id.driverDeliveryCourseArrowIv);
        driverDeliveryLayout = findViewById(R.id.driverDeliveryLayout);

//
//        driverDeliveryCourseRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false) {
//            @Override
//            public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
//                lp.height = (int) (getWidth() * 0.292);
//                return true;
//                }});

        driverDeliveryCourseRv.setAdapter(adapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setCompassEnabled(true);

        map.setOnMapClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

        } else {

            ActivityResultLauncher<String> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                            granted -> {
                                if (granted) {
                                    map.setMyLocationEnabled(true);
                                }
                            });

            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (currentLocation != null) {

//            LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
//            driverMarker = map.addMarker(new MarkerOptions().position(latLng).title("My Location")
//                    .icon(bitmapDescriptorFromVector()));

            fetchCourse();

            zoomOnCurrentLocation();
//            if(currentLocation == null){
//
//                currentLocation = new Location("currentLocation");
//                currentLocation.setLatitude(latLng.latitude);
//                currentLocation.setLongitude(latLng.longitude);
//
//                if(deliveryCourses.isEmpty()){
//                    fetchCourse();
//                }
//            }

        }

//        checkAndRequestPermissions();

    }


//    @SuppressLint("MissingPermission")
//    private void checkAndRequestPermissions() {
//
//        if (LocationRequester.areLocationPermissionsEnabled(this)) {
//
//            map.setMyLocationEnabled(true);
////            LocationListenerUtil.getInstance().addLocationChangeObserver(this);
//            LocationListenerUtil.getInstance().startListening(this);
//
//            requestLocation();
//
//        } else {
//
//
//            final ActivityResultLauncher<String[]> requestPermissionLauncher =
//                    registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
//                    new ActivityResultCallback<Map<String, Boolean>>() {
//                        @Override
//                        public void onActivityResult(Map<String, Boolean> result) {
//
//                            boolean allAccepted = true;
//
//                            for (String permission : result.keySet()) {
//                                if (!result.get(permission)) {
//                                    allAccepted = false;
//                                    break;
//                                }
//                            }
//
//                            if (allAccepted) {
//
//                                map.setMyLocationEnabled(true);
//
////                                LocationListenerUtil.getInstance().addLocationChangeObserver(DriverDeliveryMapActivity.this);
//                                LocationListenerUtil.getInstance().startListening(DriverDeliveryMapActivity.this);
//
//                                requestLocation();
//                            }
//                        }
//                    });
//
//
//            @SuppressLint("InlinedApi") final String[] permissions = {
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
//            };
//
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//
//                requestPermissionLauncher.launch(permissions);
//
//            }else{
//
//                requestPermissionLauncher.launch(new String[]{permissions[0],permissions[1]});
//
//            }
//
//        }
//    }

    private void requestLocation() {

        if (locationRequester == null) {
            locationRequester = new LocationRequester(this, this);
        }

        locationRequester.getCurrentLocation();

    }

    private void setUpListeners() {

        driverDeliveryBackIB.setOnClickListener(this);
        driverDeliveryItemsBtn.setOnClickListener(this);
        driverDeliveryMessageIB.setOnClickListener(this);
        driverDeliveryCurrentLocationIB.setOnClickListener(this);
        driverDeliveryConfirmBtn.setOnClickListener(this);
        driverDeliveryConfirmBtn.setClickable(false);
        driverDeliveryCourseArrowIv.setOnClickListener(this);
    }

    private void fetchCourse() {

        allDeliveryCourses.add(new DeliveryCourse(
                delivery.getDriverID(),
                "Start Point",
                currentLocation,
                0,
                true,
                false));

//        delivery.getRestaurantMenuItemsMap();

        List<Task<DocumentSnapshot>> restaurantTasks = new ArrayList<>();

        final CollectionReference restaurantRef =
                FirebaseFirestore.getInstance().collection("PartneredRestaurant");

//        final boolean[] isFirst = {true};

        restaurantsOrderedRef = deliveryRef.collection("RestaurantsOrdered");

        restaurantsOrderedRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if (!snapshots.isEmpty()) {

                    final LatLng[] wayPoints = new LatLng[snapshots.size()];

                    int index = 0;
                    for (DocumentSnapshot restaurantSnap : snapshots.getDocuments()) {
                        final int finalIndex = index;
                        restaurantTasks.add(restaurantRef.document(restaurantSnap.getId()).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                        if (documentSnapshot.exists()) {

                                            double lat = documentSnapshot.getDouble("lat"),
                                                    lng = documentSnapshot.getDouble("lng");

                                            wayPoints[finalIndex] = new LatLng(lat, lng);

                                            Log.d("ttt", "course lat: " + lat + " , lng: " + lng);

                                            final String name = documentSnapshot.getString("name");
                                            Location restaurantLocation = new Location(name);
                                            restaurantLocation.setLatitude(lat);
                                            restaurantLocation.setLongitude(lng);

                                            boolean orderPickedUp = restaurantSnap.getBoolean("orderPickedUp");

                                            DeliveryCourse deliveryCourse;

                                            if (!orderPickedUp && currentTargetDestinationLocation == null) {

                                                driverDeliveryConfirmBtn.setText("Confirm pickup from " + name);
                                                deliveryCourse = new DeliveryCourse(
                                                        restaurantSnap.getId(),
                                                        name,
                                                        restaurantLocation,
                                                        restaurantSnap.getLong("itemCount").intValue(),
                                                        restaurantSnap.getBoolean("orderPickedUp"),
                                                        true);

                                                currentDeliveryCourse = deliveryCourse;
                                                currentTargetDestinationLocation = restaurantLocation;
                                            } else {
                                                deliveryCourse = new DeliveryCourse(
                                                        restaurantSnap.getId(),
                                                        name,
                                                        restaurantLocation,
                                                        restaurantSnap.getLong("itemCount").intValue(),
                                                        restaurantSnap.getBoolean("orderPickedUp"),
                                                        false);
                                            }


                                            allDeliveryCourses.add(deliveryCourse);

                                            addMarker(name, restaurantLocation);

                                            Log.d("ttt", "restaurantLocation: " + restaurantLocation.getLatitude()
                                                    + ", " + restaurantLocation.getLongitude());
                                        }

                                    }
                                }));
                        index++;
                    }


                    Tasks.whenAllComplete(restaurantTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Task<?>>> task) {

                            Location deliveryLocation = new Location("deliveryLocation");
                            deliveryLocation.setLatitude(delivery.getLat());
                            deliveryLocation.setLongitude(delivery.getLng());

                            DeliveryCourse deliveryCourse;

                            if (currentTargetDestinationLocation == null) {

                                driverDeliveryConfirmBtn.setText("Confirm order delivered");

                                deliveryCourse = new DeliveryCourse(
                                        delivery.getID()
                                        , "Delivery Location",
                                        deliveryLocation,
                                        0,
                                        false,
                                        true);

                                currentDeliveryCourse = deliveryCourse;
                                currentTargetDestinationLocation = deliveryLocation;
                            } else {

                                deliveryCourse = new DeliveryCourse(
                                        delivery.getID(),
                                        "Delivery Location",
                                        deliveryLocation,
                                        0,
                                        false,
                                        false);

                            }

                            allDeliveryCourses.add(deliveryCourse);

                            addMarker("Delivery Location", deliveryLocation);

                            deliveryCourses.addAll(allDeliveryCourses);

                            adapter.notifyDataSetChanged();

                            final LatLng startLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            final LatLng destinationLatLng = new LatLng(delivery.getLat(), delivery.getLng());

                            new DirectionsUtil(DriverDeliveryMapActivity.this, deliveryRef)
                                    .getDirections(DriverDeliveryMapActivity.this
                                            , startLatLng, wayPoints, destinationLatLng);
                            deliveryRef.collection("Directions")
                                    .document("Directions")
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    if (documentSnapshot.exists()) {
                                        Log.d("DirectionsApi", "gotten result from firestore");
                                        new DirectionsUtil(DriverDeliveryMapActivity.this, deliveryRef)
                                                .getDirections(DriverDeliveryMapActivity.this,
                                                        documentSnapshot.getString("DirectionsJsonObject"));

                                    } else {
                                        Log.d("DirectionsApi", "gotten result from string");
                                        new DirectionsUtil(DriverDeliveryMapActivity.this, deliveryRef)
                                                .getDirections(DriverDeliveryMapActivity.this
                                                        , startLatLng, wayPoints, destinationLatLng);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("DirectionsApi", "gotten result from string");
                                    new DirectionsUtil(DriverDeliveryMapActivity.this, deliveryRef)
                                            .getDirections(DriverDeliveryMapActivity.this
                                                    , startLatLng, wayPoints, destinationLatLng);
                                }
                            });
                        }
                    });


                }

            }
        });

    }


    @Override
    public void notifyObservers(Location location) {

        currentLocation = location;

        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),
                location.getLongitude())));


        if (location.distanceTo(currentTargetDestinationLocation) <= MIN_CHECKING_DISTANCE &&
                !driverDeliveryConfirmBtn.isClickable()) {

            driverDeliveryConfirmBtn.setClickable(true);
            driverDeliveryConfirmBtn.setBackgroundResource(R.drawable.filled_button_background);

        } else if (driverDeliveryConfirmBtn.isClickable()) {
            driverDeliveryConfirmBtn.setClickable(false);
            driverDeliveryConfirmBtn.setBackgroundResource(R.drawable.filled_button_inactive_background);
        }

//
//        if (currentLocation != null &&
//                currentLocation.distanceTo(location) < MIN_UPDATE_DISTANCE) {
//            return;
//        }
//
//        if(currentLocation!=null){
//            deliveryRef.update("lat",currentLocation.getLatitude(),
//                    "lng",currentLocation.getLongitude());
//        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == driverDeliveryBackIB.getId()) {

            finish();

        } else if (v.getId() == driverDeliveryItemsBtn.getId()) {

            startActivity(new Intent(this, DeliveryInfoActivity.class)
                    .putExtra("isForShow", true)
                    .putExtra("delivery", delivery));

        } else if (v.getId() == driverDeliveryCurrentLocationIB.getId()) {

            zoomOnCurrentLocation();

        } else if (v.getId() == driverDeliveryConfirmBtn.getId()) {

            if (currentTargetDestinationLocation != null) {

                if (currentDeliveryCourse.getLocationID().equals(delivery.getID())) {

                    ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
                    progressDialogFragment.setTitle("Waiting user confirmation");
                    progressDialogFragment.setMessage("Please wait!");
                    progressDialogFragment.show(getSupportFragmentManager(), "progress");

                    deliveryRef.update("status", Delivery.STATUS_WAITING_USER_APPROVAL)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    deliveryRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                                            if (value != null) {
                                                if (value.contains("status")) {
                                                    long status = value.getLong("status");
                                                    if (status == Delivery.STATUS_DELIVERED) {

                                                        FirebaseFirestore.getInstance().collection("Users")
                                                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                .update("status", DeliveryDriver.STATUS_AVAILABLE,
                                                                        "currentDeliveryID", null)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        progressDialogFragment.dismiss();
                                                                        Toast.makeText(DriverDeliveryMapActivity.this,
                                                                                "Delivery ended! good job", Toast.LENGTH_SHORT).show();

                                                                        finish();


                                                                    }
                                                                });

                                                    } else if (status == Delivery.STATUS_USER_DENIED_APPROVAL) {

                                                        progressDialogFragment.dismiss();

                                                        Toast.makeText(DriverDeliveryMapActivity.this,
                                                                "User denied confirming delivery!" +
                                                                        " Delivery the users delivery and try again!",
                                                                Toast.LENGTH_LONG).show();

                                                        deliveryRef.update("status", Delivery.STATUS_ACCEPTED);

                                                    }
                                                }
                                            }

                                        }
                                    });


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialogFragment.dismiss();
                            Toast.makeText(DriverDeliveryMapActivity.this,
                                    "Something went wrong please try again!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {

                    restaurantsOrderedRef.document(currentDeliveryCourse.getLocationID())
                            .update("orderPickedUp", true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    String message = "Pick up from restaurant " + currentDeliveryCourse.getLocationName()
                                            + " confirmed";

                                    int index = allDeliveryCourses.indexOf(currentDeliveryCourse);

                                    currentDeliveryCourse.setWasPassed(true);
                                    currentDeliveryCourse.setActive(false);
                                    adapter.notifyItemChanged(index);

                                    if (index == allDeliveryCourses.size() - 2) {

                                        message = message.concat(" !Please proceed to the delivery location!");


                                        currentDeliveryCourse = allDeliveryCourses.get(allDeliveryCourses.size() - 1);
                                        currentTargetDestinationLocation = currentDeliveryCourse.getLocation();
                                        driverDeliveryConfirmBtn.setText("Confirm order delivered");
                                        currentDeliveryCourse.setActive(true);
                                        currentDeliveryCourse.setWasPassed(false);
                                        currentLocation = currentDeliveryCourse.getLocation();
                                        adapter.notifyItemChanged(allDeliveryCourses.size() - 1);


                                    } else {

                                        message = message.concat(" !Please pick up next items from " +
                                                currentDeliveryCourse.getLocationName());

                                        if (index + 1 < allDeliveryCourses.size()) {

                                            currentDeliveryCourse = allDeliveryCourses.get(index + 1);
                                            currentTargetDestinationLocation = currentDeliveryCourse.getLocation();
                                            driverDeliveryConfirmBtn.setText("Confirm pickup from " + currentDeliveryCourse.getLocationName());

                                            currentDeliveryCourse.setActive(true);
                                            currentDeliveryCourse.setWasPassed(false);
                                            currentLocation = currentDeliveryCourse.getLocation();
                                            adapter.notifyItemChanged(index + 1);

                                        }
                                    }
                                    Toast.makeText(DriverDeliveryMapActivity.this, message
                                            , Toast.LENGTH_LONG).show();

                                }
                            });
//
//                    deliveryRef.update("restaurantMenuItemsMap."+currentDeliveryCourse.getLocationID()+".orderPickedUp",
//                            true).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//
//
//
//
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                        }
//                    });

                }


            }

        } else if (v.getId() == driverDeliveryMessageIB.getId()) {

            Intent messagingIntent = new Intent(this, MessagingActivity.class);
            messagingIntent.putExtra("messagingUserId", delivery.getRequesterID());
            messagingIntent.putExtra("intendedDeliveryID", delivery.getID());
            startActivity(messagingIntent);

        } else if (v.getId() == driverDeliveryCourseArrowIv.getId()) {

            if (driverDeliveryCourseRv.getVisibility() == View.GONE) {

                driverDeliveryCourseRv.setVisibility(View.VISIBLE);
                driverDeliveryCourseArrowIv.setRotation(90);


            } else {

                if (driverDeliveryCourseArrowIv.getRotation() == 90) {

                    driverDeliveryCourseArrowIv.setRotation(-90);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            for (int i = 0; i < deliveryCourses.size(); i++) {

                                if (!deliveryCourses.get(i).isWasPassed()) {

                                    Log.d("ttt", "found active at: " + i);

//
//                                if(i+1 >= deliveryCourses.size()){
//
//
////                            deliveryCourses.remove();
//
//                                }else
                                    if (i - 1 >= 0) {

                                        DeliveryCourse previousActive = deliveryCourses.get(i - 1);
                                        DeliveryCourse currentActive = deliveryCourses.get(i);

                                        deliveryCourses.clear();
                                        deliveryCourses.add(previousActive);
                                        deliveryCourses.add(currentActive);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
//                            for(int j=i;j<deliveryCourses.size();j++){
//                                deliveryCourses.remove(j);
//                            }


                                    }

                                    break;
                                }

                            }

                        }
                    }).start();


                } else {

                    driverDeliveryCourseArrowIv.setRotation(90);
                    deliveryCourses.clear();
                    deliveryCourses.addAll(allDeliveryCourses);
                    adapter.notifyDataSetChanged();

                }
            }


        }

    }

    private void zoomOnCurrentLocation() {

        if (currentLocation != null) {

            LatLng currentLatLng =
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());


            if (!map.getCameraPosition().target.equals(currentLatLng)) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18.0f));
            }
        }
    }

    private void addMarker(String name, Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (markerMap == null) {
            markerMap = new HashMap<>();
        }

        markerMap.put(name, map.addMarker(new MarkerOptions().position(latLng).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));

    }

    private BitmapDescriptor bitmapDescriptorFromVector() {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, R.drawable.scooter_marker_icon);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void locationFetched(LatLng latLng) {

        if (currentLocation == null) {

            currentLocation = new Location("currentLocation");
            currentLocation.setLatitude(latLng.latitude);
            currentLocation.setLongitude(latLng.longitude);

            if (deliveryCourses.isEmpty()) {
                fetchCourse();
            }

            zoomOnCurrentLocation();

        }

    }

    private void bindToLocationService() {

        Intent service = new Intent(this, LocationService.class);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                Log.d("ttt", "onServiceConnected");
                LocationService.LocationBinder locationBinder = (LocationService.LocationBinder) service;
                LocationService locationService = locationBinder.getService();
                locationService.addObserver(DriverDeliveryMapActivity.this);

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("ttt", "onServiceDisconnected");
            }
        };

        bindService(service, serviceConnection, 0);

    }

    private void unBindService() {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }


    @Override
    protected void onDestroy() {
        unBindService();
        super.onDestroy();
    }

    @Override
    public void onPolyLineFetched(PolylineOptions polylineOptions) {

        if (polylineOptions != null && map != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.addPolyline(polylineOptions);
                }
            });
        }


    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        if (driverDeliveryCourseRv.getVisibility() == View.VISIBLE) {
            driverDeliveryCourseRv.setVisibility(View.GONE);

            if (driverDeliveryCourseArrowIv.getRotation() == 90) {
                driverDeliveryCourseArrowIv.setRotation(-90);
            }

        }

    }

    @Override
    public void onDeliveryCourseClicked(int position) {

        if (deliveryCourses.size() > position) {

            if (markerMap.containsKey(deliveryCourses.get(position).getLocationName())) {

                Marker marker = markerMap.get(deliveryCourses.get(position).getLocationName());
                if (marker != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18.0f));
                    if (marker.isVisible()) {
                        marker.showInfoWindow();
                    }
                }
            }

        }

    }
}