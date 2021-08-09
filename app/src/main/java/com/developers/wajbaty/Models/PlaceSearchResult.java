package com.developers.wajbaty.Models;

import java.io.Serializable;

public class PlaceSearchResult implements Serializable {

    private String name;
    private String formattedAddress;
    private double lat;
    private double lng;
    private String imageURL;

    public PlaceSearchResult(String name, String formattedAddress, double lat, double lng, String imageURL) {
        this.name = name;
        this.formattedAddress = formattedAddress;
        this.lat = lat;
        this.lng = lng;
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
