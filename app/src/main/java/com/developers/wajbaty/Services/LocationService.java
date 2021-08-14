package com.developers.wajbaty.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {

    public LocationBinder binder = new LocationBinder();
    private List<LocationChangeObserver> observers;

    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;

    public void addObserver(LocationChangeObserver observer) {

        if (observers == null)
            observers = new ArrayList<>();

        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(LocationChangeObserver observer) {

        if (observers != null) {
            observers.remove(observer);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createChannel();
        } else {
            startForeground(1, new Notification());
        }

        requestLocationUpdates();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        Log.d("ttt", "service getting destroyed");
        if (client != null && locationCallback != null) {

            Log.d("ttt", "client.removeLocationUpdates(locationCallback)");
            client.removeLocationUpdates(locationCallback);
        }
//        Intent broadCastIntent = new Intent();
//        broadCastIntent.setAction("restartLocationService");
//        broadCastIntent.setClass(this,BackgroundServiceReceiver.class);
//        sendBroadcast(broadCastIntent);

    }

    private void requestLocationUpdates() {

        LocationRequest locationRequest = LocationRequest.create().
                setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000).setFastestInterval(2000).setSmallestDisplacement(3);

        client = LocationServices.getFusedLocationProviderClient(this);

        String permission = Manifest.permission.ACCESS_FINE_LOCATION;

//        client.removeLocationUpdates()


        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {

            client.requestLocationUpdates(locationRequest, locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {

                    final Location location = locationResult.getLastLocation();

                    if (observers != null) {
                        for (LocationChangeObserver observer : observers) {
                            observer.notifyObservers(location);
                        }
                    }
                    Log.d("ttt", "location service latlng: " + location.getLatitude() +
                            "," + location.getLongitude());

//                        currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());


                }

                @Override
                public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                }
            }, null);


        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    public void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel("com.getlocationbackground",
                    "Location Service", NotificationManager.IMPORTANCE_NONE);

            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            manager.createNotificationChannel(channel);

            Notification.Builder builder = new Notification.Builder(this, "com.getlocationbackground");
            Notification notification = builder.setOngoing(true).setContentTitle("This app is using your gps")
                    .setCategory(Notification.CATEGORY_SERVICE).build();

            startForeground(2, notification);

        }
    }


    public interface LocationChangeObserver {
        void notifyObservers(Location location);
    }


//    private void startTime(){
//
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//
//
//
//            }
//        };
//
//    }

    public class LocationBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }
    }
}
