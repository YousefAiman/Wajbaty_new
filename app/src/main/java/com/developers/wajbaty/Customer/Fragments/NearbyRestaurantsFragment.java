package com.developers.wajbaty.Customer.Fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.NearbyRestaurantsAdapter;
import com.developers.wajbaty.Adapters.NearbySearchAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.Models.RestaurantSearchResult;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantActivity;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Services.LocationService;
import com.developers.wajbaty.Utils.GeocoderUtil;
import com.developers.wajbaty.Utils.LocationListenerUtil;
import com.developers.wajbaty.Utils.LocationRequester;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class NearbyRestaurantsFragment extends Fragment implements
        OnMapReadyCallback,
        NearbyRestaurantsAdapter.NearbyRestaurantsListener,
        LocationRequester.LocationRequestAction,
        GeocoderUtil.GeocoderResultListener,
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        NearbySearchAdapter.NearbySearchListener,
        LocationService.LocationChangeObserver {

    //constants
    private static final double radius = 10 * 1000;
    private static final int NEARBY_PAGE_LIMIT = 10;
    private static final String ADDRESS_MAP = "addressMap";

    private Map<String, Object> addressMap;
    private GoogleMap mMap;

    //views
    private RecyclerView nearbyRestaurantsRv, nearbyRestaurantsSearchRv;
    private ImageView nearbyRestaurantsDirectionsIv;
    private SearchView nearbyRestaurantsSv;
    private ImageButton nearbyCurrentLocationBtn;

    //restaurants adapter
    private NearbyRestaurantsAdapter nearbyAdapter;
    private ArrayList<PartneredRestaurant.NearbyPartneredRestaurant> nearbyRestaurants;
    private Query nearbyQuery;
    private LinearLayoutManager layoutManager;

    //restaurant search adapter
    private ArrayList<RestaurantSearchResult> searchResults;
    private NearbySearchAdapter searchAdapter;

    //current location
    private Location currentLocation;
    private LocationRequester locationRequester;
    private ProgressDialogFragment progressDialog;
    private Marker currentMapMarker;
    private Marker homeMapMarker;
//    private ActivityResultLauncher<String> requestPermissionLauncher;

    //nearby restaurants
    private boolean isLoadingItems;
    private DocumentSnapshot lastDocSnapshot;
    private ScrollListener scrollListener;


    //geolocation
    private List<GeoQueryBounds> geoQueryBounds;
    private GeoLocation center;
    private ServiceConnection serviceConnection;

    private Intent service;

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

        Log.d("ttt", "gotten latLng in nearby fragment: " + latLng.latitude + "-" +
                latLng.longitude);

//        currentLocation = new Location("currentLocation");
//        currentLocation.setLatitude(latLng.latitude);
//        currentLocation.setLongitude(latLng.longitude);
//

        nearbyRestaurants = new ArrayList<>();


        layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);

        nearbyAdapter = new NearbyRestaurantsAdapter(nearbyRestaurants,
                this, currentLocation, requireContext(), layoutManager);

        searchResults = new ArrayList<>();
        searchAdapter = new NearbySearchAdapter(searchResults, this, requireContext());
//        requestPermissionLauncher =
//                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                    if (isGranted) {
//                        initializeLocationRequester();
//                    } else {
//                        // Explain to the user that the feature is unavailable because the
//                        // features requires a permission that the user has denied. At the
//                        // same time, respect the user's decision. Don't link to system
//                        // settings in an effort to convince the user to change their
//                        // decision.
//
//                        Toast.makeText(requireContext(),
//                                "You need to grant location access permission in order " +
//                                        "to show nearby restaurants!", Toast.LENGTH_SHORT).show();
//
//                    }
//                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_nearby_restaurants, container, false);

        nearbyRestaurantsRv = view.findViewById(R.id.nearbyRestaurantsRv);
        nearbyRestaurantsDirectionsIv = view.findViewById(R.id.nearbyRestaurantsDirectionsIv);
        nearbyRestaurantsSv = view.findViewById(R.id.nearbyRestaurantsSv);
        nearbyCurrentLocationBtn = view.findViewById(R.id.nearbyCurrentLocationBtn);
        nearbyRestaurantsSearchRv = view.findViewById(R.id.nearbyRestaurantsSearchRv);

        nearbyRestaurantsSv.setOnQueryTextListener(this);
        nearbyRestaurantsSv.setOnSearchClickListener(this);
        nearbyRestaurantsSv.setOnClickListener(this);
        nearbyRestaurantsSv.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                hideSearchList();
                return false;
            }
        });

//        nearbyRestaurantsSv.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {
//                hideSearchList();
//            }
//        });

        nearbyRestaurantsSv.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideSearchList();
            }
        });

        nearbyCurrentLocationBtn.setOnClickListener(this);

        nearbyRestaurantsRv.setLayoutManager(layoutManager);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.nearbyMapFragment);

        mapFragment.getMapAsync(this);


        nearbyRestaurantsRv.setAdapter(nearbyAdapter);
        nearbyRestaurantsSearchRv.setAdapter(searchAdapter);

        nearbyRestaurantsDirectionsIv.setOnClickListener(this);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        showProgressDialog();


//        fetchNearbyRestaurants();

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            initializeLocationRequester();
            bindToLocationService();
        } else {
            ActivityResultLauncher<String> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                            granted -> {
                                if (granted) {
                                    mMap.setMyLocationEnabled(true);
                                    initializeLocationRequester();
                                    bindToLocationService();
                                }
                            });
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

//        if(currentLocation!=null){
//
////            final LatLng currentLatLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
////
////            homeMapMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng)
////                    .title("My Location"));
////
////            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20.0f));
//            zoomOnCurrentLocation();
//
//        }
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
//        if(nearbyRestaurantsDirectionsIv.getVisibility() == View.GONE){
//            nearbyRestaurantsDirectionsIv.setVisibility(View.VISIBLE);
//        }

        addMarkerForRestaurant(position);

    }

    @Override
    public void reSelectRestaurant(int position) {

        final LatLng restaurantLatLng = new LatLng(nearbyRestaurants.get(position).getLat(),
                nearbyRestaurants.get(position).getLng());

        if (mMap.getCameraPosition().target.latitude != restaurantLatLng.latitude
                || mMap.getCameraPosition().target.longitude != restaurantLatLng.longitude) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantLatLng, 20.0f));

        } else {

            startActivity(new Intent(requireContext(), RestaurantActivity.class)
                    .putExtra("ID", nearbyRestaurants.get(position).getID()));

        }

    }

    @Override
    public void getRestaurantDirections(int position) {

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

        if (currentLocation == null) {
            currentLocation = new Location("currentLocation");
            GeocoderUtil.getLocationAddress(requireContext(), latLng, this);
        }

        currentLocation.setLatitude(latLng.latitude);
        currentLocation.setLongitude(latLng.longitude);

        NearbyRestaurantsAdapter.setCurrentLocation(currentLocation);
        zoomOnCurrentLocation();

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
//            requestPermissionLauncher.launch(locationPermission);
        }

    }

    private void initializeLocationRequester() {

        Log.d("ttt", "initializeLocationRequester");

        locationRequester = new LocationRequester(requireActivity(), this);
        locationRequester.getCurrentLocation();


    }


    @Override
    public void addressFetched(Map<String, Object> addressMap) {

        fetchNearbyRestaurants((String) addressMap.get("countryCode"));

    }

    @Override
    public void addressFetchFailed(String errorMessage) {


    }

    private void fetchNearbyRestaurants(String countryCode) {

        nearbyQuery = FirebaseFirestore.getInstance().collection("PartneredRestaurant")
                .whereEqualTo("countryCode", countryCode)
                .orderBy("geohash").limit(NEARBY_PAGE_LIMIT);


        center = new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());

        geoQueryBounds = GeoFireUtils.getGeoHashQueryBounds(center, radius);

        getNearbyRestaurants(true);

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

        if (!tasks.isEmpty()) {

            Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                final int previousSize = nearbyRestaurants.size();

                @Override
                public void onComplete(@NonNull Task<List<Task<?>>> task) {

                    for (Task<QuerySnapshot> restaurantTask : tasks) {

                        if (restaurantTask.isSuccessful() && restaurantTask.getResult() != null &&
                                !restaurantTask.getResult().isEmpty()) {

                            Log.d("ttt", "driverTask is not empty");

                            if (isInitial) {
                                nearbyRestaurants.addAll(restaurantTask.getResult()
                                        .toObjects(PartneredRestaurant.NearbyPartneredRestaurant.class));
                            } else {
                                nearbyRestaurants.addAll(nearbyRestaurants.size(), restaurantTask.getResult()
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

                            if (nearbyRestaurants.size() >= NEARBY_PAGE_LIMIT && scrollListener == null) {
                                nearbyRestaurantsRv.addOnScrollListener(scrollListener = new ScrollListener());
                            }

                        }

                    } else {

                        if (!nearbyRestaurants.isEmpty()) {

                            nearbyAdapter.notifyItemRangeInserted(previousSize,
                                    nearbyRestaurants.size() - previousSize);

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
        } else {
            isLoadingItems = false;

            dismissProgressDialog();
        }
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

        if (v.getId() == nearbyRestaurantsDirectionsIv.getId()) {

//            if(currentMapMarker!=null){
//                if(locationRequester == null){
//
//                    initializeLocationRequester();
//
//                }else{
//                    locationRequester.getCurrentLocation();
//                }
//            }

        } else if (v.getId() == nearbyRestaurantsSv.getId()) {

            nearbyRestaurantsSv.onActionViewExpanded();

        } else if (v.getId() == nearbyCurrentLocationBtn.getId()) {

            zoomOnCurrentLocation();

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        showProgressDialog();


        if (!searchResults.isEmpty()) {
            searchResults.clear();
            searchAdapter.notifyDataSetChanged();
        }

        query = query.toLowerCase();


        for (int i = 0; i < nearbyRestaurants.size(); i++) {

            final PartneredRestaurant.NearbyPartneredRestaurant restaurant = nearbyRestaurants.get(i);

            if (restaurant.getName().equalsIgnoreCase(query) ||
                    restaurant.getName().toLowerCase().contains(query) ||
                    query.contains(restaurant.getName().toLowerCase())) {

                final Location location = new Location(restaurant.getID());
                location.setLatitude(restaurant.getLat());
                location.setLongitude(restaurant.getLng());

                double distance = location.distanceTo(currentLocation);
                String distanceFormatted;
                if (distance >= 1000) {
                    distanceFormatted = Math.round(distance / 1000) + "km ";
                } else {
                    distanceFormatted = Math.round(distance) + "m ";
                }

                RestaurantSearchResult searchResult = new RestaurantSearchResult(
                        restaurant.getID(),
                        restaurant.getMainImage(),
                        restaurant.getName(),
                        distanceFormatted,
                        restaurant.getGeohash(),
                        restaurant.getLat(),
                        restaurant.getLng()
                );

                searchResults.add(searchResult);
            }
        }

        searchForRestaurant(query);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void searchForRestaurant(String name) {

        final List<String> splitName = Arrays.asList(name.split(" "));

        Query query = FirebaseFirestore.getInstance().collection("PartneredRestaurant")
                .whereEqualTo("countryCode", addressMap.get("countryCode"))
                .limit(5);

        if (splitName.size() == 0) {

            query = query.whereArrayContains("keyWords", name);

        } else if (splitName.size() <= 10) {

            query = query.whereArrayContainsAny("keyWords", splitName);

        } else {

            query = query.whereArrayContainsAny("keyWords", splitName.subList(0, 10));

        }

        query = query.orderBy("geohash").limit(NEARBY_PAGE_LIMIT);


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

        final GeoLocation center = new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());

        final List<GeoQueryBounds> geoQueryBounds = GeoFireUtils.getGeoHashQueryBounds(center, radius);

        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (GeoQueryBounds b : geoQueryBounds) {
            Query boundQuery = query.startAt(b.startHash).endAt(b.endHash);
            tasks.add(boundQuery.get());
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {

                for (Task<QuerySnapshot> driverTask : tasks) {

                    if (driverTask.isSuccessful() && driverTask.getResult() != null && !driverTask.getResult().isEmpty()) {

                        Log.d("ttt", "driverTask is not empty");


//                        if(!searchResults.isEmpty()){
//                            searchResults.clear();
//                            searchAdapter.notifyDataSetChanged();
//                        }

                        outer:
                        for (DocumentSnapshot snapshot : driverTask.getResult()) {

                            if (!searchResults.isEmpty()) {
                                for (RestaurantSearchResult result : searchResults) {
                                    if (result.getRestaurantID().equals(snapshot.getId())) {
                                        continue outer;
                                    }
                                }
                            }

                            final double lat = snapshot.getDouble("lat"),
                                    lng = snapshot.getDouble("lng");

                            final Location location = new Location(snapshot.getId());
                            location.setLatitude(lat);
                            location.setLongitude(lng);

                            double distance = location.distanceTo(currentLocation);
                            String distanceFormatted;
                            if (distance >= 1000) {
                                distanceFormatted = Math.round(distance / 1000) + "km ";
                            } else {
                                distanceFormatted = Math.round(distance) + "m ";
                            }

                            RestaurantSearchResult searchResult = new RestaurantSearchResult(
                                    snapshot.getId(),
                                    snapshot.getString("mainImage"),
                                    snapshot.getString("name"),
                                    distanceFormatted,
                                    snapshot.getString("geohash"),
                                    snapshot.getDouble("lat"),
                                    snapshot.getDouble("lng")
                            );

                            searchResults.add(searchResult);
                        }

                        if (nearbyRestaurantsSearchRv.getVisibility() == View.GONE) {
                            nearbyRestaurantsSearchRv.setVisibility(View.VISIBLE);
                        }

//                         nearbyRestaurants.addAll(driverTask.getResult()
//                                    .toObjects(PartneredRestaurant.NearbyPartneredRestaurant.class));
//                        if (isInitial) {
//                            nearbyRestaurants.addAll(driverTask.getResult()
//                                    .toObjects(PartneredRestaurant.NearbyPartneredRestaurant.class));
//                        } else {
//                            nearbyRestaurants.addAll(nearbyRestaurants.size(), driverTask.getResult()
//                                    .toObjects(PartneredRestaurant.NearbyPartneredRestaurant.class));
//                        }

                    }
                }

                if (!searchResults.isEmpty()) {
                    if (nearbyRestaurantsSearchRv.getVisibility() == View.GONE) {
                        nearbyRestaurantsSearchRv.setVisibility(View.VISIBLE);
                    }
                    searchAdapter.notifyDataSetChanged();
                }

                dismissProgressDialog();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (!searchResults.isEmpty()) {
                    if (nearbyRestaurantsSearchRv.getVisibility() == View.GONE) {
                        nearbyRestaurantsSearchRv.setVisibility(View.VISIBLE);
                    }
                    searchAdapter.notifyDataSetChanged();
                }

                dismissProgressDialog();
                Log.d("ttt", "failed to get geo task: " + e.getMessage());
            }
        });


    }

    private void zoomOnCurrentLocation() {

        if (currentLocation != null) {

            LatLng currentLatLng =
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());


            if (!mMap.getCameraPosition().target.equals(currentLatLng)) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18.0f));
            }
        }
    }

    @Override
    public void onSearchResultClicked(int position) {

        hideSearchList();

        if (position < searchResults.size()) {
            RestaurantSearchResult searchResult = searchResults.get(position);
            for (int i = 0; i < nearbyRestaurants.size(); i++) {
                PartneredRestaurant.NearbyPartneredRestaurant restaurant = nearbyRestaurants.get(i);
                if (restaurant.getID().equals(searchResult.getRestaurantID())) {
                    selectRestaurant(i);
                    nearbyRestaurantsRv.scrollToPosition(i);
                    return;
                }
            }

            PartneredRestaurant.NearbyPartneredRestaurant nearbyPartneredRestaurant =
                    new PartneredRestaurant.NearbyPartneredRestaurant(
                            searchResult.getRestaurantID(),
                            searchResult.getRestaurantName(),
                            searchResult.getRestaurantImageURL(),
                            searchResult.getGeohash(),
                            searchResult.getLat(),
                            searchResult.getLng()
                    );

            nearbyRestaurants.add(nearbyPartneredRestaurant);
            nearbyAdapter.notifyItemInserted(nearbyRestaurants.size() - 1);
            nearbyRestaurantsRv.scrollToPosition(nearbyRestaurants.size() - 1);

        }


    }

    private void hideSearchList() {
        if (nearbyRestaurantsSearchRv.getVisibility() == View.VISIBLE) {
            nearbyRestaurantsSearchRv.setVisibility(View.GONE);
        }

        if (!searchResults.isEmpty()) {
            searchResults.clear();
            searchAdapter.notifyDataSetChanged();
        }
    }

    private void bindToLocationService() {

        if (!LocationListenerUtil.isLocationServiceRunning(requireContext())) {

            service = new Intent(requireContext(), LocationService.class);
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    Log.d("ttt", "onServiceConnected");
                    LocationService.LocationBinder locationBinder = (LocationService.LocationBinder) service;
                    LocationService locationService = locationBinder.getService();
                    locationService.addObserver(NearbyRestaurantsFragment.this);

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("ttt", "onServiceDisconnected");
                }
            };

            requireContext().bindService(service, serviceConnection, 0);
        }


    }

    private void unBindService() {
        if (serviceConnection != null) {
            requireContext().unbindService(serviceConnection);
            serviceConnection = null;
        }

        if (service != null) {
            requireContext().stopService(service);
        }

    }

    @Override
    public void notifyObservers(Location location) {

        currentLocation = location;


//        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),
//                location.getLongitude())));


    }

    @Override
    public void onResume() {
        super.onResume();
        if (locationRequester != null) {
            locationRequester.resumeLocationUpdates();
        }

        if (serviceConnection == null) {
            bindToLocationService();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unBindService();

        if (locationRequester != null) {
            locationRequester.stopLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        unBindService();
        super.onDestroy();
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