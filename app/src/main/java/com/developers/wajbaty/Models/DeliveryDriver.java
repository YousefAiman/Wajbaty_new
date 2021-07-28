package com.developers.wajbaty.Models;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public class DeliveryDriver extends User implements Serializable {

    public static final int STATUS_AVAILABLE = 1, STATUS_UNAVAILABLE = 2, STATUS_DELIVERING = 3;

    private float rating;
    private String currentDeliveryId;
    private int status;
    private GeoPoint currentGeoPoint;
    private String geohash;


    public DeliveryDriver(String ID, String name, String email, String phoneNumber, String imageURL, String countryCode, String cloudMessagingToken, int type, float rating, String currentDeliveryId, int status, GeoPoint currentGeoPoint, String geohash) {
        super(ID, name, email, phoneNumber, imageURL, countryCode, cloudMessagingToken, type);
        this.rating = rating;
        this.currentDeliveryId = currentDeliveryId;
        this.status = status;
        this.currentGeoPoint = currentGeoPoint;
        this.geohash = geohash;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getCurrentDeliveryId() {
        return currentDeliveryId;
    }

    public void setCurrentDeliveryId(String currentDeliveryId) {
        this.currentDeliveryId = currentDeliveryId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public GeoPoint getCurrentGeoPoint() {
        return currentGeoPoint;
    }

    public void setCurrentGeoPoint(GeoPoint currentGeoPoint) {
        this.currentGeoPoint = currentGeoPoint;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }
}
