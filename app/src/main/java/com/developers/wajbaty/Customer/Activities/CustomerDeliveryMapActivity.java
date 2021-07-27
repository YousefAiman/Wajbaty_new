package com.developers.wajbaty.Customer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.developers.wajbaty.Activities.MessagingActivity;
import com.developers.wajbaty.Adapters.DeliveryCourseAdapter;
import com.developers.wajbaty.DeliveryDriver.Activities.DeliveryInfoActivity;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.Models.DeliveryCourse;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.MarkerAnimator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomerDeliveryMapActivity extends AppCompatActivity  implements OnMapReadyCallback,
        View.OnClickListener{

    private Delivery delivery;

    //driver location
    private Location currentDeliveryLocation;
    private Marker driverMarker;

    //map
    private GoogleMap map;

    //course
    private DeliveryCourseAdapter adapter;
    private ArrayList<DeliveryCourse> deliveryCourses;
    private ArrayList<DeliveryCourse> allDeliveryCourses;
    private HashMap<String,Marker> markerMap;

    //views
    private ImageButton driverDeliveryBackIB,driverDeliveryCurrentLocationIB,driverDeliveryMessageIB;
    private RecyclerView driverDeliveryCourseRv;
    private Button driverDeliveryItemsBtn,driverDeliveryConfirmBtn;
    private ImageView driverDeliveryCourseArrowIv;

    //firestore
    private DocumentReference deliveryRef;
    private ListenerRegistration snapshotListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_delivery_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driverDeliveryMap);

        if(mapFragment == null){
            finish();
            return;
        }

        mapFragment.getMapAsync(this);

        initializeObjects();

        getViews();

        setUpListeners();

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setCompassEnabled(false);

        listenToDeliveryLocation();
    }


    private void initializeObjects(){

        final Intent intent = getIntent();
        if(intent!=null && intent.hasExtra("delivery")){
            delivery = (Delivery) intent.getSerializableExtra("delivery");
        }else{
            finish();
            return;
        }

        deliveryRef = FirebaseFirestore.getInstance().collection("Deliveries")
                .document(delivery.getID());


        allDeliveryCourses = new ArrayList<>();
        deliveryCourses = new ArrayList<>();
        adapter = new DeliveryCourseAdapter(deliveryCourses);

    }


    private void getViews() {

        driverDeliveryBackIB = findViewById(R.id.driverDeliveryBackIB);
        driverDeliveryCurrentLocationIB = findViewById(R.id.driverDeliveryCurrentLocationIB);
        driverDeliveryMessageIB = findViewById(R.id.driverDeliveryMessageIB);
        driverDeliveryCourseRv = findViewById(R.id.driverDeliveryCourseRv);
        driverDeliveryCourseArrowIv = findViewById(R.id.driverDeliveryCourseArrowIv);
        driverDeliveryItemsBtn = findViewById(R.id.driverDeliveryItemsBtn);
        driverDeliveryConfirmBtn = findViewById(R.id.driverDeliveryConfirmBtn);

//        driverDeliveryCurrentLocationIB.setVisibility(View.GONE);
        disableConfirmButton();

        driverDeliveryCourseRv.setAdapter(adapter);
    }

    private void listenToDeliveryLocation(){

        snapshotListener =  deliveryRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            boolean isInitial = true;
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if(value!=null) {
                    if (value.contains("lat") && value.contains("lng")) {
                        final double lat = value.getDouble("lat"),
                                lng = value.getDouble("lng");

                        if (lat != 0 && lng != 0) {

                        if (isInitial) {

                                currentDeliveryLocation = new Location("currentDeliveryLocation");
                                currentDeliveryLocation.setLatitude(lat);
                                currentDeliveryLocation.setLatitude(lng);

                                LatLng latLng = new LatLng(lat,lng);
                           driverMarker = map.addMarker(new MarkerOptions().position(latLng).title("Your Delivery")
                                   .icon(bitmapDescriptorFromVector(CustomerDeliveryMapActivity.this)));


                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20.0f));

                            fetchCourse();
                            isInitial = false;
                        }else if(currentDeliveryLocation!=null){

                            if(lat!= currentDeliveryLocation.getLatitude() || lng!= currentDeliveryLocation.getLongitude()){

                                currentDeliveryLocation = new Location("currentDeliveryLocation");
                                currentDeliveryLocation.setLatitude(lat);
                                currentDeliveryLocation.setLatitude(lng);

                                if(driverMarker!=null){
                                    Log.d("ttt","delivery updated location: "+
                                            lat+","+lng);

                                    LatLng newPosition = new LatLng(lat,lng);

                                    MarkerAnimator.animateMarkerToICS(driverMarker,
                                            newPosition,
                                            new MarkerAnimator.LatLngInterpolator.Linear());

                                    map.animateCamera(CameraUpdateFactory.newLatLng(newPosition));

//                                    animateMarker(driverMarker,new LatLng(lat,lng),false);
                                }
                            }

                        }


                        }else{


                        }

                    } else {


                    }
                }

            }
        });

    }

    private void setUpListeners(){

        driverDeliveryBackIB.setOnClickListener(this);
        driverDeliveryItemsBtn.setOnClickListener(this);
        driverDeliveryMessageIB.setOnClickListener(this);
        driverDeliveryCourseArrowIv.setOnClickListener(this);
        driverDeliveryCurrentLocationIB.setOnClickListener(this);


        driverDeliveryConfirmBtn.setOnClickListener(this);

    }

    public static void main(String[] args){

    float lat = BigDecimal.valueOf(31.540959008656653)
                .setScale(2, RoundingMode.DOWN).floatValue();

        System.out.println(lat);
    }

    private void fetchCourse(){


        allDeliveryCourses.add(new DeliveryCourse(
                "Delivery Location",
                currentDeliveryLocation,
                0,
                true,
                false));

        List<Task<DocumentSnapshot>> restaurantTasks = new ArrayList<>();

        final CollectionReference restaurantRef =
                FirebaseFirestore.getInstance().collection("PartneredRestaurant");

        final boolean[] isFirst = {true};

        for(String restaurant:delivery.getRestaurantMenuItemsMap().keySet()){

            restaurantTasks.add(restaurantRef.document(restaurant).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            if(documentSnapshot.exists()){

                                double lat = documentSnapshot.getDouble("lat"),
                                        lng = documentSnapshot.getDouble("lng");

                                Log.d("ttt","course lat: "+lat+" , lng: "+lng);

                                final String name = documentSnapshot.getString("name");
                                Location restaurantLocation = new Location(name);
                                restaurantLocation.setLatitude(lat);
                                restaurantLocation.setLongitude(lng);

                                allDeliveryCourses.add(new DeliveryCourse(
                                        name,
                                        restaurantLocation,
                                        delivery.getRestaurantMenuItemsMap().get(restaurant),
                                        false,
                                        isFirst[0]));

                                addMarker(name,restaurantLocation);

                                isFirst[0] = false;
                                Log.d("ttt","restaurantLocation: "+restaurantLocation.getLatitude()
                                        +", "+restaurantLocation.getLongitude());
                            }

                        }
                    }));

        }

        Tasks.whenAllComplete(restaurantTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                Location deliveryLocation = new Location("deliveryLocation");
                deliveryLocation.setLatitude(delivery.getLat());
                deliveryLocation.setLongitude(delivery.getLng());

                allDeliveryCourses.add(
                        new DeliveryCourse(
                                "Delivery Location",
                                deliveryLocation,
                                0,
                                false,
                                false));

//                if(){
//
//
//
//                }

                addMarker("Delivery Location",deliveryLocation);

                deliveryCourses.addAll(allDeliveryCourses);

                adapter.notifyDataSetChanged();

            }
        });

    }

    private void addMarker(String name,Location location){

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        if(markerMap == null){
            markerMap = new HashMap<>();
        }

       markerMap.put(name,map.addMarker(new MarkerOptions().position(latLng).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));

    }

//    public void animateMarker(final Marker marker, final LatLng toPosition,
//                              final boolean hideMarker) {
//
//        final Handler handler = new Handler();
//        final long start = SystemClock.uptimeMillis();
//        Projection proj = map.getProjection();
//        Point startPoint = proj.toScreenLocation(marker.getPosition());
//        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
//        final long duration = 500;
//
//        final LinearInterpolator interpolator = new LinearInterpolator();
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                long elapsed = SystemClock.uptimeMillis() - start;
//                float t = interpolator.getInterpolation((float) elapsed / duration);
//                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
//                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
//                marker.setPosition(new LatLng(lat, lng));
//
//                if (t < 1.0) {
//                    // Post again 16ms later.
//                    handler.postDelayed(this, 16);
//                } else {
//                    marker.setVisible(!hideMarker);
//                }
//            }
//        });
//    }

    private void disableConfirmButton() {
        driverDeliveryConfirmBtn.setClickable(false);
        driverDeliveryConfirmBtn.setBackgroundResource(R.drawable.filled_button_inactive_background);
    }

    private void enableConfirmButton() {
        driverDeliveryConfirmBtn.setClickable(true);
        driverDeliveryConfirmBtn.setBackgroundResource(R.drawable.filled_button_background);
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == driverDeliveryBackIB.getId()){

            finish();

        }else if(v.getId() == driverDeliveryItemsBtn.getId()){

            startActivity(new Intent(this, DeliveryInfoActivity.class)
                    .putExtra("isForShow",true)
                    .putExtra("delivery",delivery));

        }
        else if(v.getId() == driverDeliveryCurrentLocationIB.getId()){

            zoomOnCurrentLocation();

        }
        else if(v.getId() == driverDeliveryConfirmBtn.getId()){



        }else if(v.getId() == driverDeliveryMessageIB.getId()){

            Intent messagingIntent = new Intent(this, MessagingActivity.class);
            messagingIntent.putExtra("messagingUserId", delivery.getDriverID());
            messagingIntent.putExtra("intendedDeliveryID", delivery.getID());
            startActivity(messagingIntent);

        }else if(v.getId() == driverDeliveryCourseArrowIv.getId()){

            if(driverDeliveryCourseArrowIv.getRotation() == 90){

                driverDeliveryCourseArrowIv.setRotation(-90);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        for(int i=0;i<deliveryCourses.size();i++){

                            if(!deliveryCourses.get(i).isWasPassed()){

                                Log.d("ttt","found active at: "+i);

//
//                                if(i+1 >= deliveryCourses.size()){
//
//
////                            deliveryCourses.remove();
//
//                                }else
                                    if(i-1 >= 0){

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



            }else{

             driverDeliveryCourseArrowIv.setRotation(90);
             deliveryCourses.clear();
             deliveryCourses.addAll(allDeliveryCourses);
             adapter.notifyDataSetChanged();

            }


        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (snapshotListener != null) {
            snapshotListener.remove();
        }
    }

    private void zoomOnCurrentLocation(){

        if(currentDeliveryLocation != null){
            LatLng currentLatLng =
                    new LatLng(currentDeliveryLocation.getLatitude(), currentDeliveryLocation.getLongitude());


            if(!map.getCameraPosition().target.equals(currentLatLng)){
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16.0f));
            }
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(
            Context context
    ) {
//        Drawable background = ContextCompat.getDrawable(context, R.drawable.scooter_marker_icon);
//        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.scooter_marker_icon);
//        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
//        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}