package com.developers.wajbaty.Customer.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.developers.wajbaty.Adapters.NearbyRestaurantsAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.MenuItem;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantActivity;
import com.developers.wajbaty.PartneredRestaurant.Fragments.RestaurantMenuFragment;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.GeocoderUtil;
import com.developers.wajbaty.Utils.LocationRequester;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.firebase.geofire.core.GeoHashQuery;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class NearbyRestaurantsFragment extends Fragment implements
        OnMapReadyCallback,
        NearbyRestaurantsAdapter.NearbyRestaurantsListener,
        LocationRequester.LocationRequestAction,
        GeocoderUtil.GeocoderResultListener,
        View.OnClickListener {

    //constants
    private static final double radius = 10 * 1000;
    private static final int NEARBY_PAGE_LIMIT = 10;
    private static final String ADDRESS_MAP = "addressMap";

    private Map<String, Object> addressMap;
    private GoogleMap mMap;

    //views
    private RecyclerView nearbyRestaurantsRv;
    private ImageView nearbyRestaurantsDirectionsIv;

    //restaurants adapter
    private NearbyRestaurantsAdapter nearbyAdapter;
    private ArrayList<PartneredRestaurant.NearbyPartneredRestaurant> nearbyRestaurants;
    private Query nearbyQuery;
    private LinearLayoutManager layoutManager;

    //current location
    private Location currentLocation;
    private LocationRequester locationRequester;
    private ProgressDialogFragment progressDialog;
    private Marker currentMapMarker;
    private Marker homeMapMarker;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    //nearby restaurants
    private boolean isLoadingItems;
    private DocumentSnapshot lastDocSnapshot;
    private ScrollListener scrollListener;


    //geolocation
    private List<GeoQueryBounds> geoQueryBounds;
    private GeoLocation center;

//    //directions
//    private List<Marker> markers;

//    private static final String ARG_PARAM1 = "param1",ARG_PARAM2 = "param2";
//    private String mParam1,mParam2;

    public NearbyRestaurantsFragment() {
        // Required empty public constructor
    }

    public static NearbyRestaurantsFragment newInstance(Map<String, Object> addressMap) {
        NearbyRestaurantsFragment fragment = new NearbyRestaurantsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ADDRESS_MAP, (Serializable) addressMap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            addressMap = (Map<String, Object>) getArguments().getSerializable(ADDRESS_MAP);
        }

        final LatLng latLng = (LatLng) addressMap.get("latLng");

        Log.d("ttt", "gotten latLng in nearby fragment: " + latLng.latitude + "-" + latLng.longitude);

        currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(latLng.latitude);
        currentLocation.setLongitude(latLng.longitude);


        nearbyRestaurants = new ArrayList<>();


        layoutManager = new LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false);

        nearbyAdapter = new NearbyRestaurantsAdapter(nearbyRestaurants,
                this, currentLocation, requireContext(), layoutManager);

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        initializeLocationRequester();
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.

                        Toast.makeText(requireContext(),
                                "You need to grant location access permission in order " +
                                        "to show nearby restaurants!", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_nearby_restaurants, container, false);

        nearbyRestaurantsRv = view.findViewById(R.id.nearbyRestaurantsRv);
        nearbyRestaurantsDirectionsIv = view.findViewById(R.id.nearbyRestaurantsDirectionsIv);
        nearbyRestaurantsRv.setLayoutManager(layoutManager);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.nearbyMapFragment);

        mapFragment.getMapAsync(this);


        nearbyRestaurantsRv.setAdapter(nearbyAdapter);

        nearbyRestaurantsDirectionsIv.setOnClickListener(this);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        showProgressDialog();

        fetchNearbyRestaurants((String) addressMap.get("countryCode"));

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);


        if(currentLocation!=null){

            final LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

            homeMapMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng)
                    .title("My Location"));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20.0f));

        }
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(requireContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION)
//                    == PackageManager.PERMISSION_GRANTED) {
//                buildGoogleApiClient();
//                mMap.setMyLocationEnabled(true);
//            }
//        } else {
//            buildGoogleApiClient();
//            mMap.setMyLocationEnabled(true);
//        }



//        requestCurrentLocation();

    }

    @Override
    public void selectRestaurant(int position) {

//        nearbyAdapter.notifyItemChanged(position);
//        final LatLng latLng = new LatLng(nearbyRestaurants.get(position).getLat(),
//                nearbyRestaurants.get(position).getLng());
        if(nearbyRestaurantsDirectionsIv.getVisibility() == View.GONE){
            nearbyRestaurantsDirectionsIv.setVisibility(View.VISIBLE);
        }

        addMarkerForRestaurant(position);

    }

    @Override
    public void reSelectRestaurant(int position) {

        final LatLng restaurantLatLng =
                new LatLng(nearbyRestaurants.get(position).getLat(),
                        nearbyRestaurants.get(position).getLng());

        if (!mMap.getCameraPosition().target.equals(restaurantLatLng)) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantLatLng, 20.0f));

        } else {

            startActivity(new Intent(requireContext(), RestaurantActivity.class)
                    .putExtra("ID", nearbyRestaurants.get(position).getID()));

        }

    }

    private void addMarkerForRestaurant(int position) {

        if (currentMapMarker != null)
            currentMapMarker.remove();

        final PartneredRestaurant.NearbyPartneredRestaurant nearbyRestaurant =
                nearbyRestaurants.get(position);


        final LatLng restaurantLatLng =
                new LatLng(nearbyRestaurant.getLat(), nearbyRestaurant.getLng());


        currentMapMarker = mMap.addMarker(new MarkerOptions().position(restaurantLatLng)
                .title(nearbyRestaurant.getName()));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantLatLng, 20.0f));

    }

    @Override
    public void locationFetched(LatLng latLng) {

        currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(latLng.latitude);
        currentLocation.setLongitude(latLng.longitude);

        NearbyRestaurantsAdapter.setCurrentLocation(currentLocation);

        GeocoderUtil.getLocationAddress(requireContext(), latLng, this);

    }


    private void requestCurrentLocation() {

        final String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(requireContext(), locationPermission)
                        == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            initializeLocationRequester();

        } else if (currentLocation == null) {
            requestPermissionLauncher.launch(locationPermission);
        }

    }

    private void initializeLocationRequester() {

        Log.d("ttt", "initializeLocationRequester");

//        showProgressDialog();
        locationRequester = new LocationRequester(requireActivity(), this);
        locationRequester.getCurrentLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationRequester != null) {
            locationRequester.resumeLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationRequester != null) {
            locationRequester.stopLocationUpdates();
        }
    }


    @Override
    public void addressFetched(Map<String, Object> addressMap) {

        final String countryCode = (String) addressMap.get("countryCode");
        fetchNearbyRestaurants(countryCode);

    }

    @Override
    public void addressFetchFailed(String errorMessage) {


    }

    private void fetchNearbyRestaurants(String countryCode) {

        nearbyQuery = FirebaseFirestore.getInstance().collection("PartneredRestaurant")
//                .whereEqualTo("countryCode", countryCode)
                .orderBy("geohash").limit(NEARBY_PAGE_LIMIT);

//        nearbyQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot snapshots) {
//
//                if(snapshots!=null && !snapshots.isEmpty()){
//                    Log.d("ttt","result: "+snapshots.size());
//
//                }else{
//                    Log.d("ttt","result is empty");
//
//                }
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d("ttt","result failed: "+e.getMessage());
//            }
//        });

        Log.d("ttt", "currentLocation.getLatitude: " + currentLocation.getLatitude());
        Log.d("ttt", "currentLocation.getLongitude: " + currentLocation.getLongitude());

        center = new GeoLocation(currentLocation.getLatitude(),
                currentLocation.getLongitude());

        geoQueryBounds = GeoFireUtils.getGeoHashQueryBounds(center, radius);

        getNearbyRestaurants(true);


//        Tasks.whenAllSuccess(tasks).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
//            @Override
//            public void onComplete(@NonNull Task<List<Object>> task) {
//
//                Log.d("ttt","tasks completeted");
//
//                for (Task<QuerySnapshot> driverTask : tasks) {
//
//                    if(driverTask.isSuccessful() && driverTask.getResult()!=null && !driverTask.getResult().isEmpty()){
//
//                        Log.d("ttt","driverTask is not empty");
//
//                        nearbyRestaurants.addAll(driverTask.getResult()
//                                .toObjects(PartneredRestaurant.NearbyPartneredRestaurant.class));
//                    }
//
//                }
//
//                nearbyAdapter.notifyDataSetChanged();
//
//                dismissProgressDialog();
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//                dismissProgressDialog();
//
//            }
//        });

    }

    private void getNearbyRestaurants(boolean isInitial) {

//        showProgressDialog();

        isLoadingItems = true;

        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (GeoQueryBounds b : geoQueryBounds) {
            Query query = nearbyQuery.startAt(b.startHash).endAt(b.endHash);

            if (lastDocSnapshot != null) {
                query = query.startAfter(lastDocSnapshot);
            }

            tasks.add(query.get());
        }


        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            final int previousSize = nearbyRestaurants.size();

            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                for (Task<QuerySnapshot> driverTask : tasks) {

                    if (driverTask.isSuccessful() && driverTask.getResult() != null && !driverTask.getResult().isEmpty()) {

                        Log.d("ttt", "driverTask is not empty");

                        if (isInitial) {
                            nearbyRestaurants.addAll(driverTask.getResult()
                                    .toObjects(PartneredRestaurant.NearbyPartneredRestaurant.class));
                        } else {
                            nearbyRestaurants.addAll(nearbyRestaurants.size(), driverTask.getResult()
                                    .toObjects(PartneredRestaurant.NearbyPartneredRestaurant.class));
                        }

                    }
                }

                final QuerySnapshot finalSnapshot = tasks.get(tasks.size() - 1).getResult();

                if (finalSnapshot != null && !finalSnapshot.isEmpty()) {
                    if (!finalSnapshot.getDocuments().isEmpty()) {
                        final DocumentSnapshot lastDoc = finalSnapshot.getDocuments().get(finalSnapshot.size() - 1);
                        if (lastDoc != null) {
                            lastDocSnapshot = lastDoc;
                        }
                    }
                }


                if (isInitial) {

                    if (!nearbyRestaurants.isEmpty()) {

                        nearbyRestaurantsRv.setVisibility(View.VISIBLE);
                        nearbyAdapter.notifyDataSetChanged();

                        if (nearbyRestaurants.size() == NEARBY_PAGE_LIMIT && scrollListener == null) {
                            nearbyRestaurantsRv.addOnScrollListener(scrollListener = new ScrollListener());
                        }

                    }

                } else {

                    if (!nearbyRestaurants.isEmpty()) {

                        nearbyAdapter.notifyItemRangeInserted(
                                nearbyRestaurants.size() - previousSize, previousSize);

                        if (task.getResult().size() < NEARBY_PAGE_LIMIT && scrollListener != null) {
                            nearbyRestaurantsRv.removeOnScrollListener(scrollListener);
                            scrollListener = null;
                        }
                    }

                }

                dismissProgressDialog();
                isLoadingItems = false;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                isLoadingItems = false;

                dismissProgressDialog();

                Log.d("ttt", "failed to get geo task: " + e.getMessage());
            }
        });

    }


    private void showProgressDialog() {

        Fragment oldFragment = getChildFragmentManager().findFragmentByTag("progress");

        if (oldFragment != null && progressDialog.isAdded()) {
            return;
        }
        if (progressDialog == null) {
            progressDialog = new ProgressDialogFragment();
        }

        progressDialog.show(getChildFragmentManager(), "progress");
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {

            FragmentManager manager = getChildFragmentManager();

            Fragment fragment = manager.findFragmentByTag("progress");

            if (fragment != null) {
                manager.beginTransaction().remove(fragment).commit();
            }

            progressDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == nearbyRestaurantsDirectionsIv.getId()){

            if(homeMapMarker!=null && currentMapMarker!=null){

            }

        }
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems && !recyclerView.canScrollHorizontally(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at end");

                getNearbyRestaurants(false);

            }
        }
    }


}