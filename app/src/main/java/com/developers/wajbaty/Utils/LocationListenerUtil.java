package com.developers.wajbaty.Utils;

import android.app.ActivityManager;
import android.content.Context;

import com.developers.wajbaty.Services.LocationService;

public class LocationListenerUtil {

    public static boolean isLocationServiceRunning(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceInfo.getClass().getName().equals(LocationService.class.getName())) {
                return true;
            }
        }

        return false;
    }

}
