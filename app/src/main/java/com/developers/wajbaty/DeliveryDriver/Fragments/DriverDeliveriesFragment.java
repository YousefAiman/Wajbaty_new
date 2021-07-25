package com.developers.wajbaty.DeliveryDriver.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.developers.wajbaty.Activities.HomeActivity;
import com.developers.wajbaty.Adapters.DriverDeliveriesAdapter;
import com.developers.wajbaty.Customer.Fragments.HomeFragment;
import com.developers.wajbaty.DeliveryDriver.Activities.DeliveryInfoActivity;
import com.developers.wajbaty.DeliveryDriver.Activities.DriverDeliveryMapActivity;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.Models.MenuItem;
import com.developers.wajbaty.Models.RestaurantCategory;
import com.developers.wajbaty.PartneredRestaurant.Fragments.RestaurantMenuFragment;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.LocationListenerUtil;
import com.developers.wajbaty.Utils.LocationRequester;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DriverDeliveriesFragment extends Fragment implements
        CompoundButton.OnCheckedChangeListener,DriverDeliveriesAdapter.DriverDeliveriesListener,
        LocationListenerUtil.LocationChangeObserver{

    private static final String ADDRESS_MAP = "addressMap";

    private static final int PENDING_DELIVERIES_LIMIT = 10;

    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10,
            MIN_UPDATE_DISTANCE = 10;

    private static final int RADIUS = 10 * 1000;

    private Map<String,Object> addressMap;
    private String currentUID;

    private CollectionReference deliveriesRef;

    //views
    private SwitchMaterial driverDeliveriesWorkingSwitch;
    private RecyclerView driverDeliveriesRv;
    private View currentDeliveryLayout;
    private ProgressBar driverDeliveriesProgressBar;

    //adapter
    private DriverDeliveriesAdapter deliveriesAdapter;
    private ArrayList<Delivery> deliveries;
    private Query pendingDeliveriesQuery;
    private boolean isLoadingDeliveries;
    private DocumentSnapshot lastDeliverySnapshot;
    private DeliveriesScrollListener deliveriesScrollListener;

    //location
    private List<GeoQueryBounds> geoQueryBounds;


    //delivery listener
    private String currentDeliveryID;
    private List<ListenerRegistration> snapShotListeners;

    //location
    private Location currentLocation;

    public DriverDeliveriesFragment() {
    }

    public static DriverDeliveriesFragment newInstance(Map<String, Object> addressMap) {
        DriverDeliveriesFragment fragment = new DriverDeliveriesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ADDRESS_MAP, (Serializable) addressMap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            addressMap = (HashMap<String, Object>) getArguments().getSerializable(ADDRESS_MAP);
        }

        deliveries = new ArrayList<>();
        deliveriesAdapter = new DriverDeliveriesAdapter(deliveries,this);

        currentUID = FirebaseAuth.getInstance().getUid();

        snapShotListeners = new ArrayList<>();

        deliveriesRef =  FirebaseFirestore.getInstance().collection("Deliveries");

        final LatLng latLng = (LatLng) addressMap.get("latLng");

        final GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);

        geoQueryBounds = GeoFireUtils.getGeoHashQueryBounds(center, RADIUS);

        pendingDeliveriesQuery =
                deliveriesRef.whereEqualTo("status",Delivery.STATUS_PENDING)
                        .orderBy("geohash")
                        .orderBy("orderTimeInMillis", Query.Direction.DESCENDING)
                        .limit(PENDING_DELIVERIES_LIMIT);

        checkAndRequestPermissions();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_driver_delivries, container, false);

        final NestedScrollView driverDeliveriesNSV = view.findViewById(R.id.driverDeliveriesNSV);
        driverDeliveriesNSV.setNestedScrollingEnabled(false);

        driverDeliveriesWorkingSwitch = view.findViewById(R.id.driverDeliveriesWorkingSwitch);
        driverDeliveriesRv = view.findViewById(R.id.driverDeliveriesRv);
        currentDeliveryLayout = view.findViewById(R.id.currentDeliveryLayout);
        driverDeliveriesProgressBar = view.findViewById(R.id.driverDeliveriesProgressBar);

        driverDeliveriesWorkingSwitch.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        driverDeliveriesRv.setAdapter(deliveriesAdapter);

        checkAndFetchCurrentDelivery();

        listenToNewDeliveryRequests();

    }

    private void checkAndFetchCurrentDelivery(){

//        final List<Integer> activeStatuses = new ArrayList<>();
//        activeStatuses.add(Delivery.STATUS_ACCEPTED);
//        activeStatuses.add(Delivery.STATUS_PICKED_UP);

        FirebaseFirestore.getInstance().collection("Users").document(currentUID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){

                    if(documentSnapshot.contains("currentDeliveryId")){

                         String currentDeliveryId = documentSnapshot.getString("currentDeliveryId");

                        if(currentDeliveryId!=null && !currentDeliveryId.isEmpty()){

                             boolean[] isInitial = {true};

                             int index = snapShotListeners.isEmpty()?0:snapShotListeners.size()-1;

                            snapShotListeners.add(deliveriesRef.document(currentDeliveryId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                                            if(value!=null){

                                                if(isInitial[0]){

                                                    isInitial[0] = false;

                                                    currentDeliveryID = value.getId();
                                                    showCurrentDelivery(value.toObject(Delivery.class));

                                                    getPendingDeliveries(true);

                                                }else{

                                                    if(value.contains("status")){
                                                        long status = value.getLong("status");
                                                        if(status == Delivery.STATUS_DELIVERED){

                                                            currentDeliveryID = null;
                                                            currentDeliveryLayout.setVisibility(View.GONE);
                                                            snapShotListeners.get(index).remove();
                                                            snapShotListeners.remove(index);

                                                        }
                                                    }

                                                }
                                            }else{
                                                getPendingDeliveries(true);
                                            }


                                        }
                                    })
                            );

//                            deliveriesRef.document(currentDeliveryId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                @Override
//                                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                    if(documentSnapshot.exists()){
//                                        currentDeliveryID = documentSnapshot.getId();
//                                        showCurrentDelivery(documentSnapshot.toObject(Delivery.class));
//
//                                    }
//                                }
//                            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                                    getPendingDeliveries(true);
//
//                                }
//                            });

                        }else{
                            getPendingDeliveries(true);
                        }
                    }

                }

            }
        });
    }

    private void getPendingDeliveries(boolean isInitial){

        isLoadingDeliveries = true;

        driverDeliveriesProgressBar.setVisibility(View.VISIBLE);


        Query currentQuery = pendingDeliveriesQuery;

        if (lastDeliverySnapshot != null) {
            currentQuery = currentQuery.startAfter(lastDeliverySnapshot);
        }

        final List<Task<QuerySnapshot>> pendingDeliveriesTasks = new ArrayList<>();

        final AtomicInteger previousSize  = new AtomicInteger(deliveries.size());

        for (GeoQueryBounds b : geoQueryBounds) {



            snapShotListeners.add(currentQuery.startAt(b.startHash).endAt(b.endHash)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        boolean initialSnapshots = true;
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            if(value!=null && !value.getDocuments().isEmpty()){
                                if(initialSnapshots){

                                    if (isInitial) {

                                        for(DocumentSnapshot snapshot:value.getDocuments()){
                                            deliveries.add(snapshot.toObject(Delivery.class));
                                        }

                                        if (!deliveries.isEmpty()) {

                                            deliveriesAdapter.notifyDataSetChanged();
                                            if (deliveries.size() == PENDING_DELIVERIES_LIMIT && deliveriesScrollListener == null) {
                                                driverDeliveriesRv.addOnScrollListener(deliveriesScrollListener = new DeliveriesScrollListener());
                                            }

                                        }
                                    } else {

                                        for(DocumentSnapshot snapshot:value.getDocuments()){
                                            deliveries.add(deliveries.size(),snapshot.toObject(Delivery.class));
                                        }

                                        if (!deliveries.isEmpty()) {

                                            deliveriesAdapter.notifyItemRangeInserted(
                                                    deliveries.size() - previousSize.get(),previousSize.get());

                                            if (value.getDocuments().size() < PENDING_DELIVERIES_LIMIT && deliveriesScrollListener != null) {
                                                driverDeliveriesRv.removeOnScrollListener(deliveriesScrollListener);
                                                deliveriesScrollListener = null;
                                            }

                                        }else{
                                            driverDeliveriesRv.removeOnScrollListener(deliveriesScrollListener);
                                            deliveriesScrollListener = null;

                                        }
                                    }

//                                    if(isInitial){
//
//                                        for(DocumentSnapshot snapshot:value.getDocuments()){
//                                            deliveries.add(snapshot.toObject(Delivery.class));
//                                        }
//
//                                        deliveriesAdapter.notifyDataSetChanged();
//
//                                    }else{
//
//
//
//
//                                    }

                                    initialSnapshots = false;
                                    isLoadingDeliveries = false;
                                    driverDeliveriesProgressBar.setVisibility(View.GONE);
                                }else{

                                    for(DocumentChange dc:value.getDocumentChanges()){

                                        DocumentSnapshot snapshot = dc.getDocument();
                                        switch (dc.getType()){

                                            case REMOVED:

                                                findAndRemoveDelivery(snapshot.getId());

                                                break;

                                            case MODIFIED:

                                                if(snapshot.contains("status")){

                                                    long status = snapshot.getLong("status");

                                                    if(status != Delivery.STATUS_PENDING) {

                                                        if(snapshot.contains("driverID")){

                                                            final String driverId=  snapshot.getString("driverID");

                                                            if(driverId.equals(currentUID)){
                                                                showCurrentDelivery(snapshot.toObject(Delivery.class));
                                                            }else{
                                                                findAndRemoveDelivery(snapshot.getId());
                                                            }
                                                        }else{
                                                            findAndRemoveDelivery(snapshot.getId());
                                                        }
                                                    }

                                                }

                                                break;

                                        }
                                    }

                                }
                            }

                        }
                    }));

//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot snapshots) {
//
//                            if(!snapshots.isEmpty()){
//                                deliveries.addAll(snapshots.toObjects(Delivery.class));
//                            }
//
//                        }
//                    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                    if(!deliveries.isEmpty()){
//
//                        deliveriesAdapter.notifyDataSetChanged();
//                    }
//
//                }
//            })

//            pendingDeliveriesTasks.add(
//                    currentQuery.startAt(b.startHash).endAt(b.endHash).get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                @Override
//                public void onSuccess(QuerySnapshot snapshots) {
//
//                    if(!snapshots.isEmpty()){
//                        deliveries.addAll(snapshots.toObjects(Delivery.class));
//                    }
//
//                }
//            }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                    if(!deliveries.isEmpty()){
//
//                        deliveriesAdapter.notifyDataSetChanged();
//                    }
//
//                }
//            }));

        }

//        Tasks.whenAllComplete(pendingDeliveriesTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
//            @Override
//            public void onComplete(@NonNull Task<List<Task<?>>> task) {
//
//                for(Delivery delivery:deliveries){
//                    if(delivery.getID().equals(currentDeliveryID)){
//                        deliveries.remove(delivery);
//                        break;
//                    }
//                }
//
//                isLoadingDeliveries = false;
//
//               driverDeliveriesProgressBar.setVisibility(View.GONE);
//            }
//        });


    }


    private void listenToNewDeliveryRequests(){

        final Query query = deliveriesRef
                .whereEqualTo("status",Delivery.STATUS_PENDING)
                .orderBy("orderTimeInMillis", Query.Direction.DESCENDING)
                .whereGreaterThan("orderTimeInMillis",System.currentTimeMillis())
                .orderBy("geohash");

//        final List<Task<QuerySnapshot>> offerTasks = new ArrayList<>();

        for (GeoQueryBounds b : geoQueryBounds) {
            snapShotListeners.add(query.startAt(b.startHash).endAt(b.endHash).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    Log.d("ttt","new deliveriy added");

                    if(value!=null){

                        for(DocumentChange dc:value.getDocumentChanges()){

                            if(dc.getType() == DocumentChange.Type.ADDED){

                                final DocumentSnapshot snapshot = dc.getDocument();
//                                final String docID = snapshot.getId();
//
//                                int position = findDeliveryById(docID);
//
//                                if(position != -1){
//
//                                    if(snapshot.contains("status") &&
//                                            snapshot.getLong("status") == Delivery.STATUS_PENDING){
//
//                                        if(snapshot.contains("driverID")){
//
//                                            String driverID = snapshot.getString("driverID");
//
//                                            if(driverID.equals(currentUID)){
//
//                                                showCurrentDelivery(deliveries.get(position));
//
//                                                deliveries.remove(position);
//                                                deliveriesAdapter.notifyItemRemoved(position);
//
//
//                                            }
////                                            else if(position != -1){
////
////                                                deliveries.remove(position);
////                                                deliveriesAdapter.notifyItemRemoved(position);
////
////                                            }
//
//                                        }
//
//                                    }
//
//                                }else{
                                Log.d("ttt","new deliveriy added to list");
                                    deliveries.add(snapshot.toObject(Delivery.class));
                                    deliveriesAdapter.notifyItemInserted(deliveries.size());

//                                }

                            }
                            else if(dc.getType() == DocumentChange.Type.MODIFIED){

                                final DocumentSnapshot snapshot = dc.getDocument();

                                if(snapshot.contains("status")){

                                    long status = snapshot.getLong("status");

                                     if(status != Delivery.STATUS_PENDING) {

                                         if(snapshot.contains("driverID")){

                                             final String driverId=  snapshot.getString("driverID");

                                             if(driverId.equals(currentUID)){
                                                 showCurrentDelivery(snapshot.toObject(Delivery.class));
                                             }else{

                                                 findAndRemoveDelivery(snapshot.getId());

                                             }
                                         }else{

                                             findAndRemoveDelivery(snapshot.getId());

                                         }

                                     }

                                }
                            }else if(dc.getType() == DocumentChange.Type.REMOVED){

                                findAndRemoveDelivery(dc.getDocument().getId());

                            }

                        }

                    }
                }
            }));
        }

    }

    private void findAndRemoveDelivery(String docID){

        int position = findDeliveryById(docID);

        if(position!=-1){
            deliveries.remove(position);
            deliveriesAdapter.notifyItemRemoved(position);
        }

    }


    private void showCurrentDelivery(Delivery delivery){

        currentDeliveryID = delivery.getID();

        currentDeliveryLayout.setVisibility(View.VISIBLE);

        final ImageView currentDeliveryUserImageIv = currentDeliveryLayout.findViewById(R.id.currentDeliveryUserImageIv);
        final TextView currentDeliveryUserNameTv = currentDeliveryLayout.findViewById(R.id.currentDeliveryUserNameTv),
                currentDeliveryAddressTv = currentDeliveryLayout.findViewById(R.id.currentDeliveryAddressTv),
                currentDeliveryOrderTimeTv = currentDeliveryLayout.findViewById(R.id.currentDeliveryOrderTimeTv),
                currentDeliveryTotalPriceTv = currentDeliveryLayout.findViewById(R.id.currentDeliveryTotalPriceTv),
                currentDeliveryRestaurantCountTv = currentDeliveryLayout.findViewById(R.id.currentDeliveryRestaurantCountTv);

        final Button currentDeliveryShowItemsBtn = currentDeliveryLayout.findViewById(R.id.currentDeliveryShowItemsBtn)
        ,currentDeliveryShowMapBtn = currentDeliveryLayout.findViewById(R.id.currentDeliveryShowMapBtn);



        FirebaseFirestore.getInstance().collection("Users")
                .document(delivery.getID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){

                    Picasso.get().load(documentSnapshot.getString("imageURL")).fit().centerCrop().into(currentDeliveryUserImageIv);
                    currentDeliveryUserNameTv.setText(documentSnapshot.getString("username"));

                }


            }
        });

        currentDeliveryAddressTv.setText(delivery.getAddress());
        currentDeliveryOrderTimeTv.setText(TimeFormatter.formatTime(delivery.getOrderTimeInMillis()));
        currentDeliveryTotalPriceTv.setText(delivery.getTotalCost() + delivery.getCurrency());
        currentDeliveryRestaurantCountTv.setText(delivery.getRestaurantMenuItemsMap().size()+" Restaurants");

        currentDeliveryShowItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(requireContext(),DeliveryInfoActivity.class)
                        .putExtra("isForShow",true)
                .putExtra("delivery",delivery));

            }
        });

        currentDeliveryShowMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(requireContext(), DriverDeliveryMapActivity.class)
                .putExtra("delivery",delivery)
                .putExtra("currentLocation",currentLocation));

            }
        });

    }

    private int findDeliveryById(String docID) {

        for(int i=0;i<deliveries.size();i++){
            if(deliveries.get(i).getID().equals(docID)){
                return i;
            }
        }


        return -1;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }


    @Override
    public void onDestroy() {

        if(!snapShotListeners.isEmpty()){
            for(ListenerRegistration listenerRegistration:snapShotListeners){
                listenerRegistration.remove();
            }
        }

        super.onDestroy();
    }


    @Override
    public void onDeliveryClicked(int position) {

        if(currentDeliveryID == null){
            startActivity(new Intent(requireContext(),DeliveryInfoActivity.class)
                    .putExtra("delivery",deliveries.get(position)));
        }else{
            Toast.makeText(requireContext(),
                    "You can't start other deliveries until you end the current one!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void checkAndRequestPermissions(){

        @SuppressLint("InlinedApi") final String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };


        if (LocationRequester.areLocationPermissionsEnabled(requireContext())) {

            LocationListenerUtil.getInstance().addLocationChangeObserver(this);
            LocationListenerUtil.getInstance().startListening(requireActivity());

        } else {


            ActivityResultLauncher<String[]> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {

                        boolean allAccepted = true;

                        for (String permission : result.keySet()) {
                            if (!result.get(permission)) {
                                allAccepted = false;
                                break;
                            }
                        }

                        if (allAccepted) {

                            LocationListenerUtil.getInstance().addLocationChangeObserver(DriverDeliveriesFragment.this);
                            LocationListenerUtil.getInstance().startListening(requireActivity());

                        }
                    });

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                requestPermissionLauncher.launch(permissions);

            }else{

                requestPermissionLauncher.launch(new String[]{permissions[0],permissions[1]});

            }

        }

    }


    @Override
    public void notifyObservers(Location location) {
        currentLocation = location;


    }


    private class DeliveriesScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingDeliveries &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {


                getPendingDeliveries(false);

            }
        }
    }

}