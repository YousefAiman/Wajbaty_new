package com.developers.wajbaty.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class UserReview implements Serializable {

    private String reviewerId;
    private int rating;
    private long ratingTime;
    private String comment;
    private int likes;


    @Exclude
    private String reviewerImageUrl;
    @Exclude
    private String reviewerUsername;


    public UserReview() {
    }

    public UserReview(String reviewerId, int rating, long ratingTime, String comment) {
        this.reviewerId = reviewerId;
        this.rating = rating;
        this.ratingTime = ratingTime;
        this.comment = comment;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public long getRatingTime() {
        return ratingTime;
    }

    public void setRatingTime(long ratingTime) {
        this.ratingTime = ratingTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    @Exclude
    public String getReviewerImageUrl() {
        return reviewerImageUrl;
    }

    @Exclude
    public void setReviewerImageUrl(String reviewerImageUrl) {
        this.reviewerImageUrl = reviewerImageUrl;
    }

    @Exclude
    public String getReviewerUsername() {
        return reviewerUsername;
    }

    @Exclude
    public void setReviewerUsername(String reviewerUsername) {
        this.reviewerUsername = reviewerUsername;
    }
}
