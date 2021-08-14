package com.developers.wajbaty.Models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class PartneredRestaurant implements Serializable {

    public static final int STATUS_OPEN = 1, STATUS_CLOSED = 2, STATUS_SHUTDOWN = 3, STATUS_UNKNOWN = 4;

    private String ID;
    private String name;
    private String description;
    private String category;
    private String geohash;
    private double lat;
    private double lng;
    //    private Map<String,Object> coordinates;
    private String countryCode;
    private String city;
    private String fullAddress;
    private String mainImage;
    private List<String> bannerImages;
    private List<String> albumImages;
    private int status;
    private String ownerUid;
    private List<String> admins;
    private float averageRating;
    private int favCount;
    private HashMap<String, String> address;
    private ReviewSummary reviewSummary;

    private Map<String, Object> socialMediaLinks;
    private List<String> ServiceOptions;
    private Map<String, Map<String, Object>> Schedule;
    private Map<String, Object> ContactInformation;
    private List<String> AdditionalServices;

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
//    private List<String> AdditionalServices;


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }


    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public List<String> getBannerImages() {
        return bannerImages;
    }

    public void setBannerImages(List<String> bannerImages) {
        this.bannerImages = bannerImages;
    }

    public List<String> getAlbumImages() {
        return albumImages;
    }

    public void setAlbumImages(List<String> albumImages) {
        this.albumImages = albumImages;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public Map<String, Object> getSocialMediaLinks() {
        return socialMediaLinks;
    }

    public void setSocialMediaLinks(Map<String, Object> socialMediaLinks) {
        this.socialMediaLinks = socialMediaLinks;
    }

    public List<String> getServiceOptions() {
        return ServiceOptions;
    }

    public void setServiceOptions(List<String> serviceOptions) {
        ServiceOptions = serviceOptions;
    }

    public Map<String, Map<String, Object>> getSchedule() {
        return Schedule;
    }

    public void setSchedule(Map<String, Map<String, Object>> schedule) {
        Schedule = schedule;
    }

    public Map<String, Object> getContactInformation() {
        return ContactInformation;
    }

    public void setContactInformation(Map<String, Object> contactInformation) {
        ContactInformation = contactInformation;
    }


    public List<String> getAdditionalServices() {
        return AdditionalServices;
    }

    public void setAdditionalServices(List<String> additionalServices) {
        AdditionalServices = additionalServices;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }


    public ReviewSummary getReviewSummary() {
        return reviewSummary;
    }

    public void setReviewSummary(ReviewSummary reviewSummary) {
        this.reviewSummary = reviewSummary;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getFavCount() {
        return favCount;
    }

    public void setFavCount(int favCount) {
        this.favCount = favCount;
    }

    public HashMap<String, String> getAddress() {
        return address;
    }

    public void setAddress(HashMap<String, String> address) {
        this.address = address;
    }

    @IgnoreExtraProperties
    public static class PartneredRestaurantSummary {

        private String ID;
        private String name;
        private String mainImage;
        private String status;

        public PartneredRestaurantSummary() {
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

        public String getMainImage() {
            return mainImage;
        }

        public void setMainImage(String mainImage) {
            this.mainImage = mainImage;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    @IgnoreExtraProperties
    public static class NearbyPartneredRestaurant {

        private String ID;
        private String name;
        private String mainImage;
        private String geohash;
        private double lat;
        private double lng;

        @Exclude
        private String distanceFormatted;
        @Exclude
        private boolean isSelected;


        public NearbyPartneredRestaurant() {
        }

        public NearbyPartneredRestaurant(String ID, String name, String mainImage, String geohash, double lat, double lng) {
            this.ID = ID;
            this.name = name;
            this.mainImage = mainImage;
            this.geohash = geohash;
            this.lat = lat;
            this.lng = lng;
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

        public String getMainImage() {
            return mainImage;
        }

        public void setMainImage(String mainImage) {
            this.mainImage = mainImage;
        }

        @Exclude
        public String getDistanceFormatted() {
            return distanceFormatted;
        }

        @Exclude
        public void setDistanceFormatted(String distanceFormatted) {
            this.distanceFormatted = distanceFormatted;
        }

        public String getGeohash() {
            return geohash;
        }

        public void setGeohash(String geohash) {
            this.geohash = geohash;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }
    }


}
