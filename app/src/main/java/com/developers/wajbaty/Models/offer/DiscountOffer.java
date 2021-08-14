package com.developers.wajbaty.Models.offer;

import com.google.firebase.firestore.GeoPoint;

public class DiscountOffer extends Offer {

    private float previousPrice;
    private float newPrice;

    public DiscountOffer() {
    }

    public DiscountOffer(String ID, String restaurantId, String destinationId, int type, String imageUrl, String title, long startTime, long endTime, GeoPoint restaurantLatLng, String geohash) {
        super(ID, restaurantId, destinationId, type, imageUrl, title, startTime, endTime, restaurantLatLng, geohash);
    }

    public DiscountOffer(String ID, String restaurantId, String destinationId, int type, String imageUrl, String title, long startTime, long endTime, GeoPoint restaurantLatLng, float previousPrice, float newPrice, String geohash) {
        super(ID, restaurantId, destinationId, type, imageUrl, title, startTime, endTime, restaurantLatLng, geohash);
        this.previousPrice = previousPrice;
        this.newPrice = newPrice;
    }

    public float getPreviousPrice() {
        return previousPrice;
    }

    public void setPreviousPrice(float previousPrice) {
        this.previousPrice = previousPrice;
    }

    public float getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(float newPrice) {
        this.newPrice = newPrice;
    }


}
