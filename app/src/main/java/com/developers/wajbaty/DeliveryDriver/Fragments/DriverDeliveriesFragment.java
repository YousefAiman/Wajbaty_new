package com.developers.wajbaty.DeliveryDriver.Fragments;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.DriverDeliveriesAdapter;
import com.developers.wajbaty.DeliveryDriver.Activities.DeliveryInfoActivity;
import com.developers.wajbaty.DeliveryDriver.Activities.DriverDeliveryMapActivity;
import com.developers.wajbaty.Models.Delivery;
import com.developers.wajbaty.Models.DeliveryDriver;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Services.LocationService;
import com.developers.wajbaty.Utils.TimeFormatter;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
        CompoundButton.OnCheckedChangeListener, DriverDeliveriesAdapter.DriverDeliveriesListener,
//        LocationListenerUtil.LocationChangeObserver,
        LocationService.LocationChangeObserver {

    private static final String ADDRESS_MAP = "addressMap";

    private static final int PENDING_DELIVERIES_LIMIT = 10;

//    private static final int
//            REQUEST_CHECK_SETTINGS = 100,
//            REQUEST_LOCATION_PERMISSION = 10,
//            MIN_UPDATE_DISTANCE = 10;

    private static final int RADIUS = 10 * 1000;

    private Map<String, Object> addressMap;
    private String currentUID;

    private CollectionReference deliveriesRef;

    //views
    private SwitchMaterial driverDeliveriesWorkingSwitch;
    private RecyclerView driverDeliveriesRv;
    private View currentDeliveryLayout;
    private ProgressBar driverDeliveriesProgressBar;
    private TextView driverDeliveriesNotWorkingTv, driverDeliveriesWorkingTv;

    //adapter
    private DriverDeliveriesAdapter deliveriesAdapter;
    private ArrayList<Delivery> deliveries;
    private Query pendingDeliveriesQuery;
    private boolean isLoadingDeliveries;
    private DocumentSnapshot lastDeliverySnapshot;
    private DeliveriesScrollListener deliveriesScrollListener;

    //location
    private List<GeoQueryBounds> geoQueryBounds;
    private ServiceConnection serviceConnection;

    //delivery listener
    private String currentDeliveryID;
    private ListenerRegistration currentDeliverySnapshotListener;
    private List<ListenerRegistration> snapShotListeners;


    //location
    private Location currentLocation;
    private DocumentReference driverRef;

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
        deliveriesAdapter = new DriverDeliveriesAdapter(deliveries, this, requireContext());

        currentUID = FirebaseAuth.getInstance().getUid();
        driverRef = FirebaseFirestore.getInstance().collection("Users").document(currentUID);

        snapShotListeners = new ArrayList<>();

        deliveriesRef = FirebaseFirestore.getInstance().collection("Deliveries");

        final LatLng latLng = (LatLng) addressMap.get("latLng");

        Log.d("ttt", "latlng: " + latLng.latitude + "," + latLng.longitude);

        currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(latLng.latitude);
        currentLocation.setLongitude(latLng.longitude);


        final GeoLocation center = new GeoLocation(latLng.latitude, latLng.longitude);

        geoQueryBounds = GeoFireUtils.getGeoHashQueryBounds(center, RADIUS);

        pendingDeliveriesQuery =
                deliveriesRef.whereEqualTo("status", Delivery.STATUS_PENDING)
                        .orderBy("geohash")
                        .orderBy("orderTimeInMillis", Query.Direction.DESCENDING)
                        .limit(PENDING_DELIVERIES_LIMIT);

//        checkAndRequestPermissions();

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
        driverDeliveriesNotWorkingTv = view.findViewById(R.id.driverDeliveriesNotWorkingTv);
        driverDeliveriesWorkingTv = view.findViewById(R.id.driverDeliveriesWorkingTv);

        driverDeliveriesWorkingSwitch.setOnCheckedChangeListener(this);

        final AdView adView = view.findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

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

        listenToDriverChanges();

    }

    private void listenToDriverChanges() {

//        final List<Integer> activeStatuses = new ArrayList<>();
//        activeStatuses.add(Delivery.STATUS_ACCEPTED);
//        activeStatuses.add(Delivery.STATUS_PICKED_UP);

        driverRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            boolean isInitial = true;
            private long currentStatus;

            @Override
            public void onEvent(@Nullable DocumentSnapshot value,
                                @Nullable FirebaseFirestoreException error) {

                if (value != null) {

                    if (value.contains("currentDeliveryID")) {

                        String currentDeliveryId = value.getString("currentDeliveryID");

                        if (isInitial) {

                            if (currentDeliveryId != null) {
                                currentDeliveryID = currentDeliveryId;
                                listenToCurrentDelivery(currentDeliveryId, true);
                            } else {
                                getPendingDeliveries(true);
                                listenToNewDeliveryRequests();
                            }

                            if (value.contains("status")) {
                                currentStatus = value.getLong("status");
                                if (currentStatus == DeliveryDriver.STATUS_AVAILABLE) {
                                    driverDeliveriesWorkingSwitch.setChecked(true);
                                } else if (currentStatus == DeliveryDriver.STATUS_UNAVAILABLE) {
                                    driverDeliveriesNotWorkingTv.setVisibility(View.VISIBLE);
                                }
                            }

                        } else {

                            if (currentDeliveryId != null && !currentDeliveryId.isEmpty()) {

                                if (currentDeliveryID == null || !currentDeliveryID.equals(currentDeliveryId)) {
                                    if (currentDeliveryID != null && currentDeliverySnapshotListener != null) {
                                        currentDeliverySnapshotListener.remove();
                                    }

                                    currentDeliveryID = currentDeliveryId;
                                    listenToCurrentDelivery(currentDeliveryID, false);


                                    stopListeningToNewDeliveries();
                                }
                            } else if (currentDeliveryID != null) {

                                driverDeliveriesWorkingTv.setVisibility(View.VISIBLE);
                                driverDeliveriesWorkingSwitch.setVisibility(View.VISIBLE);

                                currentDeliveryID = null;
                                currentDeliveryLayout.setVisibility(View.GONE);

                                if (currentDeliverySnapshotListener != null) {
                                    currentDeliverySnapshotListener.remove();
                                }

                                getPendingDeliveries(true);
                                listenToNewDeliveryRequests();
                            }
                        }
                    } else {

                        driverDeliveriesWorkingTv.setVisibility(View.VISIBLE);
                        driverDeliveriesWorkingSwitch.setVisibility(View.VISIBLE);

                        currentDeliveryID = null;
                        currentDeliveryLayout.setVisibility(View.GONE);
                        if (currentDeliverySnapshotListener != null) {

                            currentDeliverySnapshotListener.remove();
                        }


                    }

                    if (isInitial) {

                        Log.d("ttt", "getPendingDeliveries 2");
//                                getPendingDeliveries(true);

                    }

                    if (!isInitial && value.contains("status")) {
                        long status = value.getLong("status");

                        if (status == DeliveryDriver.STATUS_AVAILABLE && currentStatus != DeliveryDriver.STATUS_AVAILABLE) {

                            bindToLocationService();
                            startListeningToNewDeliveries();

                        } else if (status == DeliveryDriver.STATUS_UNAVAILABLE && currentStatus != DeliveryDriver.STATUS_UNAVAILABLE) {

                            unBindService();
                            stopListeningToNewDeliveries();

                        }
                        currentStatus = status;
                    }

//
//                            if(isInitial){
//
//                                isInitial = false;
//
//                            }else{
//
//
//                            }
//
//                            if(value.contains("currentDeliveryId")){
//
//                                String currentDeliveryId = value.getString("currentDeliveryId");
//
//                                if(currentDeliveryId!=null && !currentDeliveryId.isEmpty()){
//
//                                    listenToCurrentDelivery(currentDeliveryId);
////                            deliveriesRef.document(currentDeliveryId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
////                                @Override
////                                public void onSuccess(DocumentSnapshot documentSnapshot) {
////                                    if(documentSnapshot.exists()){
////                                        currentDeliveryID = documentSnapshot.getId();
////                                        showCurrentDelivery(documentSnapshot.toObject(Delivery.class));
////
////                                    }
////                                }
////                            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
////                                @Override
////                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
////
////                                    getPendingDeliveries(true);
////
////                                }
////                            });
//
//                                }else{
//
//                                    currentDeliveryID = null;
//                                    currentDeliveryLayout.setVisibility(View.GONE);
//
//                                    getPendingDeliveries(true);
//                                }
//                            }else{
//                                currentDeliveryID = null;
//                                currentDeliveryLayout.setVisibility(View.GONE);
//                            }

                } else if (isInitial) {
                    Log.d("ttt", "getPendingDeliveries 3");
                    getPendingDeliveries(true);

                }

                isInitial = false;

            }
        });
    }


    private void listenToCurrentDelivery(String currentDeliveryId, boolean isInitial) {

//        int index = snapShotListeners.isEmpty()?0:snapShotListeners.size()-1;

        currentDeliverySnapshotListener = deliveriesRef.document(currentDeliveryId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    boolean deliverySnapshotIsInitial = true;

                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (value != null) {

                            if (deliverySnapshotIsInitial) {

                                deliverySnapshotIsInitial = false;

                                currentDeliveryID = value.getId();
                                Delivery delivery = value.toObject(Delivery.class);

                                if (delivery != null) {
                                    showCurrentDelivery(delivery);
                                }

                                if (isInitial) {

                                    if (!isDeliveryMapActivityRunning() && isAdded()) {
                                        startActivity(new Intent(requireContext(), DriverDeliveryMapActivity.class)
                                                .putExtra("delivery", delivery)
                                                .putExtra("currentLocation", currentLocation));
                                    }


                                    Log.d("ttt", "getPendingDeliveries 4");
//                                    getPendingDeliveries(true);
                                }

                            } else {

                                if (value.contains("status")) {
                                    long status = value.getLong("status");
                                    if (status == Delivery.STATUS_DELIVERED) {

                                        driverDeliveriesWorkingTv.setVisibility(View.VISIBLE);
                                        driverDeliveriesWorkingSwitch.setVisibility(View.VISIBLE);

                                        currentDeliveryID = null;
                                        currentDeliveryLayout.setVisibility(View.GONE);
                                        if (currentDeliverySnapshotListener != null) {

                                            currentDeliverySnapshotListener.remove();
                                        }

//                                        currentDeliverySnapshotListener.remove();
//                                        currentDeliverySnapshotListener = null;

                                    }
                                }

                            }
                        } else if (isInitial) {
                            Log.d("ttt", "getPendingDeliveries 5");
//                            getPendingDeliveries(true);
                        }


                    }
                });

    }

    private void getPendingDeliveries(boolean isInitial) {

        isLoadingDeliveries = true;

        driverDeliveriesProgressBar.setVisibility(View.VISIBLE);


        Query currentQuery = pendingDeliveriesQuery;

        if (lastDeliverySnapshot != null) {
            currentQuery = currentQuery.startAfter(lastDeliverySnapshot);
        }

//        final List<Task<QuerySnapshot>> pendingDeliveriesTasks = new ArrayList<>();

        final AtomicInteger previousSize = new AtomicInteger(deliveries.size());

        for (GeoQueryBounds b : geoQueryBounds) {

            snapShotListeners.add(currentQuery.startAt(b.startHash).endAt(b.endHash)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        boolean initialSnapshots = true;

                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            if (value != null && !value.getDocuments().isEmpty()) {
                                if (initialSnapshots) {

                                    if (isInitial) {

                                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                                            deliveries.add(snapshot.toObject(Delivery.class));
                                        }

                                        if (!deliveries.isEmpty()) {

                                            deliveriesAdapter.notifyDataSetChanged();
                                            if (deliveries.size() == PENDING_DELIVERIES_LIMIT && deliveriesScrollListener == null) {
                                                driverDeliveriesRv.addOnScrollListener(deliveriesScrollListener = new DeliveriesScrollListener());
                                            }

                                        }
                                    } else {

                                        for (DocumentSnapshot snapshot : value.getDocuments()) {
                                            deliveries.add(deliveries.size(), snapshot.toObject(Delivery.class));
                                        }

                                        if (!deliveries.isEmpty()) {

                                            deliveriesAdapter.notifyItemRangeInserted(
                                                    deliveries.size() - previousSize.get(), previousSize.get());

                                            if (value.getDocuments().size() < PENDING_DELIVERIES_LIMIT && deliveriesScrollListener != null) {
                                                driverDeliveriesRv.removeOnScrollListener(deliveriesScrollListener);
                                                deliveriesScrollListener = null;
                                            }

                                        } else {
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
                                } else {

                                    for (DocumentChange dc : value.getDocumentChanges()) {

                                        DocumentSnapshot snapshot = dc.getDocument();
                                        switch (dc.getType()) {

                                            case REMOVED:

                                                findAndRemoveDelivery(snapshot.getId());

                                                break;

                                            case MODIFIED:

                                                if (snapshot.contains("status")) {

                                                    long status = snapshot.getLong("status");

                                                    if (status != Delivery.STATUS_PENDING) {

                                                        if (snapshot.contains("driverID")) {

                                                            final String driverId = snapshot.getString("driverID");

                                                            if (driverId.equals(currentUID)) {
                                                                showCurrentDelivery(snapshot.toObject(Delivery.class));
                                                            } else {
                                                                findAndRemoveDelivery(snapshot.getId());
                                                            }
                                                        } else {
                                                            findAndRemoveDelivery(snapshot.getId());
                                                        }
                                                    }

                                                }

                                                break;

                                        }
                                    }

                                }
                            } else {
                                driverDeliveriesProgressBar.setVisibility(View.GONE);

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


    private void listenToNewDeliveryRequests() {

        final Query query = deliveriesRef
                .whereEqualTo("status", Delivery.STATUS_PENDING)
                .orderBy("orderTimeInMillis", Query.Direction.DESCENDING)
                .whereGreaterThan("orderTimeInMillis", System.currentTimeMillis())
                .orderBy("geohash");

//        final List<Task<QuerySnapshot>> offerTasks = new ArrayList<>();

        for (GeoQueryBounds b : geoQueryBounds) {
            snapShotListeners.add(query.startAt(b.startHash).endAt(b.endHash)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value,
                                            @Nullable FirebaseFirestoreException error) {

                            Log.d("ttt", "new deliveriy added");

                            if (value != null) {

                                for (DocumentChange dc : value.getDocumentChanges()) {

                                    if (dc.getType() == DocumentChange.Type.ADDED) {

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
                                        Log.d("ttt", "new deliveriy added to list");
                                        deliveries.add(snapshot.toObject(Delivery.class));
                                        deliveriesAdapter.notifyItemInserted(deliveries.size());

//                                }

                                    } else if (dc.getType() == DocumentChange.Type.MODIFIED) {

                                        final DocumentSnapshot snapshot = dc.getDocument();

                                        if (snapshot.contains("status")) {

                                            long status = snapshot.getLong("status");

                                            if (status != Delivery.STATUS_PENDING) {
                                                findAndRemoveDelivery(snapshot.getId());
//                                         if(snapshot.contains("driverID")){
//
//                                             final String driverId=  snapshot.getString("driverID");
//
////                                             if(driverId.equals(currentUID)){
////                                                 showCurrentDelivery(snapshot.toObject(Delivery.class));
////                                             }else{
//
//                                                 findAndRemoveDelivery(snapshot.getId());
//
////                                             }
//                                         }else{
//
//                                             findAndRemoveDelivery(snapshot.getId());
//
//                                         }

                                            }

                                        }
                                    } else if (dc.getType() == DocumentChange.Type.REMOVED) {

                                        findAndRemoveDelivery(dc.getDocument().getId());

                                    }

                                }

                            } else {
                                driverDeliveriesProgressBar.setVisibility(View.GONE);
                            }
                        }
                    }));
        }

    }

    private void findAndRemoveDelivery(String docID) {

        int position = findDeliveryById(docID);

        if (position != -1) {
            deliveries.remove(position);
            deliveriesAdapter.notifyItemRemoved(position);
        }

    }


    private void showCurrentDelivery(Delivery delivery) {

        driverDeliveriesWorkingTv.setVisibility(View.GONE);
        driverDeliveriesWorkingSwitch.setVisibility(View.GONE);

        currentDeliveryID = delivery.getID();

        currentDeliveryLayout.setVisibility(View.VISIBLE);

        final ImageView currentDeliveryUserImageIv = currentDeliveryLayout.findViewById(R.id.currentDeliveryUserImageIv);
        final TextView currentDeliveryUserNameTv = currentDeliveryLayout.findViewById(R.id.currentDeliveryUserNameTv),
                currentDeliveryAddressTv = currentDeliveryLayout.findViewById(R.id.currentDeliveryAddressTv),
                currentDeliveryOrderTimeTv = currentDeliveryLayout.findViewById(R.id.currentDeliveryOrderTimeTv),
                currentDeliveryTotalPriceTv = currentDeliveryLayout.findViewById(R.id.customerDeliveryTotalPriceTv),
                currentDeliveryRestaurantCountTv = currentDeliveryLayout.findViewById(R.id.customerRestaurantCountTv);

        final Button currentDeliveryShowItemsBtn = currentDeliveryLayout.findViewById(R.id.currentDeliveryShowItemsBtn), currentDeliveryShowMapBtn = currentDeliveryLayout.findViewById(R.id.currentDeliveryShowMapBtn);


        FirebaseFirestore.getInstance().collection("Users")
                .document(delivery.getID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {

                    Picasso.get().load(documentSnapshot.getString("imageURL")).fit().centerCrop().into(currentDeliveryUserImageIv);
                    currentDeliveryUserNameTv.setText(documentSnapshot.getString("name"));

                }
            }
        });

        currentDeliveryAddressTv.setText(delivery.getAddress());
        currentDeliveryOrderTimeTv.setText(TimeFormatter.formatTime(delivery.getOrderTimeInMillis()));
        currentDeliveryTotalPriceTv.setText(delivery.getTotalCost() + delivery.getCurrency());
        currentDeliveryRestaurantCountTv.setText(delivery.getRestaurantCount() + " Restaurants");

        currentDeliveryShowItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(requireContext(), DeliveryInfoActivity.class)
                        .putExtra("isForShow", true)
                        .putExtra("delivery", delivery);

                startActivity(intent);

            }
        });

        currentDeliveryShowMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentLocation == null) {
                    Log.d("ttt", "currentLocation is null");

                } else {
                    Log.d("ttt", "currentLocation is not null");

                }

                startActivity(new Intent(requireContext(), DriverDeliveryMapActivity.class)
                        .putExtra("delivery", delivery)
                        .putExtra("currentLocation", currentLocation));

            }
        });

    }

    private int findDeliveryById(String docID) {

        for (int i = 0; i < deliveries.size(); i++) {
            if (deliveries.get(i).getID().equals(docID)) {
                return i;
            }
        }


        return -1;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            driverRef.update("status", DeliveryDriver.STATUS_AVAILABLE);
        } else {
            driverRef.update("status", DeliveryDriver.STATUS_UNAVAILABLE);

//            requireActivity().stopService(new Intent(requireContext(), LocationService.class));
        }

    }


    @Override
    public void onDestroy() {

        if (!snapShotListeners.isEmpty()) {
            for (ListenerRegistration listenerRegistration : snapShotListeners) {
                listenerRegistration.remove();
            }
        }

        if (currentDeliverySnapshotListener != null) {
            currentDeliverySnapshotListener.remove();
        }

        super.onDestroy();
    }


    @Override
    public void onDeliveryClicked(int position) {

        if (currentDeliveryID == null) {

            Intent intent = new Intent(requireContext(), DeliveryInfoActivity.class)
//                    .putExtra("isForShow",true)
                    .putExtra("delivery", deliveries.get(position));

            if (currentLocation != null) {
                intent.putExtra("currentLocation", currentLocation);
            }

            startActivity(intent);

        } else {
            Toast.makeText(requireContext(),
                    "You can't start other deliveries until you end the current one!",
                    Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void notifyObservers(Location location) {

        Log.d("ttt", "location in driver delivereis fragment: " + location.getLatitude()
                + "," + location.getLatitude());

        currentLocation = location;

        final GeoLocation center = new GeoLocation(location.getLatitude(), location.getLongitude());
        geoQueryBounds = GeoFireUtils.getGeoHashQueryBounds(center, RADIUS);

    }

    private void bindToLocationService() {

        if (!isAdded())
            return;

        Intent service = new Intent(requireContext(), LocationService.class);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                Log.d("ttt", "onServiceConnected");
                LocationService.LocationBinder locationBinder = (LocationService.LocationBinder) service;
                LocationService locationService = locationBinder.getService();
                locationService.addObserver(DriverDeliveriesFragment.this);

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("ttt", "onServiceDisconnected");
            }
        };

        requireContext().bindService(service, serviceConnection, 0);

    }

    private void unBindService() {
        if (serviceConnection != null) {
            if (getContext() != null) {
                getContext().unbindService(serviceConnection);
                serviceConnection = null;
            }
        }
    }

    private void startListeningToNewDeliveries() {

        driverDeliveriesNotWorkingTv.setVisibility(View.GONE);
        getPendingDeliveries(true);
        listenToNewDeliveryRequests();

    }

    private void stopListeningToNewDeliveries() {


        if (deliveries != null) {
            deliveries.clear();
            if (deliveriesAdapter != null) {
                deliveriesAdapter.notifyDataSetChanged();
            }
        }
        lastDeliverySnapshot = null;

        driverDeliveriesNotWorkingTv.setVisibility(View.VISIBLE);

        if (snapShotListeners != null && !snapShotListeners.isEmpty()) {
            for (ListenerRegistration listenerRegistration : snapShotListeners) {
                listenerRegistration.remove();
            }
        }
    }

    public boolean isDeliveryMapActivityRunning() {

        if (isAdded()) {

            ActivityManager activityManager =
                    (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> activitys = activityManager.getRunningTasks(Integer.MAX_VALUE);
            for (int i = 0; i < activitys.size(); i++) {
                if (activitys.get(i).topActivity.toString().equalsIgnoreCase("ComponentInfo{com.developers.wajbaty/com.developers.wajbaty.DeliveryDriver.Activities.DriverDeliveryMapActivity}")) {
                    return true;
                }
            }
        }

        return false;
    }


    private class DeliveriesScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingDeliveries &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "getPendingDeliveries 6");
                getPendingDeliveries(false);

            }
        }
    }

}