package com.developers.wajbaty.Utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GeocoderUtil {

    private static int retries = 0;
    private static RequestQueue queue;

    public static void getLocationAddress(Context context, LatLng latLng, GeocoderResultListener geocoderResultListener) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                retries = 0;


                final Geocoder geocoder = new Geocoder(context, new Locale("ar"));

                try {
                    final List<Address> addresses = geocoder.getFromLocation(latLng.latitude,
                            latLng.longitude, 1);

                    if (!addresses.isEmpty() && addresses.get(0).getCountryName() != null) {

                        final Address a = addresses.get(0);

                        Log.d("ttt", "address to string: " + a.toString());

                        final String countryCode = a.getCountryCode();

                        String currency = Currency.getInstance(new Locale("en", countryCode))
                                .getCurrencyCode();

                        if (currency.contains(".")) {
                            if (currency.substring(currency.length() - 2, currency.length() - 1)
                                    .equals(".")) {
                                currency = currency.substring(0, currency.length() - 2);
                            }
                        }

                        Log.d("ttt", "currency: " + currency);

                        Log.d("ttt", "from geocoder: " + a.toString());

                        final Map<String, Object> map = new HashMap<>();

                        Log.d("ttt", a.getAddressLine(0));

                        map.put("latLng", latLng);
                        map.put("countryCode", countryCode);
                        map.put("city", a.getLocality());

                        map.put("currency", currency);
//                    map.put("street",a.getAddressLine(0).split(",")[0]);
                        map.put("address", formatAddressGeocoder(a));
                        map.put("fullAddress", a.getAddressLine(0));

                        map.put("placeName", a.getPremises() + " " + a.getPostalCode());

                        geocoderResultListener.addressFetched(map);

                    } else {
                        Log.d("ttt", "no address so fetching from api");

                        fetchFromApi(context, latLng.latitude, latLng.longitude, geocoderResultListener);
                    }
                } catch (IOException e) {
                    fetchFromApi(context, latLng.latitude, latLng.longitude, geocoderResultListener);
                    Log.d("ttt", "geocoder error:" + e.getLocalizedMessage());
                }

            }
        }).start();

    }
//
//    public interface GeocoderCountryResultListener{
//        void fetchedCountryCode(String countryCode);
//        void fetchCountryCodeFailed(String errorMessage);
//    }

    private static void fetchFromApi(Context context, double latitude, double longitude, GeocoderResultListener geocoderResultListener) {

        final String url =
                "https://api.opencagedata.com/geocode/v1/json?key=078648c6ff684a8e851e63cbb1c8f6d8&q="
                        + latitude + "+" + longitude + "&pretty=1&no_annotations=1";

        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, response -> {
                    try {
                        if (response.getJSONObject("status").getString("message")
                                .equalsIgnoreCase("ok")) {

                            final JSONObject result = response.getJSONArray("results").getJSONObject(0);

                            final JSONObject address = result.getJSONObject("components");

                            Log.d("ttt", "address: " + address.toString());

                            final String countryCode = address.getString("country_code");

                            final Map<String, Object> map = new HashMap<>();
                            map.put("latLng", new LatLng(latitude, longitude));
                            map.put("countryCode", countryCode);
                            map.put("address", new Gson().fromJson(address.toString(), HashMap.class));


                            String formattedAddress;

                            if (result.has("formatted")) {
                                formattedAddress = result.getString("formatted");
                            } else {
                                formattedAddress = formatAddress(address);
                            }

                            if (formattedAddress != null) {
                                map.put("fullAddress", formattedAddress);
                            }

                            String currency = Currency.getInstance(new Locale("en", countryCode))
                                    .getCurrencyCode();

                            if (currency.contains(".")) {
                                if (currency.substring(currency.length() - 2, currency.length() - 1).equals(".")) {
                                    currency = currency.substring(0, currency.length() - 2);
                                }
                            }

                            map.put("currency", currency);


//
                            String city = null;

                            if (address.has("adminArea")) {
                                city = address.getString("adminArea");
                            } else if (address.has("village")) {
                                city = address.getString("village");
                            } else if (address.has("region")) {
                                city = address.getString("region");
                            } else if (address.has("city")) {
                                city = address.getString("city");
                            } else if (address.has("county")) {
                                city = address.getString("county");
                            }

                            if (city != null) {
                                map.put("city", city);
                            }

                            if (address.has("_type") && !address.isNull("_type")) {
                                final String type = address.getString("_type");
                                if (!address.isNull(type)) {
                                    map.put("placeName", address.getString(type));
                                }
                            }

                            geocoderResultListener.addressFetched(map);

//
//                    Log.d("ttt", "code:+ " + countryCode);
//

                        } else {
                            Log.d("ttt", "error here man 3: " +
                                    response.getJSONObject("status").getString("message"));

                            geocoderResultListener.addressFetchFailed(
                                    response.getJSONObject("status").getString("message"));
                        }
                    } catch (JSONException e) {
                        geocoderResultListener.addressFetchFailed(e.getMessage());
                        Log.d("ttt", "error here man 1: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, error -> {

                    if (retries < 3) {
                        retries++;

                        fetchFromApi(context, latitude, longitude, geocoderResultListener);
                    } else {

                        geocoderResultListener.addressFetchFailed(error.getMessage());

                    }

                    Log.d("ttt", "error here man 2: " + error.getMessage());
                });
                queue.add(jsonObjectRequest);
                queue.start();

            }
        }).start();


    }


//    public static void main(String[] args){
//        fetchFromApi();
//    }

    private static String formatAddress(JSONObject address) {


        try {

            String cityName = null;

            if (address.has("city")) {
                cityName = address.getString("city");
            } else if (address.has("county")) {
                cityName = address.getString("county");
            } else if (address.has("village")) {
                cityName = address.getString("village");
            }


            String road = null;

            if (address.has("road")) {
                road = address.getString("road");
            }

            String suburb = null;

            if (address.has("suburb")) {
                suburb = address.getString("suburb");
            }


            return (suburb != null ? suburb + ", " : "") + (road != null ? road + ", " : "") + cityName + ", " +
                    address.get("region") + ", " + address.getString("country");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    private static HashMap<String, String> formatAddressGeocoder(Address address) {

        HashMap<String, String> addressMap = new HashMap<>();

        if (address.getAdminArea() != null) {
            addressMap.put("adminArea", address.getAdminArea());
        }

        if (address.getSubAdminArea() != null) {
            addressMap.put("region", address.getSubAdminArea());
        }


        if (address.getLocality() != null) {
            addressMap.put("city", address.getLocality());
        }

        if (address.getFeatureName() != null) {
            addressMap.put("placeName", address.getFeatureName());
        }

        if (address.getThoroughfare() != null) {
            addressMap.put("street", address.getThoroughfare());
        }

        return addressMap;
    }

    public interface GeocoderResultListener {
        void addressFetched(Map<String, Object> addressMap);

        void addressFetchFailed(String errorMessage);
    }
}
