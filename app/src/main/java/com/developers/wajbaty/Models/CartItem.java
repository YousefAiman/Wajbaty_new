package com.developers.wajbaty.Models;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class CartItem implements Serializable {

    private String itemId;
    private int count;
    private long timeAdded;
    private String restaurantID;

    @Exclude
    private String name;
    @Exclude
    private String imageUrl;
    @Exclude
    private float price;
    @Exclude
    private String currency;

    public CartItem() {
    }

    public CartItem(String itemId, int count, long timeAdded, String restaurantID) {
        this.itemId = itemId;
        this.count = count;
        this.timeAdded = timeAdded;
        this.restaurantID = restaurantID;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }

    @Exclude
    public String getName() {
        return name;
    }

    @Exclude
    public void setName(String name) {
        this.name = name;
    }

    @Exclude
    public float getPrice() {
        return price;
    }

    @Exclude
    public void setPrice(float price) {
        this.price = price;
    }

    @Exclude
    public String getImageUrl() {
        return imageUrl;
    }

    @Exclude
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }
}
