package com.developers.wajbaty.Models;

public class UserInfo {

    String ID;
    String username;
    String email;
    String phoneNumber;
    String cloudMessagingToken;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
