package com.developers.wajbaty.Adapters;

import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.UserReview;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final byte TYPE_REVIEWED = 1, TYPE_UN_REVIEWED = 2;
    private static final int OUTLINED_HEART = 1, FILLED_HEART = 2;

    private static String currentUid;
    private static ReviewClickListener reviewClickListener;
    private static CollectionReference usersRef;
    private final ArrayList<UserReview> userReviews;
    private final List<String> userLikedReviews;
    private boolean userHasReviewed;

    public ReviewsAdapter(ArrayList<UserReview> userReviews,
                          ReviewClickListener reviewClickListener,
                          List<String> userLikedReviews) {
        this.userReviews = userReviews;
        ReviewsAdapter.reviewClickListener = reviewClickListener;
        this.userLikedReviews = userLikedReviews;
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("ttt", "reviews adapter created");
    }

    private static void fillUserInfo(UserReview review, String userId, ReviewUserVh holder) {

        if (usersRef == null)
            usersRef = FirebaseFirestore.getInstance().collection("Users");

        usersRef.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if (snapshot.exists()) {

                    review.setReviewerImageUrl(snapshot.getString("userImageUrl"));
                    Picasso.get().load(review.getReviewerImageUrl()).fit().centerCrop().into(holder.reviewUserIv);

                    review.setReviewerUsername(snapshot.getString("name"));
                    holder.reviewUserNameTv.setText(review.getReviewerUsername());

                }
            }
        });

    }

    public List<String> getUserLikedReviews() {
        return userLikedReviews;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_UN_REVIEWED) {

            Log.d("ttt", "item type unreviewd created");
            return new AddReviewUserVh(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_add_user_review, parent, false));
        }


        Log.d("ttt", "item type reviewd created");

        return new ReviewUserVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_review, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        if (holder.getItemViewType() == TYPE_UN_REVIEWED) {
            ((AddReviewUserVh) holder).bind(userReviews.get(position));
            return;
        }


        final UserReview userReview = userReviews.get(position);

        ((ReviewUserVh) holder).bind(userReview,
                getUserLikedReviews().contains(userReview.getReviewerId()));


    }


    @Override
    public int getItemViewType(int position) {

        if (position == 0 && !isUserHasReviewed()) {

            Log.d("ttt", "item type unreviewd");
            return TYPE_UN_REVIEWED;

        }

        Log.d("ttt", "item type reviewd");

        return TYPE_REVIEWED;

    }

    @Override
    public int getItemCount() {
        return userReviews.size();
    }

    public boolean isUserHasReviewed() {
        return userHasReviewed;
    }

    public void setUserHasReviewed(boolean userHasReviewed) {
        this.userHasReviewed = userHasReviewed;
    }


    public interface ReviewClickListener {

        void likeReview(int position);

        void addReview(byte rating, String comment);

    }

    public static class ReviewUserVh extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView reviewUserIv, reviewFavIv;
        private final RatingBar reviewRatingBar;
        private final TextView reviewUserNameTv, reviewCommentTv;

        public ReviewUserVh(@NonNull View itemView) {
            super(itemView);

            reviewUserIv = itemView.findViewById(R.id.reviewUserIv);
            reviewFavIv = itemView.findViewById(R.id.reviewFavIv);
            reviewRatingBar = itemView.findViewById(R.id.reviewRatingBar);
            reviewUserNameTv = itemView.findViewById(R.id.reviewUserNameTv);
            reviewCommentTv = itemView.findViewById(R.id.reviewCommentTv);
            reviewFavIv.setOnClickListener(this);
            reviewFavIv.setTag(OUTLINED_HEART);
        }

        private void bind(UserReview review, boolean hasLiked) {

            if (review.getReviewerUsername() == null)
                fillUserInfo(review, review.getReviewerId(), this);

            reviewRatingBar.setRating(review.getRating());
            reviewCommentTv.setText(review.getComment());

            if (review.getReviewerId().equals(currentUid)) {

                reviewFavIv.setVisibility(View.GONE);
                reviewFavIv.setOnClickListener(null);

            } else {

                final int tag = ((int) reviewFavIv.getTag());

                if (hasLiked && tag != FILLED_HEART) {
                    Log.d("ttt", "making heart filled");
                    reviewFavIv.setImageResource(R.drawable.heart_filled_icon);
                    reviewFavIv.setTag(FILLED_HEART);
                } else if (!hasLiked && tag != OUTLINED_HEART) {
                    Log.d("ttt", "making heart outlined");
                    reviewFavIv.setImageResource(R.drawable.heart_outlined_icon);
                    reviewFavIv.setTag(OUTLINED_HEART);
                }
            }

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == reviewFavIv.getId()) {
                reviewClickListener.likeReview(getAdapterPosition());
            }
        }
    }

    public static class AddReviewUserVh extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView reviewSubmitIv, reviewUserIv;
        private final RatingBar reviewRatingBar;
        private final EditText reviewCommentEd;
        private final TextView reviewUserNameTv;


        public AddReviewUserVh(@NonNull View itemView) {
            super(itemView);

            reviewSubmitIv = itemView.findViewById(R.id.reviewSubmitIv);
            reviewUserIv = itemView.findViewById(R.id.reviewUserIv);
            reviewRatingBar = itemView.findViewById(R.id.reviewRatingBar);
            reviewCommentEd = itemView.findViewById(R.id.reviewCommentEd);
            reviewUserNameTv = itemView.findViewById(R.id.reviewUserNameTv);

            reviewSubmitIv.setOnClickListener(this);
            reviewCommentEd.addTextChangedListener(new ReviewTextWatcher(reviewSubmitIv, itemView.getResources()));
        }

        public void bind(UserReview userReview) {

            if (reviewUserNameTv.getText().toString().isEmpty()) {

                FirebaseFirestore.getInstance().collection("Users")
                        .document(userReview.getReviewerId())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        if (snapshot.exists()) {

                            if (snapshot.contains("imageURL")) {


                                Picasso.get().load(snapshot.getString("imageURL")).fit().centerCrop().into(reviewUserIv);

                            }

                            if (snapshot.contains("name")) {
                                reviewUserNameTv.setText(snapshot.getString("name"));
                            }
                        }

                    }
                });

            }

        }

        @Override
        public void onClick(View v) {
            if (v.getId() == reviewSubmitIv.getId()) {

                reviewCommentEd.clearFocus();
                reviewClickListener.addReview((byte) reviewRatingBar.getRating(), reviewCommentEd.getText().toString());

            }
        }
    }

    private static class ReviewTextWatcher implements TextWatcher {

        private final ImageView checkIv;
        private final int activeColor, inactiveColor;
        private boolean inactive = true;

        public ReviewTextWatcher(ImageView checkIv, Resources resources) {
            this.checkIv = checkIv;
            activeColor = ResourcesCompat.getColor(resources, R.color.orange, null);
            inactiveColor = ResourcesCompat.getColor(resources, R.color.transparent_orange, null);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (inactive && s.length() > 0) {
                DrawableCompat.setTint(
                        DrawableCompat.wrap(checkIv.getDrawable()),
                        activeColor
                );

                inactive = false;
            } else if (!inactive && s.length() == 0) {

                DrawableCompat.setTint(
                        DrawableCompat.wrap(checkIv.getDrawable()),
                        inactiveColor
                );

                inactive = true;
            }

        }


        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}
