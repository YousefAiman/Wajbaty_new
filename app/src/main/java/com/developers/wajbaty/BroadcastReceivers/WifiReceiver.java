package com.developers.wajbaty.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.developers.wajbaty.Utils.GlobalVariables;

public class WifiReceiver extends BroadcastReceiver {

    private static ConnectivityManager connectivityManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (connectivityManager == null)
            connectivityManager = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);


        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

        if (netInfo != null &&
//            netInfo.getType() == ConnectivityManager.TYPE_WIFI
                netInfo.isConnected()
        ) {
            Log.d("ttt", "wifi online");
            GlobalVariables.setWifiIsOn(true);
        } else {
            GlobalVariables.setWifiIsOn(false);
            Log.d("ttt", "wifi offline");
        }
    }

}
