package com.developers.wajbaty.Models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Delivery implements Serializable {

    public static final int STATUS_PENDING = 1,STATUS_ACCEPTED = 2,STATUS_PICKED_UP = 3,
            STATUS_DELIVERED = 4;

    private String ID;
    private String requesterID;
    private HashMap<String,Float> menuItemPriceMap;
    private int status;
    private long orderTimeInMillis;

    private float totalCost;
    private String currency;
    private String address;
    private double lat;
    private double lng;
    private String geohash;

    private Map<String,Integer> restaurantMenuItemsMap;

    @Exclude private String userImageUrl;
    @Exclude private String userUsername;


    public Delivery() {
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, Integer> getRestaurantMenuItemsMap() {
        return restaurantMenuItemsMap;
    }

    public void setRestaurantMenuItemsMap(Map<String, Integer> restaurantMenuItemsMap) {
        this.restaurantMenuItemsMap = restaurantMenuItemsMap;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

//    @IgnoreExtraProperties
//    public static class DeliverySummary{
//
//        private String ID;
//        private String requesterID;
//        private long orderTimeInMillis;
//        private String address;
//
//        public DeliverySummary() {
//        }
//
//        public String getID() {
//            return ID;
//        }
//
//        public void setID(String ID) {
//            this.ID = ID;
//        }
//
//        public String getRequesterID() {
//            return requesterID;
//        }
//
//        public void setRequesterID(String requesterID) {
//            this.requesterID = requesterID;
//        }
//
//        public long getOrderTimeInMillis() {
//            return orderTimeInMillis;
//        }
//
//        public void setOrderTimeInMillis(long orderTimeInMillis) {
//            this.orderTimeInMillis = orderTimeInMillis;
//        }
//
//        public String getAddress() {
//            return address;
//        }
//
//        public void setAddress(String address) {
//            this.address = address;
//        }
//    }

    public static class InProgressDelivery extends Delivery{

        private String driverID;
        private GeoPoint driverLocation;
        private List<String> pickedUpCartItemsIDs;

        public InProgressDelivery() {
        }

        public InProgressDelivery(String driverID, GeoPoint driverLocation, List<String> pickedUpCartItemsIDs) {
            this.driverID = driverID;
            this.driverLocation = driverLocation;
            this.pickedUpCartItemsIDs = pickedUpCartItemsIDs;
        }

        public String getDriverID() {
            return driverID;
        }

        public void setDriverID(String driverID) {
            this.driverID = driverID;
        }

        public GeoPoint getDriverLocation() {
            return driverLocation;
        }

        public void setDriverLocation(GeoPoint driverLocation) {
            this.driverLocation = driverLocation;
        }

        public List<String> getPickedUpCartItemsIDs() {
            return pickedUpCartItemsIDs;
        }

        public void setPickedUpCartItemsIDs(List<String> pickedUpCartItemsIDs) {
            this.pickedUpCartItemsIDs = pickedUpCartItemsIDs;
        }
    }

    public Delivery(String ID, String requesterID, HashMap<String, Float> menuItemPriceMap, int status, long orderTimeInMillis, float totalCost,String currency, String address, double lat,double lng, String geohash,Map<String,Integer> restaurantMenuItemsMap) {
        this.ID = ID;
        this.requesterID = requesterID;
        this.menuItemPriceMap = menuItemPriceMap;
        this.status = status;
        this.orderTimeInMillis = orderTimeInMillis;
        this.totalCost = totalCost;
        this.setCurrency(currency);
        this.address = address;
        this.geohash = geohash;
        this.setLat(lat);
        this.setLng(lng);
        this.restaurantMenuItemsMap = restaurantMenuItemsMap;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getRequesterID() {
        return requesterID;
    }

    public void setRequesterID(String requesterID) {
        this.requesterID = requesterID;
    }

    public HashMap<String, Float> getMenuItemPriceMap() {
        return menuItemPriceMap;
    }

    public void setMenuItemPriceMap(HashMap<String, Float> menuItemPriceMap) {
        this.menuItemPriceMap = menuItemPriceMap;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getOrderTimeInMillis() {
        return orderTimeInMillis;
    }

    public void setOrderTimeInMillis(long orderTimeInMillis) {
        this.orderTimeInMillis = orderTimeInMillis;
    }

    public float getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(float totalCost) {
        this.totalCost = totalCost;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }
}
