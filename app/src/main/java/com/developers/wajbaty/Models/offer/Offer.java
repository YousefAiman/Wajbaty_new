package com.developers.wajbaty.Models.offer;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public class Offer implements Serializable {

    public static final int RESTAURANT_OFFER = 1, MENU_ITEM_DISCOUNT = 2, MEAL_OFFER = 3;

    private String ID;
    private String restaurantId;
    private String destinationId;
    private int type;
    private String imageUrl;
    private String title;
    private long startTime;
    private long endTime;
    private GeoPoint restaurantLatLng;
    private String geohash;

    public Offer() {
    }

    public Offer(String ID, String restaurantId, String destinationId, int type, String imageUrl, String title, long startTime, long endTime, GeoPoint restaurantLatLng, String geohash) {
        this.ID = ID;
        this.restaurantId = restaurantId;
        this.destinationId = destinationId;
        this.type = type;
        this.imageUrl = imageUrl;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.restaurantLatLng = restaurantLatLng;
        this.geohash = geohash;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public GeoPoint getRestaurantLatLng() {
        return restaurantLatLng;
    }

    public void setRestaurantLatLng(GeoPoint restaurantLatLng) {
        this.restaurantLatLng = restaurantLatLng;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }


//
//    public class LimitedTimeDiscountOffer extends Offer{
//
//
//        float previousPrice;
//        float newPrice;
//
//    }
//
}
