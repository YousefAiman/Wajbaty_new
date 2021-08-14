package com.developers.wajbaty.Customer.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.developers.wajbaty.Customer.Fragments.DeliveryDriverInfoFragment;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.CartItem;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.Models.DeliveryModel;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.BitmapUtil;
import com.developers.wajbaty.Utils.GeocoderUtil;
import com.developers.wajbaty.Utils.LocationRequester;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

public class DeliveryLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, GoogleMap.OnMapClickListener, LocationRequester.LocationRequestAction,
        GeocoderUtil.GeocoderResultListener, Observer {

    private static final int BUTTON_ACTIVE = 1, BUTTON_INACTIVE = 2;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private ArrayList<CartItem> cartItemList;
    private HashMap<String, Object> addressMap;
    private DeliveryModel model;
    private ListenerRegistration deliveryAcceptanceListener;

    private GoogleMap map;
    private LocationRequester locationRequester;
    private Marker currentMapMarker;
    private String chosenAddress;

    //views
    private TextView deliveryLocationPlaceNameTv;
    private TextView deliveryLocationCoordinatesTv;
    private TextView deliveryLocationAddressTv;
    private ImageButton deliveryLocationCurrentLocationIv;
    private Button deliveryLocationConfirmBtn;

    private ProgressDialogFragment progressDialogFragment;

    private Delivery currentDelivery;

    public static void main(String[] args) {

        final List<String> ids = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            ids.add("text");
        }

        for (int i = 0; i < ids.size(); i += 10) {

            System.out.println("size: " + ids.subList(i, Math.min(ids.size(), i + 10)).size());

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_location_map);

        final Intent intent = getIntent();

        if (intent != null && intent.hasExtra("cartItems")) {

            cartItemList = (ArrayList<CartItem>) intent.getSerializableExtra("cartItems");
            addressMap = (HashMap<String, Object>) intent.getSerializableExtra("addressMap");

        } else {
            finish();
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.deliveryLocationMap);

        mapFragment.getMapAsync(this);


        getViews();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;
        map.setOnMapClickListener(this);


        markCurrentPosition();

    }

    private void getViews() {

        deliveryLocationPlaceNameTv = findViewById(R.id.deliveryLocationPlaceNameTv);
        deliveryLocationCoordinatesTv = findViewById(R.id.deliveryLocationCoordinatesTv);
        deliveryLocationAddressTv = findViewById(R.id.deliveryLocationAddressTv);
        deliveryLocationCurrentLocationIv = findViewById(R.id.deliveryLocationCurrentLocationIv);
        deliveryLocationConfirmBtn = findViewById(R.id.deliveryLocationConfirmBtn);

        deliveryLocationConfirmBtn.setTag(BUTTON_INACTIVE);

        deliveryLocationCurrentLocationIv.setOnClickListener(this);
        deliveryLocationConfirmBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == deliveryLocationCurrentLocationIv.getId()) {

            if (locationRequester == null) {

                initializeLocationRequester();

            } else {
                locationRequester.getCurrentLocation();
            }


        } else if (v.getId() == deliveryLocationConfirmBtn.getId()) {

            if (chosenAddress == null || currentMapMarker == null)
                return;

            progressDialogFragment = new ProgressDialogFragment();
            progressDialogFragment.setTitle("Creating your delivery");
            progressDialogFragment.setMessage("Please wait");
            progressDialogFragment.show(getSupportFragmentManager(), "progress");

            final String ID = UUID.randomUUID().toString();

            final List<String> ids = new ArrayList<>();

            for (CartItem cartItem : cartItemList) {
                ids.add(cartItem.getItemId());
            }


            final Query query = FirebaseFirestore.getInstance().collection("MenuItems");

            final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

            final int loopNum = ids.size() / 10;

            if (loopNum == 0) {

                tasks.add(query.whereIn("id", ids).get());

            } else {

                for (int i = 0; i < ids.size(); i += 10) {
                    tasks.add(query.whereIn("id", ids.subList(i, Math.min(ids.size(), i + 10))).get());
                }
            }

            final float[] totalCost = {0};

            Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                @Override
                public void onComplete(@NonNull Task<List<Task<?>>> task) {

                    final HashMap<String, Float> menuItemPriceMap = new HashMap<>();

                    final HashMap<String, HashMap<String, Object>> restaurantMenuItemsMap = new HashMap<>();

                    for (Task<QuerySnapshot> queryTask : tasks) {
                        if (queryTask.isSuccessful() && queryTask.getResult() != null && !queryTask.getResult().isEmpty()) {
                            for (DocumentSnapshot snapshot : queryTask.getResult()) {

                                float price = snapshot.getDouble("price").floatValue();

                                totalCost[0] += price;

                                menuItemPriceMap.put(snapshot.getId(), price);

                                final String restaurantID = snapshot.getString("restaurantId");

                                if (restaurantMenuItemsMap.containsKey(restaurantID)) {

//                                    restaurantMenuItemsMap.put(restaurantID,
//                                            restaurantMenuItemsMap.get(restaurantID)+1);

                                    HashMap<String, Object> restaurantMap = restaurantMenuItemsMap.get(restaurantID);

                                    if (restaurantMap != null) {
                                        restaurantMap.put("itemCount", ((Integer) restaurantMap.get("itemCount")) + 1);
                                    }

                                } else {
                                    HashMap<String, Object> restaurantMap = new HashMap<>();
                                    restaurantMap.put("itemCount", 1);
                                    restaurantMap.put("orderPickedUp", false);

                                    restaurantMenuItemsMap.put(restaurantID, restaurantMap);
//
//                                    restaurantMenuItemsMap.put(restaurantID,1);

                                }

                            }
                        }
                    }

                    final GeoLocation chosenLocation = new GeoLocation(
                            currentMapMarker.getPosition().latitude,
                            currentMapMarker.getPosition().longitude);

                    currentDelivery = new Delivery(
                            ID,
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            menuItemPriceMap,
                            Delivery.STATUS_PENDING,
                            System.currentTimeMillis(),
                            totalCost[0],
                            (String) addressMap.get("currency"),
                            chosenAddress,
                            chosenLocation.latitude,
                            chosenLocation.longitude,
                            GeoFireUtils.getGeoHashForLocation(chosenLocation),
                            restaurantMenuItemsMap.size()
//                             ,
//                            restaurantMenuItemsMap
                    );


                    final CollectionReference restaurantOrderedRef = FirebaseFirestore.getInstance()
                            .collection("Deliveries").document(ID)
                            .collection("RestaurantsOrdered");

                    List<Task<?>> restaurantOrderedTasks = new ArrayList<>();

                    for (String key : restaurantMenuItemsMap.keySet()) {
                        restaurantOrderedTasks.add(
                                restaurantOrderedRef.document(key).set(restaurantMenuItemsMap.get(key)));
                    }

                    Tasks.whenAllComplete(restaurantOrderedTasks).addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                        @Override
                        public void onSuccess(List<Task<?>> tasks) {
                            model = new DeliveryModel(currentDelivery, DeliveryLocationMapActivity.this);
                            model.addObserver(DeliveryLocationMapActivity.this);
                            model.requestDelivery(addressMap, cartItemList);
                        }
                    });


                }
            });


        }

    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        markLocation(latLng);

    }

    private void markCurrentPosition() {

        Log.d("ttt", "markCurrentPosition");

        final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

            Log.d("ttt", "requesting location persmission");

            requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);

        } else if (currentMapMarker == null) {

            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);

            initializeLocationRequester();
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);

                initializeLocationRequester();

            } else {

                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);

                markLocation(new LatLng(-34, 151));

            }
        }

    }

    private void showProgressDialog() {
        Log.d("ttt", "showProgressDialog");

        if (progressDialogFragment == null) {
            progressDialogFragment = new ProgressDialogFragment();
        }

        progressDialogFragment.show(getSupportFragmentManager(), "progress");
    }

    private void initializeLocationRequester() {

        Log.d("ttt", "initializeLocationRequester");

        showProgressDialog();

        locationRequester = new LocationRequester(this, this);
        locationRequester.getCurrentLocation();

    }

    private void markLocation(LatLng latLng) {

        GeocoderUtil.getLocationAddress(this, latLng, this);

        if ((int) deliveryLocationConfirmBtn.getTag() == BUTTON_INACTIVE) {

            deliveryLocationConfirmBtn.setTag(BUTTON_ACTIVE);
            deliveryLocationConfirmBtn.setBackgroundResource(R.drawable.filled_button_background);

        }

        if (currentMapMarker != null)
            currentMapMarker.remove();


        currentMapMarker = map.addMarker(new MarkerOptions().position(latLng).title("Restaurant location"));

        if (currentMapMarker != null) {
            currentMapMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                    BitmapUtil.getBitmap(this, R.drawable.location_marker_icon)));
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20.0f));

        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            progressDialogFragment.dismiss();
            progressDialogFragment = null;
        }

    }

    @Override
    public void addressFetched(Map<String, Object> addressMap) {

        dismissProgressDialog();

        final LatLng latLng = (LatLng) addressMap.get("latLng");

        float lat = new BigDecimal(latLng.latitude)
                .setScale(1, RoundingMode.HALF_UP).floatValue();

        float lng = new BigDecimal(latLng.longitude)
                .setScale(1, RoundingMode.HALF_UP).floatValue();


        deliveryLocationCoordinatesTv.setText("[" + lat + " , " + lng + "]");
        chosenAddress = (String) addressMap.get("fullAddress");

        deliveryLocationAddressTv.setText(chosenAddress);

        deliveryLocationPlaceNameTv.setVisibility(View.VISIBLE);
        deliveryLocationCoordinatesTv.setVisibility(View.VISIBLE);
        deliveryLocationAddressTv.setVisibility(View.VISIBLE);

        String placeName;
        if (addressMap.containsKey("addressMap")) {
            placeName = (String) addressMap.get("addressMap");
        } else {
            placeName = "Unknown location";
        }
        deliveryLocationPlaceNameTv.setText(placeName);

    }

    @Override
    public void addressFetchFailed(String errorMessage) {

        Toast.makeText(this,
                "Failed while trying to fetch address! Please try again",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void locationFetched(LatLng latLng) {

        markLocation(latLng);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationRequester != null) {
            locationRequester.resumeLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationRequester != null) {
            locationRequester.stopLocationUpdates();
        }
    }

    private void dismissProgressDialog() {

        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            progressDialogFragment.dismiss();

        }
    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof Integer) {

            switch ((int) arg) {

                case DeliveryModel.DELIVERY_DRIVERS_NOTIFIED:

                    dismissProgressDialog();
                    finish();
//                    deliveryAcceptanceListener = model.listenForDriverDeliveryAcceptance();

                    break;

                case DeliveryModel.DELIVERY_DRIVER_ACCEPTED_DELIVERY:


                    break;

                case DeliveryModel.DRIVER_DELIVERY_REQUEST_ACCEPTED:


//                        dismissProgressDialog();
//
//                        startActivity(new Intent(this, CustomerDeliveryMapActivity.class)
//                                .putExtra("delivery",currentDelivery));

                    break;

                case DeliveryModel.DELIVERY_STARTED:

                    dismissProgressDialog();

                    finish();

                    startActivity(new Intent(this, CustomerDeliveryMapActivity.class)
                            .putExtra("delivery", currentDelivery));

                    break;


            }

        } else if (arg instanceof Delivery.InProgressDelivery) {


        } else if (arg instanceof Map) {

            final Map<Integer, Object> resultMap = (Map<Integer, Object>) arg;

            final int key = resultMap.keySet().iterator().next();

            switch (key) {

                case DeliveryModel.DELIVERY_DRIVER_NOT_FOUND:

                    finish();
//                    Toast.makeText(this,
//                            "No Drivers Found currently!", Toast.LENGTH_SHORT).show();
//                    dismissProgressDialog();

                    break;

                case DeliveryModel.DRIVER_DELIVERY_REQUEST:

                    final String driverID = (String) resultMap.get(key);

                    dismissProgressDialog();

                    DeliveryDriverInfoFragment.newInstance(driverID,
                            new DeliveryDriverInfoFragment.DeliveryListener() {
                                @Override
                                public void startDelivery() {

                                    model.acceptDriverRequest();

//                            DriverDeliveryMapActivity

                                    Toast.makeText(DeliveryLocationMapActivity.this,
                                            "Delivery Started", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void cancelDelivery() {

                                    model.refuseDriverRequest();

                                    Toast.makeText(DeliveryLocationMapActivity.this,
                                            "Delivery Cancelled", Toast.LENGTH_SHORT).show();


                                }
                            }).show(getSupportFragmentManager(), "DeliveryDriverInfo");

                    break;

            }
        }

    }

    @Override
    protected void onDestroy() {


        if (deliveryAcceptanceListener != null) {
            deliveryAcceptanceListener.remove();
        }
        super.onDestroy();

    }
}