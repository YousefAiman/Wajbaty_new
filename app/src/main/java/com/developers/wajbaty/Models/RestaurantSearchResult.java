package com.developers.wajbaty.Models;

import java.io.Serializable;

public class RestaurantSearchResult implements Serializable {

    private String restaurantID;
    private String restaurantImageURL;
    private String restaurantName;
    private String geohash;
    private double lat;
    private double lng;

    private String restaurantDistance;

    public RestaurantSearchResult(String restaurantID, String restaurantImageURL, String restaurantName, String restaurantDistance,
                                  String geohash, double lat, double lng) {
        this.setRestaurantID(restaurantID);
        this.setRestaurantImageURL(restaurantImageURL);
        this.setRestaurantName(restaurantName);
        this.setRestaurantDistance(restaurantDistance);
        this.setGeohash(geohash);
        this.setLat(lat);
        this.setLng(lng);
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getRestaurantImageURL() {
        return restaurantImageURL;
    }

    public void setRestaurantImageURL(String restaurantImageURL) {
        this.restaurantImageURL = restaurantImageURL;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantDistance() {
        return restaurantDistance;
    }

    public void setRestaurantDistance(String restaurantDistance) {
        this.restaurantDistance = restaurantDistance;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
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
}
