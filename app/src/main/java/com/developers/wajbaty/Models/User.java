package com.developers.wajbaty.Models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public static final int TYPE_CUSTOMER = 1, TYPE_ADMIN = 2, TYPE_DELIVERY = 3;

    private String ID;
    private String name;
    private String email;
    private String phoneNumber;
    private String imageURL;
    private String countryCode;
    private String cloudMessagingToken;
    private int type;

    public User() {
    }

    public User(String ID, String name, String email, String phoneNumber, String imageURL, String countryCode, String cloudMessagingToken, int type) {
        this.ID = ID;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.imageURL = imageURL;
        this.countryCode = countryCode;
        this.cloudMessagingToken = cloudMessagingToken;
        this.type = type;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCloudMessagingToken() {
        return cloudMessagingToken;
    }

    public void setCloudMessagingToken(String cloudMessagingToken) {
        this.cloudMessagingToken = cloudMessagingToken;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
