package com.developers.wajbaty.Utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.developers.wajbaty.Models.PlaceSearchResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlacesUtil {

    private static final String TAG = "PlacesUtil";

    private static final String PLACES_API_KEY = "AIzaSyDvlvmMqf3YXfMsSKn4xs0SGFj8OCGzLvU";
    private static PlaceResultListener placeResultListener;
    private final Context context;

    public PlacesUtil(Context context, PlaceResultListener placeResultListener) {
        this.context = context;
        PlacesUtil.placeResultListener = placeResultListener;
    }

    public void searchForRestaurant(String name) {

        String url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?" +
                "input=" + name +
                "&inputtype=textquery&" +
                "fields=photos,formatted_address,name,geometry&type=restaurant" +
                "&key=" + PLACES_API_KEY;

        RequestQueue queue = Volley.newRequestQueue(context);
        new Thread(new Runnable() {
            @Override
            public void run() {

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, response -> {
                    try {
                        if (response.getString("status").equals("OK")) {

                            final JSONArray candidates = response.getJSONArray("candidates");
                            if (candidates != null && candidates.length() != 0) {

                                final ArrayList<PlaceSearchResult> placeSearchResults = new ArrayList<>();

                                for (int i = 0; i < candidates.length(); i++) {

                                    final JSONObject cadidateObject = candidates.getJSONObject(i);

                                    JSONObject locationObject = cadidateObject.getJSONObject("geometry").getJSONObject("location");

                                    final double lat = locationObject.getDouble("lat"),
                                            lng = locationObject.getDouble("lng");

                                    final JSONArray photos = cadidateObject.getJSONArray("photos");

                                    String photoUrl = null;

                                    if (photos != null && photos.length() != 0) {

                                        JSONObject firstPhoto = photos.getJSONObject(0);

                                        photoUrl =
                                                "https://maps.googleapis.com/maps/api/place/photo?" +
                                                        "maxwidth=" + firstPhoto.getInt("width") +
                                                        "&photoreference=" + firstPhoto.getString("photo_reference") +
                                                        "&key=" + PLACES_API_KEY;

                                    }

                                    if (cadidateObject != null) {

                                        PlaceSearchResult searchResult =
                                                new PlaceSearchResult(
                                                        cadidateObject.getString("name"),
                                                        cadidateObject.getString("formatted_address"),
                                                        lat,
                                                        lng,
                                                        photoUrl
                                                );

                                        placeSearchResults.add(searchResult);
                                    }

                                }

                                placeResultListener.onPlacesFound(placeSearchResults);
                            } else {
                                placeResultListener.onPlacesError("Empty candidates");
                            }

                        } else {
                            Log.d(TAG, "error here man 3: " +
                                    response.getJSONObject("status").getString("message"));

                            placeResultListener.onPlacesError("status error " + response.getJSONObject("status").getString("message"));
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "error here man 1: " + e.getMessage());
                        e.printStackTrace();
                        placeResultListener.onPlacesError("JSONException: " + e.getMessage());
                    }
                }, error -> {
                    Log.d(TAG, "error here man 2: " + error.getMessage());
                    placeResultListener.onPlacesError("error: " + error.getMessage());
                });
                queue.add(jsonObjectRequest);
                queue.start();

            }
        }).start();


    }

    public interface PlaceResultListener {
        void onPlacesFound(ArrayList<PlaceSearchResult> placeSearchResults);

        void onPlacesError(String errorMessage);
    }


}
