package com.developers.wajbaty.Models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class MenuItem implements Serializable {

    private String ID;
    private String name;
    private float price;
    private String currency;
    private List<String> imageUrls;
    private String category;
    private long timeCreated;
    private String restaurantId;
    private List<String> ingredients;
    private ReviewSummary reviewSummary;
    private boolean isDiscounted;
    private Map<String, Object> discountMap;
    private float rating;
    private int favoriteCount;
    private String region;


    public MenuItem() {
    }

    public MenuItem(String ID, String name, float price, List<String> imageUrls,
                    String category, long timeCreated, String restaurantId, String region) {
        this.ID = ID;
        this.name = name;
        this.price = price;
        this.imageUrls = imageUrls;
        this.category = category;
        this.timeCreated = timeCreated;
        this.restaurantId = restaurantId;
        this.region = region;
    }


    public ReviewSummary getReviewSummary() {
        return reviewSummary;
    }

    public void setReviewSummary(ReviewSummary reviewSummary) {
        this.reviewSummary = reviewSummary;
    }

    public boolean isDiscounted() {
        return isDiscounted;
    }

    public void setDiscounted(boolean discounted) {
        isDiscounted = discounted;
    }

    public Map<String, Object> getDiscountMap() {
        return discountMap;
    }

    public void setDiscountMap(Map<String, Object> discountMap) {
        this.discountMap = discountMap;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    static class Builder {

        private String ID, name, currency, category, restaurantId;
        private float price;
        private List<String> imageUrls, ingredients;
        private long timeCreated;
        private String region;


        public MenuItem build() {

            if (ID == null)
                throw new NullPointerException(" add the menu item id");

            if (name == null)
                throw new NullPointerException(" add the menu item name");

            if (category == null)
                throw new NullPointerException(" add the menu item category");

            if (price == 0)
                throw new NullPointerException(" add the menu item price");

            if (restaurantId == null)
                throw new NullPointerException(" add the menu item restaurant id");

            if (timeCreated == 0)
                throw new NullPointerException(" add the menu item creation time");

            if (imageUrls == null || imageUrls.isEmpty())
                throw new NullPointerException(" add at least one menu item imageUrl");


            final MenuItem menuItem = new MenuItem(ID, name, price, imageUrls, category, timeCreated, restaurantId, region);
            if (currency != null) {
                menuItem.setCurrency(currency);
            }

            if (ingredients != null) {
                menuItem.setIngredients(ingredients);
            }


            return menuItem;

        }


        public void setID(String ID) {
            this.ID = ID;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPrice(float price) {
            this.price = price;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setImageUrls(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setTimeCreated(long timeCreated) {
            this.timeCreated = timeCreated;
        }

        public void setRestaurantId(String restaurantId) {
            this.restaurantId = restaurantId;
        }

        public void setIngredients(List<String> ingredients) {
            this.ingredients = ingredients;
        }

        public void setRegion(String region) {
            this.region = region;
        }
    }

    public static class MenuItemSummary implements Serializable {

        private String ID;
        private String name;
        private float price;
        private String currency;
        private String restaurantId;
        private List<String> imageUrls;
        private boolean isDiscounted;
        private Map<String, Object> discountMap;


        public MenuItemSummary() {
        }

        public MenuItemSummary(MenuItem menuItem) {
            this.ID = menuItem.getID();
            this.name = menuItem.getName();
            this.price = menuItem.getPrice();
            this.currency = menuItem.getCurrency();
            this.restaurantId = menuItem.getRestaurantId();
            this.imageUrls = menuItem.getImageUrls();
            this.isDiscounted = menuItem.isDiscounted();
            this.discountMap = menuItem.getDiscountMap();
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

        public float getPrice() {
            return price;
        }

        public void setPrice(float price) {
            this.price = price;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public List<String> getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        public boolean isDiscounted() {
            return isDiscounted;
        }

        public void setDiscounted(boolean discounted) {
            isDiscounted = discounted;
        }

        public Map<String, Object> getDiscountMap() {
            return discountMap;
        }

        public void setDiscountMap(Map<String, Object> discountMap) {
            this.discountMap = discountMap;
        }

        public String getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(String restaurantId) {
            this.restaurantId = restaurantId;
        }
    }

}
