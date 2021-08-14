package com.developers.wajbaty.Models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class RestaurantAdmin extends User {

    private String myRestaurantID;

    public RestaurantAdmin(String ID, String name, String email, String phoneNumber, String imageURL, String countryCode, String cloudMessagingToken, int type, String myRestaurantID) {
        super(ID, name, email, phoneNumber, imageURL, countryCode, cloudMessagingToken, type);
        this.myRestaurantID = myRestaurantID;
    }

    public String getMyRestaurantID() {
        return myRestaurantID;
    }

    public void setMyRestaurantID(String myRestaurantID) {
        this.myRestaurantID = myRestaurantID;
    }
}
