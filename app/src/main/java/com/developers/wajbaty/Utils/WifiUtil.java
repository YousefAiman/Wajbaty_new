package com.developers.wajbaty.Utils;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.developers.wajbaty.BroadcastReceivers.WifiReceiver;

import java.util.ArrayList;
import java.util.List;

public class WifiUtil {

    static boolean checkWifiConnection(Context context) {
        if (!GlobalVariables.isWifiIsOn()) {
            Toast.makeText(context, "Please check your internet connection and try again!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static void registerNetworkCallback(ConnectivityManager cm) {


        final NetworkRequest.Builder builder = new NetworkRequest.Builder();

        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        }

//        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
//                .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
//                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
//                .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH);

        ConnectivityManager.NetworkCallback networkCallback;


        cm.registerNetworkCallback(builder.build(),
                networkCallback = new ConnectivityManager.NetworkCallback() {

                    final List<Network> activeNetworks = new ArrayList<>();

                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                        Log.d("ttt", "no internet avilalblble man");
                    }

                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);

                        Log.d("ttt", "network onAvailable");

                        if (!activeNetworks.contains(network)) {
                            Log.d("ttt", "adding netowrk to list: " + network.toString());
                            activeNetworks.add(network);
                        }

                        if (activeNetworks.size() > 0) {

                            if (!GlobalVariables.isWifiIsOn()) {
//                    FirebaseFirestore.getInstance().enableNetwork()
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                              @Override
//                              public void onSuccess(Void aVoid) {
//                                Log.d("ttt","enabled netowrk man hehe");
//                              }
//                            });

                                GlobalVariables.setWifiIsOn(true);
                            }

                            Log.d("ttt", "network is on man");


                        }

                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        super.onLost(network);

                        if (activeNetworks.contains(network)) {
                            Log.d("ttt", "removing netowrk from list: " + network.toString());
                            activeNetworks.remove(network);
                        }

                        if (activeNetworks.size() == 0) {

                            if (GlobalVariables.isWifiIsOn()) {
//                    FirebaseFirestore.getInstance().disableNetwork()
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                      @Override
//                      public void onSuccess(Void aVoid) {
//                        Log.d("ttt","disabled all firestore network queryies");
//                      }
//                    });
                                GlobalVariables.setWifiIsOn(false);
//                    FirebaseDatabase.getInstance().
                            }

                            Log.d("ttt", "wifi offline: " + network.toString());

                        }


                    }
                });


        GlobalVariables.setRegisteredNetworkCallback(networkCallback);
    }

    static void registerReceiver(Context context) {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        final WifiReceiver wifiReceiver = new WifiReceiver();
        GlobalVariables.setCurrentWifiReceiver(wifiReceiver);
        context.registerReceiver(wifiReceiver, intentFilter);

    }

    public static boolean isConnectedToInternet(Context context) {

        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//      cm.getAllNetworks()
            final NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());

            if (capabilities != null
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
//              (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
//                      || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
//                      || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
//                      || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
            ) {

                registerNetworkCallback(cm);

                Log.d("ttt", "registering ");
                return true;
            } else {

                return false;
            }

        } else {

            final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnected()) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    registerNetworkCallback(cm);
                } else {
                    registerReceiver(context);
                }

                return true;
            } else {

                return false;

            }

        }
    }

}
