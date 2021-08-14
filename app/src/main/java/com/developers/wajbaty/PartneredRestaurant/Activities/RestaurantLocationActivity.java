package com.developers.wajbaty.PartneredRestaurant.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.PlaceSearchAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.PlaceSearchResult;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.BitmapUtil;
import com.developers.wajbaty.Utils.GeocoderUtil;
import com.developers.wajbaty.Utils.LocationRequester;
import com.developers.wajbaty.Utils.PlacesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RestaurantLocationActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener,
        LocationRequester.LocationRequestAction, SearchView.OnQueryTextListener,
        GeocoderUtil.GeocoderResultListener,
        PlaceSearchAdapter.PlaceSearchListener,
        PlacesUtil.PlaceResultListener {

    private static final int REQUEST_LOCATION_PERMISSION = 10;

    private GoogleMap mMap;
    private Marker currentMapMarker;

    //views
    private Button confirmLocationBtn;
    private ImageView currentLocationIV;
    private SearchView searchView;
    private RecyclerView mapLocateSearchRv;

    private ProgressDialogFragment progressDialogFragment;
    private LocationRequester locationRequester;

    //search adapter
    private PlaceSearchAdapter searchAdapter;
    private ArrayList<PlaceSearchResult> searchResults;

    private PlacesUtil placesUtil;

    private Map<String, PlaceSearchResult> placeResultMap;
    private int selectedSearchRestaurant = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.locateRestaurantMap);

        mapFragment.getMapAsync(this);

        confirmLocationBtn = findViewById(R.id.confirmLocationBtn);
        currentLocationIV = findViewById(R.id.currentLocationIV);
        searchView = findViewById(R.id.mapLocateSearchView);
        mapLocateSearchRv = findViewById(R.id.mapLocateSearchRv);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                Log.d("ttt", "closed search view");

                if (mapLocateSearchRv.getVisibility() == View.VISIBLE) {
                    mapLocateSearchRv.setVisibility(View.GONE);
                }

                if (!searchResults.isEmpty()) {
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                }

                return false;
            }
        });
        confirmLocationBtn.setOnClickListener(this);
        currentLocationIV.setOnClickListener(this);
        searchView.setOnQueryTextListener(this);


        searchResults = new ArrayList<>();
        searchAdapter = new PlaceSearchAdapter(searchResults, this);

        mapLocateSearchRv.setAdapter(searchAdapter);

//        CursorAdapter cursorAdapter = new CursorAdapter() {
//            @Override
//            public View newView(Context context, Cursor cursor, ViewGroup parent) {
//                return null;
//            }
//
//            @Override
//            public void bindView(View view, Context context, Cursor cursor) {
//
//            }
//        };
//
//
//searchView.setSuggestionsAdapter();

        fillSearchSuggestionsAdapter();

        Log.d("ttt", "onCreate");

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        markCurrentPosition();
        mMap.setOnMapClickListener(this);

        Log.d("ttt", "onMapReady");
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        markLocation(latLng, "Restaurant location");
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == confirmLocationBtn.getId()) {

            if (progressDialogFragment == null) {
                showProgressDialog();
            } else {
                progressDialogFragment.show(getSupportFragmentManager(), "progressDialog");
            }

            GeocoderUtil.getLocationAddress(this, currentMapMarker.getPosition(), this);

            Log.d("ttt", "current location is: " + currentMapMarker.getPosition());

        } else if (v.getId() == currentLocationIV.getId()) {

            if (locationRequester == null) {

                initializeLocationRequester();

            } else {

                locationRequester.getCurrentLocation();

            }

        }

    }


    private void markCurrentPosition() {

        Log.d("ttt", "markCurrentPosition");

        final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

            Log.d("ttt", "requesting location persmission");

            requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);

        } else if (currentMapMarker == null) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            initializeLocationRequester();
        }

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                initializeLocationRequester();

            } else {

                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                markLocation(new LatLng(-34, 151), "Restaurant location");

            }
        }

    }

    private void showProgressDialog() {
        Log.d("ttt", "showProgressDialog");

        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getSupportFragmentManager(), "progress");
    }


    private void initializeLocationRequester() {

        Log.d("ttt", "initializeLocationRequester");

        if (progressDialogFragment == null) {

            showProgressDialog();

        } else {

            progressDialogFragment.show(getSupportFragmentManager(), "progressDialog");

        }

        locationRequester = new LocationRequester(this, this);
        locationRequester.getCurrentLocation();

    }


//    public void markCurrentLocation(Location location) {
//
//        progressDialogFragment.dismiss();
////        sweetAlertDialog.dismiss();
//
//        final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//        currentMapMarker =
//                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Restaurant Location"));
//
//        if(currentMapMarker!=null){
//            currentMapMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
//                    BitmapUtil.getBitmap(this, R.drawable.location_marker_icon)));
//        }
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 20.0f));
//
//    }


    private void markLocation(LatLng latLng, String name) {
        if (currentMapMarker != null)
            currentMapMarker.remove();


        currentMapMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));

        if (currentMapMarker != null) {
            currentMapMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                    BitmapUtil.getBitmap(this, R.drawable.location_marker_icon)));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20.0f));

        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            progressDialogFragment.dismiss();
            progressDialogFragment = null;
        }

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


    private void fillSearchSuggestionsAdapter() {

//        Places.initialize(this, "AIzaSyBIWrMT4apMKnRNdm1kZwfOtqyTZG-eBUw");
//
//        PlacesClient placesClient = Places.createClient(this);
////
////        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
////
////
////
////        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
////                .build();
//
//        final String link =
//                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
//                        "location=-33.8670522,151.1957362&radius=1500&type=restaurant&" +
//                        "keyword=cruise&key=AIzaSyBIWrMT4apMKnRNdm1kZwfOtqyTZG-eBUw";
//
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(link, null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//
//                        Log.d("ttt",response.toString());
//
//                    }
//                },error -> {
//
//            Log.d("ttt",error.getMessage());
//
//        });
//
//        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        queue.add(jsonObjectRequest);
//
//        queue.start();

//        ArrayAdapter<String> placesAdapter


    }


    @Override
    public void locationFetched(LatLng latLng) {

        markLocation(latLng, "Restaurant location");

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        if (!searchResults.isEmpty()) {
            searchResults.clear();
            searchAdapter.notifyDataSetChanged();
        }

        if (placeResultMap != null) {

            final String placeSearchName = query.toLowerCase();

            for (String placeName : placeResultMap.keySet()) {

                placeName = placeName.toLowerCase();

                if (placeName.equals(placeSearchName) || placeName.contains(placeSearchName)
                        || placeSearchName.contains(placeName)) {
                    searchResults.add(placeResultMap.get(placeName));
                }

            }

        }


        if (!searchResults.isEmpty()) {

            if (mapLocateSearchRv.getVisibility() == View.GONE) {
                mapLocateSearchRv.setVisibility(View.VISIBLE);
            }
            searchAdapter.notifyDataSetChanged();

        } else {
            showProgressDialog();


            if (placesUtil == null)
                placesUtil = new PlacesUtil(this, this);

            placesUtil.searchForRestaurant(query);

        }


        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void addressFetched(Map<String, Object> addressMap) {

        Log.d("ttt", "addressFetched");

        dismissProgressDialog();

        final Intent intent = new Intent(this, RestaurantMediaFillingActivity.class);
        intent.putExtra("addressMap", (Serializable) addressMap);

        if (!searchResults.isEmpty() && selectedSearchRestaurant > -1 && selectedSearchRestaurant < searchResults.size()) {

            final PlaceSearchResult searchResult = searchResults.get(selectedSearchRestaurant);

            if (searchResult.getLat() == currentMapMarker.getPosition().latitude &&
                    searchResult.getLng() == currentMapMarker.getPosition().longitude) {

                intent.putExtra("restaurantName", searchResult.getName());
                intent.putExtra("restaurantImageURL", searchResult.getImageURL());
            }
        }

        startActivity(intent);
        finish();
    }

    @Override
    public void addressFetchFailed(String errorMessage) {

        Toast.makeText(this,
                "Failed to fetch your address! Please check your internet connection and try again",
                Toast.LENGTH_LONG).show();

        dismissProgressDialog();

        if (errorMessage != null) {

            Log.d("ttt", errorMessage);

        }

    }

    private void dismissProgressDialog() {
        if (progressDialogFragment != null) {
            progressDialogFragment.dismiss();

        }
    }

    @Override
    public void onSearchResultClicked(int position) {

        selectedSearchRestaurant = position;

        final PlaceSearchResult searchResult = searchResults.get(position);

        mapLocateSearchRv.setVisibility(View.GONE);
        markLocation(new LatLng(searchResult.getLat(), searchResult.getLng()), searchResult.getName());


    }

    @Override
    public void onPlacesFound(ArrayList<PlaceSearchResult> placeSearchResults) {

        if (placeResultMap == null)
            placeResultMap = new HashMap<>();

        for (PlaceSearchResult placeSearchResult : placeSearchResults) {

            boolean found = false;
            for (String placeName : placeResultMap.keySet()) {

                placeName = placeName.toLowerCase();

                if (placeName.equalsIgnoreCase(placeSearchResult.getName())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                placeResultMap.put(placeSearchResult.getName().toLowerCase(), placeSearchResult);
            }

        }


        dismissProgressDialog();

//        if(!searchResults.isEmpty()){
//            searchResults.clear();
//            searchAdapter.notifyDataSetChanged();
//        }

        searchResults.addAll(placeSearchResults);
        searchAdapter.notifyDataSetChanged();

        if (mapLocateSearchRv.getVisibility() == View.GONE) {
            mapLocateSearchRv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPlacesError(String errorMessage) {
        dismissProgressDialog();
        Toast.makeText(this,
                "An error occurred while search for restaurant! " +
                        "Please try again", Toast.LENGTH_SHORT).show();
        Log.d("RestuarantLocation", errorMessage);

    }
}