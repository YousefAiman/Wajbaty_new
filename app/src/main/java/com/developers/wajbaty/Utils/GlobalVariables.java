package com.developers.wajbaty.Utils;

import android.net.ConnectivityManager;

import com.developers.wajbaty.BroadcastReceivers.WifiReceiver;

import java.util.Map;

public class GlobalVariables {

    //    private static GlobalVariables instance;
    private static String currentRestaurantId;
    private static boolean wifiIsOn;
    private static Map<String, Integer> messagesNotificationMap;
    private static WifiReceiver currentWifiReceiver;
    private static ConnectivityManager.NetworkCallback registeredNetworkCallback;
//
//    public static GlobalVariables getInstance(){
//
//        if(instance == null){
//            instance = new GlobalVariables();
//        }
//
//        return instance;
//    }

    public static String getCurrentRestaurantId() {
        return currentRestaurantId;
    }

    public static void setCurrentRestaurantId(String currentRestaurantId) {
        GlobalVariables.currentRestaurantId = currentRestaurantId;
    }

    public static boolean isWifiIsOn() {
        return wifiIsOn;
    }

    public static void setWifiIsOn(boolean wifiIsOn) {
        GlobalVariables.wifiIsOn = wifiIsOn;
    }

    public static Map<String, Integer> getMessagesNotificationMap() {
        return messagesNotificationMap;
    }

    public static void setMessagesNotificationMap(Map<String, Integer> messagesNotificationMap) {
        GlobalVariables.messagesNotificationMap = messagesNotificationMap;
    }

    public static WifiReceiver getCurrentWifiReceiver() {
        return currentWifiReceiver;
    }

    public static void setCurrentWifiReceiver(WifiReceiver currentWifiReceiver) {
        GlobalVariables.currentWifiReceiver = currentWifiReceiver;
    }

    public static ConnectivityManager.NetworkCallback getRegisteredNetworkCallback() {
        return registeredNetworkCallback;
    }

    public static void setRegisteredNetworkCallback(ConnectivityManager.NetworkCallback registeredNetworkCallback) {
        GlobalVariables.registeredNetworkCallback = registeredNetworkCallback;
    }
}
