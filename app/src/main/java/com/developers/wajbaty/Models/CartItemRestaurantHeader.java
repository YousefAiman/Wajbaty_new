package com.developers.wajbaty.Models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class CartItemRestaurantHeader extends CartItem {

    private String header;

    public CartItemRestaurantHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
