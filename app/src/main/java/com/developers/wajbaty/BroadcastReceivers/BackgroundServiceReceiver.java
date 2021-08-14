package com.developers.wajbaty.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.developers.wajbaty.Services.LocationService;

public class BackgroundServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.startForegroundService(new Intent(context, LocationService.class));

        } else {
            context.startService(new Intent(context, LocationService.class));

        }

    }
}
