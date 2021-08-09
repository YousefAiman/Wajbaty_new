package com.developers.wajbaty.Models;

import java.io.Serializable;

public class RestaurantSearchResult implements Serializable {

    private String restaurantID;
    private String restaurantImageURL;
    private String restaurantName;
    private String restaurantDistance;

    public RestaurantSearchResult(String restaurantID, String restaurantImageURL, String restaurantName, String restaurantDistance) {
        this.setRestaurantID(restaurantID);
        this.setRestaurantImageURL(restaurantImageURL);
        this.setRestaurantName(restaurantName);
        this.setRestaurantDistance(restaurantDistance);
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
}
