package com.developers.wajbaty.Models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public class ReviewSummary implements Serializable {

    private float averageRating;
    private HashMap<String, Long> ratingsMap;
    private int totalReviews;

    public ReviewSummary() {
    }

    public ReviewSummary(float averageRating, HashMap<String, Long> ratingsMap, int totalReviews) {
        this.setAverageRating(averageRating);
        this.ratingsMap = ratingsMap;
        this.totalReviews = totalReviews;
    }

    public ReviewSummary(HashMap<String, Object> reviewSummaryMap) {

        this.averageRating = ((Double) Objects.requireNonNull(reviewSummaryMap.get("averageRating"))).floatValue();
        this.ratingsMap = (HashMap<String, Long>) reviewSummaryMap.get("ratingsMap");
        this.totalReviews = ((Long) Objects.requireNonNull(reviewSummaryMap.get("totalReviews"))).intValue();

    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }


    public HashMap<String, Long> getRatingsMap() {
        return ratingsMap;
    }

    public void setRatingsMap(HashMap<String, Long> ratingsMap) {
        this.ratingsMap = ratingsMap;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }


}
