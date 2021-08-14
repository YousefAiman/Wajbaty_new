package com.developers.wajbaty.Models;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class RestaurantOrder implements Serializable {

    public static final int TYPE_PENDING = 1, TYPE_DONE = 2, TYPE_CANCELLED = 3;

    private String ID;
    private long orderTimeInMillis;
    private String driverID;
    private float totalCost;
    private int status;
    private String currency;
    private int itemCount;

    @Exclude
    private String driverName;


    public RestaurantOrder() {
    }

    public RestaurantOrder(String ID, long orderTimeInMillis, String driverID, int status,
                           float totalCost, String currency, int itemCount) {
        this.ID = ID;
        this.orderTimeInMillis = orderTimeInMillis;
        this.driverID = driverID;
        this.status = status;
        this.totalCost = totalCost;
        this.currency = currency;
        this.itemCount = itemCount;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public long getOrderTimeInMillis() {
        return orderTimeInMillis;
    }

    public void setOrderTimeInMillis(long orderTimeInMillis) {
        this.orderTimeInMillis = orderTimeInMillis;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(float totalCost) {
        this.totalCost = totalCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Exclude
    public String getDriverName() {
        return driverName;
    }

    @Exclude
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
