package com.developers.wajbaty.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.developers.wajbaty.Activities.HomeActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.util.ArrayList;

public class LocationListenerUtil implements LocationListener {

    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10,
            MIN_UPDATE_DISTANCE = 10;

    private static LocationListenerUtil instance;
    private static ArrayList<LocationChangeObserver> observers;
    private static boolean isActive;

    public static LocationListenerUtil getInstance(){

        if(instance == null){
            instance = new LocationListenerUtil();
            observers = new ArrayList<>();
        }

        return instance;
    }

    private LocationListenerUtil() {
    }

    public interface LocationChangeObserver{
        void notifyObservers(Location location);
    }

    public void addLocationChangeObserver(LocationChangeObserver observer){
        if(!observers.contains(observer)){
            observers.add(observer);
        }
    }

    public void removeLocationChangeObserver(LocationChangeObserver observer){
            observers.remove(observer);
    }


    public void startListening(Activity activity){

        if(isActive){
           return;
        }

        final LocationRequest locationRequest = LocationRequest.create().
                setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10000).setFastestInterval(5000);

        final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices.getSettingsClient(activity)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> {
                    Log.d("ttt", "location is enabled");

                    LocationManager locationManager = (LocationManager)
                            activity.getSystemService(Context.LOCATION_SERVICE);

                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        isActive = true;

                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                5000,
                                10,
                                this);

                    }


                }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                Log.d("ttt", "location is not enabled");
                try {
                    isActive = false;
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(activity,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        for(LocationChangeObserver observer:observers){
            observer.notifyObservers(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}
