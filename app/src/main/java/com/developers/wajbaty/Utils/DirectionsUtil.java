package com.developers.wajbaty.Utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.developers.wajbaty.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsUtil {

    private static DirectionsListeners directionsListeners;
    private final DocumentReference documentReference;


    public DirectionsUtil(DirectionsListeners directionsListeners, DocumentReference documentReference) {
        DirectionsUtil.directionsListeners = directionsListeners;
        this.documentReference = documentReference;
    }

    public static List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<>();

        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {

            jRoutes = jObject.getJSONArray("routes");
            int jRoutesLength = jRoutes.length();
            // Traversing all routes
            for (int i = 0; i < jRoutesLength; i++) {

                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");

                List<HashMap<String, String>> path = new ArrayList<>();

                int jLegsLength = jLegs.length();
                // Traversing all legs
                for (int j = 0; j < jLegsLength; j++) {

                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    int jStepsLength = jSteps.length();
                    // Traversing all steps
                    for (int k = 0; k < jStepsLength; k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                }
                routes.add(path);
            }


        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return routes;
    }

    private static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private static String getDirectionsUrl(Context context, LatLng origin, LatLng[] wayPoints, LatLng destination) {

        String baseUrl = "https://maps.googleapis.com/maps/api/directions/json?";

        //Origin
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        //Destination
        String str_destination = "destination=" + destination.latitude + "," + destination.longitude;

        //Way points
        String waypoints = "";

        for (int i = 0; i < wayPoints.length; i++) {

            LatLng point = wayPoints[i];

            if (i == 0) {
                waypoints = "waypoints=";
            }

            waypoints = waypoints.concat("via:" + point.latitude + "%2C" + point.longitude);

            if (i < waypoints.length() - 1) {
                waypoints = waypoints.concat("%7C");
            }

        }

        String api_key = "key=" + context.getString(R.string.directions_api_key);


        return baseUrl + str_origin + "&" + str_destination + "&" + waypoints + "&" + api_key;
    }

    public void getDirections(Context context, String firestoreResult) {

        try {

            JSONObject jsonObject = new JSONObject(firestoreResult);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    getPolyLinesForRoute(context, parse(jsonObject));
                }
            }).start();

        } catch (JSONException e) {
            Log.d("DirectionsApi", "failed to convert to json: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void getDirections(Context context, LatLng startPoint, LatLng[] wayPoints, LatLng destinationPoint) {


        RequestQueue queue = Volley.newRequestQueue(context);

        String url = getDirectionsUrl(context, startPoint, wayPoints, destinationPoint);

        new Thread(new Runnable() {
            @Override
            public void run() {

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, response -> {
                    try {
                        if (response.getString("status").equals("OK")) {

                            getPolyLinesForRoute(context, parse(response));

                            final HashMap<String, String> directionsMap = new HashMap<>();
                            directionsMap.put("DirectionsJsonObject", response.toString());

                            documentReference.collection("Directions")
                                    .document("Directions")
                                    .set(directionsMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            Log.d("DirectionsApi", "uploaded directions object to firestore");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.d("DirectionsApi", "failed uploading " +
                                            "json object to firestore: " + e.getMessage());

                                }
                            });

                        } else {
                            Log.d("DirectionsApi", "error here man 3: " +
                                    response.getJSONObject("status").getString("message"));
                        }
                    } catch (JSONException e) {
                        Log.d("DirectionsApi", "error here man 1: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, error -> {
                    Log.d("DirectionsApi", "error here man 2: " + error.getMessage());
                });
                queue.add(jsonObjectRequest);
                queue.start();

            }
        }).start();


//        Log.d("DirectionsApi","url is: "+url);
    }

    private void getPolyLinesForRoute(Context context, List<List<HashMap<String, String>>> directionsResult) {

        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        int resultSize = directionsResult.size();

        //Traversing through all the routes
        for (int i = 0; i < resultSize; i++) {

            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            //Fetching i-th route
            List<HashMap<String, String>> path = directionsResult.get(i);

            int pathSize = path.size();
            // Fetching all the points in i-th route
            for (int j = 0; j < pathSize; j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));

                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
//            lineOptions.width(7);
            lineOptions.color(ResourcesCompat.getColor(context.getResources(), R.color.orange, null));
        }

        directionsListeners.onPolyLineFetched(lineOptions);
    }

    public interface DirectionsListeners {
        void onPolyLineFetched(PolylineOptions polylineOptions);
    }


}
