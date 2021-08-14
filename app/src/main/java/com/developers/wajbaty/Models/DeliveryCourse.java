package com.developers.wajbaty.Models;

import android.location.Location;

import java.io.Serializable;

public class DeliveryCourse implements Serializable {

    private String locationID;
    private String locationName;
    private Location location;
    private int itemCount;
    private boolean wasPassed;
    private boolean isActive;

    public DeliveryCourse() {
    }

    public DeliveryCourse(String locationID, String locationName, Location location, int itemCount, boolean wasPassed, boolean isActive) {
        this.setLocationID(locationID);
        this.locationName = locationName;
        this.location = location;
        this.itemCount = itemCount;
        this.wasPassed = wasPassed;
        this.isActive = isActive;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isWasPassed() {
        return wasPassed;
    }

    public void setWasPassed(boolean wasPassed) {
        this.wasPassed = wasPassed;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }
}
