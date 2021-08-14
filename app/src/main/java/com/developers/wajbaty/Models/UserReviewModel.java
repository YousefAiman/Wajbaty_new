package com.developers.wajbaty.Models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class UserReviewModel extends Observable {

    public static final int
            LIKE_RESULT_SUCCESS = 1,
            LIKE_RESULT_FAILED = 2,
            UNLIKE_RESULT_SUCCESS = 3,
            UNLIKE_RESULT_FAILED = 4,
            LIKED_REVIEWS_SUCCESS = 5,
            LIKED_REVIEWS_FAILED = 6,
            ALREADY_REVIEWED = 7,
            HAS_NOT_REVIEWED = 8;

    private final CollectionReference reviewsRef;
    private final List<String> likedReviews;

    public UserReviewModel(CollectionReference reviewsRef) {
        this.reviewsRef = reviewsRef;
        likedReviews = new ArrayList<>();
    }

    public List<String> getLikedReviews() {
        return likedReviews;
    }

    public void getReviewSummary() {

        reviewsRef.getParent().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if (snapshot.contains("reviewSummary")) {

                    final ReviewSummary reviewSummary = (ReviewSummary) snapshot.get("reviewSummary");
                    setChanged();
                    notifyObservers(reviewSummary);

                } else {

//                    HashMap<Integer,Integer> emptyRatingMap = new HashMap<>();
//
//                    for(int i=1;i<=5;i++){
//                        emptyRatingMap.put((byte) i,0);
//                    }
//                    setChanged();
//                    notifyObservers(new ReviewSummary(0,emptyRatingMap,0));
                }


            }
        });

    }

    public void addReview(String comment, int rating) {

//        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final UserReview userReview = new UserReview(userId, rating, System.currentTimeMillis(), comment);

        reviewsRef.document(userId).set(userReview).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                reviewsRef.getParent().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        if (!snapshot.contains("reviewSummary") || snapshot.get("reviewSummary") == null) {

                            final HashMap<String, Long> ratingMap = new HashMap<>();

                            for (int i = 1; i <= 5; i++) {
                                if (i == rating)
                                    continue;

                                ratingMap.put(String.valueOf(i), 0L);
                            }

                            ratingMap.put(String.valueOf(rating), 1L);

                            final ReviewSummary reviewSummary = new ReviewSummary(
                                    rating, ratingMap, 1);

                            snapshot.getReference().update("reviewSummary", reviewSummary)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            setChanged();
                                            notifyObservers(reviewSummary);
                                            setChanged();
                                            notifyObservers(userReview);

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            reviewsRef.document(userId).delete();
                                            setChanged();
                                            notifyObservers(e.getMessage());
                                        }
                                    });

                        } else {

                            updatedRatingSummary(rating, userReview, snapshot);

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        reviewsRef.document(userId).delete();
                        setChanged();
                        notifyObservers(e.getMessage());

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setChanged();
                notifyObservers(e.getMessage());

            }
        });

    }

    private void updatedRatingSummary(int rating, UserReview userReview, DocumentSnapshot snapshot) {

        if (!snapshot.contains("reviewSummary")) {
            return;
        }

        Object object = snapshot.get("reviewSummary");

        if (object == null) {
            return;
        }

        if (!(object instanceof HashMap)) {
            return;
        }

        final ReviewSummary reviewSummary = new ReviewSummary((HashMap<String, Object>) object);

        final DocumentReference ref = snapshot.getReference();

        final HashMap<String, Long> ratingsMap = reviewSummary.getRatingsMap();


        ref.update("reviewSummary.ratingsMap." + rating,
                FieldValue.increment(1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

//                        int value = ((Long)ratingsMap.get(String.valueOf(rating))).intValue();

                        ratingsMap.put(String.valueOf(rating), ratingsMap.get(String.valueOf(rating)) + 1);

                        ref.update("reviewSummary.totalReviews", FieldValue.increment(1))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        reviewSummary.setTotalReviews(reviewSummary.getTotalReviews() + 1);

                                        float totalRating = 0;

                                        for (String rating : ratingsMap.keySet()) {

                                            totalRating += ratingsMap.get(rating) * Integer.parseInt(rating);

                                        }

                                        final float newAverageRating =
                                                new BigDecimal(totalRating / reviewSummary.getTotalReviews())
                                                        .setScale(1, RoundingMode.HALF_UP).floatValue();


                                        reviewSummary.setAverageRating(newAverageRating);

                                        ref.update("reviewSummary.averageRating", newAverageRating,
                                                "averageRating", newAverageRating)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        setChanged();
                                                        notifyObservers(reviewSummary);
                                                        setChanged();
                                                        notifyObservers(userReview);

                                                    }
                                                });

                                    }
                                });
                    }
                });


    }

    public void likeReview(String userId, String reviewId, String targetId) {

        final boolean hasLiked = likedReviews.contains(reviewId);

        final Map<Integer, String> resultMap = new HashMap<>();

        reviewsRef.document(reviewId).update("likes", FieldValue.increment(hasLiked ? -1 : 1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        FirebaseFirestore.getInstance().collection("Users")
                                .document(userId)
                                .collection("LikedReviews")
                                .document(targetId)
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {

                                if (snapshot.exists()) {
                                    updateUserLikes(snapshot.getReference(), reviewId, hasLiked, resultMap, userId);
                                } else {

                                    final HashMap<String, Object> likedMap = new HashMap<>();
                                    likedMap.put("LikedReviews", new ArrayList<>());

                                    snapshot.getReference().set(likedMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    updateUserLikes(snapshot.getReference(), reviewId, false, resultMap, userId);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            resultMap.put(hasLiked ? UNLIKE_RESULT_FAILED : LIKE_RESULT_FAILED, reviewId);
                                            resultMap.put(-1, e.getMessage());
                                            setChanged();
                                            notifyObservers(resultMap);

                                        }
                                    });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                resultMap.put(hasLiked ? UNLIKE_RESULT_FAILED : LIKE_RESULT_FAILED, reviewId);
                                resultMap.put(-1, e.getMessage());
                                setChanged();
                                notifyObservers(resultMap);
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("ttt", "failed to inceremetn like count: " + e.getMessage());
                resultMap.put(hasLiked ? UNLIKE_RESULT_FAILED : LIKE_RESULT_FAILED, reviewId);
                resultMap.put(-1, e.getMessage());
                setChanged();
                notifyObservers(resultMap);

            }
        });


    }

    private void updateUserLikes(DocumentReference documentReference, String reviewId, boolean hasLiked,
                                 Map<Integer, String> resultMap, String userId) {

        documentReference.update("LikedReviews",
                hasLiked ? FieldValue.arrayRemove(reviewId) :
                        FieldValue.arrayUnion(reviewId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        resultMap.put(hasLiked ? UNLIKE_RESULT_SUCCESS : LIKE_RESULT_SUCCESS, reviewId);
                        setChanged();
                        notifyObservers(resultMap);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("ttt", "failed to add to users likes: " + e.getMessage());

                reviewsRef.document(reviewId)
                        .update("likes", FieldValue.increment(hasLiked ? 1 : -1));

                resultMap.put(hasLiked ? UNLIKE_RESULT_FAILED : LIKE_RESULT_FAILED, reviewId);
                resultMap.put(-1, e.getMessage());
                setChanged();
                notifyObservers(resultMap);

            }
        });
    }


    public void getUserLikedReviews(String userId, String targetId) {

        Log.d("ttt", "getUserLikedReviews");

        FirebaseFirestore.getInstance().collection("Users")
                .document(userId)
                .collection("LikedReviews")
                .document(targetId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("ttt", "onSuccess liked");
                    likedReviews.addAll((ArrayList<String>) snapshot.get("LikedReviews"));
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    setChanged();
                    notifyObservers(LIKED_REVIEWS_SUCCESS);

                    Log.d("ttt", "complete exits");
                } else {
                    setChanged();
                    notifyObservers(LIKED_REVIEWS_FAILED);

                    Log.d("ttt", "complete doesn;'t exits");
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                setChanged();
                Log.d("ttt", "failed: " + e.getMessage());
                notifyObservers(LIKED_REVIEWS_FAILED);

            }
        });

    }

    public void checkUserHasReviewed() {
        reviewsRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                setChanged();
                notifyObservers(task.getResult().exists() ? ALREADY_REVIEWED : HAS_NOT_REVIEWED);
            }
        });

    }


//
//    public void getReviews(){
//
//
//    }

}
