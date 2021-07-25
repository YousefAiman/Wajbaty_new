package com.developers.wajbaty.Models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
public class RestaurantAdmin extends User{

    private List<String> administratingRestaurants;

    public RestaurantAdmin(String ID, String name, String email, String imageURL, String countryCode, String cloudMessagingToken, int type, List<String> administratingRestaurants) {
        super(ID, name, email, imageURL, countryCode, cloudMessagingToken, type);
        this.administratingRestaurants = administratingRestaurants;
    }

    public List<String> getAdministratingRestaurants() {
        return administratingRestaurants;
    }

    public void setAdministratingRestaurants(List<String> administratingRestaurants) {
        this.administratingRestaurants = administratingRestaurants;
    }
}
