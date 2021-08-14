package com.developers.wajbaty.Models;

import com.google.gson.annotations.SerializedName;

public class PlaceResponse {

    @SerializedName("location")
    private
    String location;
    @SerializedName("radius")
    private
    int radius;
    @SerializedName("key")
    private
    String key;
    @SerializedName("language")
    private
    String language;


    public PlaceResponse() {

    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
