package com.developers.wajbaty.Models;

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
