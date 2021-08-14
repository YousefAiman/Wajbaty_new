package com.developers.wajbaty.PartneredRestaurant.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.ReviewSummaryAdapter;
import com.developers.wajbaty.Adapters.ReviewsAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.ReviewSummary;
import com.developers.wajbaty.Models.UserReview;
import com.developers.wajbaty.Models.UserReviewModel;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


public class FirebaseReviewsFragment extends Fragment implements ReviewsAdapter.ReviewClickListener,
        Observer {

    private static final String TAG = "FirebaseReviews";
    private static final int REVIEWS_PAGE_LIMIT = 8;

    private final CollectionReference reviewsRef;
    private final DocumentReference reviewParentRef;
    private ProgressDialogFragment progressDialog;

    //views
    private RecyclerView reviewsRv, ratingsRv;
    private TextView ratingTv, ratingsCountTv, reviewSummaryTv, reviewsEmptyTv;
    private RatingBar averageRatingBar;
    private View reviewsSeperator;

    //reviews
    private ArrayList<UserReview> reviews;
    private ReviewsAdapter adapter;
    private DocumentSnapshot lastReviewSnapshot;
    private Query reviewsQuery;
    private boolean isLoadingReviews;
    private ReviewsScrollListener scrollListener;

    //review summary
    private ReviewSummaryAdapter reviewSummaryAdapter;

    private String userId;

    private UserReviewModel reviewModel;
    private ReviewSummary reviewSummary;

    public FirebaseReviewsFragment(DocumentReference reviewParentRef, CollectionReference reviewsRef,
                                   ReviewSummary reviewSummary) {
        this.reviewParentRef = reviewParentRef;
        this.reviewsRef = reviewsRef;

        this.reviewSummary = reviewSummary;
    }

    public static void main(String[] args) {

//        HashMap<String,Integer> ratingsMap = new HashMap<>();
//        ratingsMap.put("1",0);
//        ratingsMap.put("2",0);
//        ratingsMap.put("3",0);
//        ratingsMap.put("4",2);
//        ratingsMap.put("5",0);
//
//
//
//        final Set<String> keySet = ratingsMap.keySet();
//
//        String[] keys = keySet.toArray(new String[0]);
//
////    String [] keys = (String[]) ratingsMap.keySet().toArray();
////    Log.d("ttt","keys: "+ Arrays.toString(keys));
//
//        Arrays.sort(keys, new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2) {
//                return Integer.parseInt(o2) - Integer.parseInt(o1);
//            }
//        });
//
//        System.out.println(Arrays.toString(keys));

//        System.out.println((int)1 == (byte)1);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reviewModel = new UserReviewModel(reviewsRef);
        reviewModel.addObserver(this);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reviews = new ArrayList<>();

//        checkUserHasReviewed();
//        reviewModel.getReviewSummary();
//        if(reviewSummary == null){
//
//            final HashMap<Byte,Integer> emptyRatingMap = new HashMap<>();
//
//            for(int i=1;i<=5;i++){
//                emptyRatingMap.put((byte) i,0);
//            }
//
//            reviewSummary = new ReviewSummary(0,emptyRatingMap,0);
//
//        }

        if (reviewSummary != null) {
            reviewSummaryAdapter = new ReviewSummaryAdapter(reviewSummary.getRatingsMap(),
                    reviewSummary.getTotalReviews());
        }

        reviewsQuery = reviewsRef
                .orderBy("reviewerId")
                .whereNotEqualTo("reviewerId", userId)
                .orderBy("ratingTime", Query.Direction.DESCENDING)
                .limit(REVIEWS_PAGE_LIMIT);
//        adapter = new ReviewsAdapter(reviews,this,reviewModel.getLikedReviews());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_reviews, container, false);

//        container.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
//        ((NestedScrollView)view.findViewById(R.id.reviewsNestedScrollView)).setNestedScrollingEnabled(false);

        reviewsRv = view.findViewById(R.id.reviewsRv);

//        reviewsRv.setNestedScrollingEnabled(false);

        ratingsRv = view.findViewById(R.id.ratingsRv);
        ratingTv = view.findViewById(R.id.ratingTv);
        reviewSummaryTv = view.findViewById(R.id.reviewSummaryTv);
        ratingsCountTv = view.findViewById(R.id.ratingsCountTv);
        averageRatingBar = view.findViewById(R.id.averageRatingBar);
        reviewsEmptyTv = view.findViewById(R.id.reviewsEmptyTv);
        reviewsSeperator = view.findViewById(R.id.reviewsSeperator);

//        reviewsRv.setAdapter(adapter);
        if (ratingsRv.getAdapter() == null && reviewSummaryAdapter != null) {
            ratingsRv.setAdapter(reviewSummaryAdapter);
        }

        if (reviewsRv.getAdapter() == null) {
            reviewsRv.setAdapter(adapter);
        }

        if (reviewSummary == null) {
            reviewSummaryTv.setVisibility(View.GONE);
            ratingTv.setVisibility(View.GONE);
            ratingsCountTv.setVisibility(View.GONE);
            averageRatingBar.setVisibility(View.GONE);
            ratingsRv.setVisibility(View.GONE);
            reviewsSeperator.setVisibility(View.GONE);
            reviewsEmptyTv.setVisibility(View.VISIBLE);

            reviewsEmptyTv.setVisibility(View.VISIBLE);
        } else {
            fillReviewSummary();
        }

        return view;
    }

//
//    private void checkUserHasReviewed(){
//
//        reviewsRef.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot snapshot) {
//                adapter.setUserHasReviewed(snapshot.exists());
//            }
//        });
//
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reviewModel.getUserLikedReviews(userId, reviewParentRef.getId());

    }

    @Override
    public void likeReview(int position) {

        showProgressDialog();

        reviewModel.likeReview(userId, reviews.get(position).getReviewerId(), reviewParentRef.getId());

    }

    @Override
    public void addReview(byte rating, String comment) {

        if (rating == 0) {
            Toast.makeText(requireContext(), "Please add a rating!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(requireContext(), "Please add a review!", Toast.LENGTH_SHORT).show();
            return;
        }


        showProgressDialog();

        reviewModel.addReview(comment, rating);

    }

    private void showProgressDialog() {


        if (progressDialog == null) {
            progressDialog = new ProgressDialogFragment();
            progressDialog.show(getChildFragmentManager(), "progress");
        } else {

            Fragment oldFragment = getChildFragmentManager().findFragmentByTag("progress");

            if (oldFragment != null && progressDialog.isAdded()) {
                return;
            }

            if (isAdded()) {
                progressDialog.show(getChildFragmentManager(), "progress");
            }

        }

    }

    private void hideProgressDialog() {

        if (progressDialog != null) {

            FragmentManager manager = getChildFragmentManager();

            Fragment fragment = manager.findFragmentByTag("progress");

            if (fragment != null) {
                manager.beginTransaction().remove(fragment).commit();
            }

            progressDialog.dismiss();
        }
    }

    private void fillReviewSummary() {

        if (reviewSummaryTv.getVisibility() == View.GONE) {
            reviewSummaryTv.setVisibility(View.VISIBLE);
            ratingTv.setVisibility(View.VISIBLE);
            ratingsCountTv.setVisibility(View.VISIBLE);
            averageRatingBar.setVisibility(View.VISIBLE);
            ratingsRv.setVisibility(View.VISIBLE);
            reviewsSeperator.setVisibility(View.VISIBLE);
            reviewsEmptyTv.setVisibility(View.GONE);
        }


        ratingTv.setText(String.valueOf(reviewSummary.getAverageRating()));
        ratingsCountTv.setText(reviewSummary.getTotalReviews() +
                (reviewSummary.getTotalReviews() == 1 ? " Review" : " Reviews"));
        averageRatingBar.setRating(reviewSummary.getAverageRating());


    }

    @Override
    public void update(Observable o, Object arg) {

        Log.d("ttt", "updated");

        if (arg instanceof Integer) {

            int result = (int) arg;

            if (result == UserReviewModel.LIKED_REVIEWS_SUCCESS ||
                    result == UserReviewModel.LIKED_REVIEWS_FAILED) {

                Log.d("ttt", "liked reviews eneded");
//                checkUserHasReviewed();

                switch ((int) arg) {

                    case UserReviewModel.LIKED_REVIEWS_SUCCESS:

                        adapter = new ReviewsAdapter(reviews, FirebaseReviewsFragment.this,
                                reviewModel.getLikedReviews());

                        break;
                    case UserReviewModel.LIKED_REVIEWS_FAILED:
                        Log.d("ttt", "liked reviews result failed");

                        adapter = new ReviewsAdapter(reviews, FirebaseReviewsFragment.this,
                                new ArrayList<>());


                        break;
                }

                if (reviewsRv != null) {
                    reviewsRv.setAdapter(adapter);
                }

                reviewsRef.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        if (snapshot.exists()) {
                            reviews.add(snapshot.toObject(UserReview.class));
                            adapter.setUserHasReviewed(true);
                        } else if (GlobalVariables.getCurrentRestaurantId() == null) {
                            reviews.add(new UserReview(userId, (byte) 0, 0, null));
                            adapter.setUserHasReviewed(false);
                        }

                        Log.d("ttt", "snapshot.exists(): " + snapshot.exists());
                        Log.d("ttt", "checking liked onSuccess");

//                        adapter.setUserHasReviewed(snapshot.exists());
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        getReviews(true);

                        Log.d("ttt", "checking liked ended");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressDialog();
                    }
                });

            }

        } else if (arg instanceof Map) {

            hideProgressDialog();

            final Map<Integer, String> resultMap = (Map<Integer, String>) arg;

            final Integer key = resultMap.keySet().iterator().next();
            final String reviewId = resultMap.get(key);

            int targetPosition = -1;

            for (int i = 0; i < reviews.size(); i++) {
                if (reviews.get(i).getReviewerId().equals(reviewId)) {
                    targetPosition = i;
                    break;
                }
            }

            if (targetPosition == -1)
                return;

            switch (key) {

                case UserReviewModel.LIKE_RESULT_SUCCESS:

                    reviewModel.getLikedReviews().add(reviewId);
                    adapter.getUserLikedReviews().add(reviewId);
                    adapter.notifyItemChanged(targetPosition);

                    break;
                case UserReviewModel.LIKE_RESULT_FAILED:

                    Toast.makeText(requireContext(),
                            "Liking review failed! Please try again", Toast.LENGTH_LONG).show();

                    Log.d(TAG, "failed liking review: " + resultMap.get(-1));

//                    reviewModel.getLikedReviews().add(reviewId);
//                    adapter.notifyItemChanged(targetPosition);

                    break;
                case UserReviewModel.UNLIKE_RESULT_SUCCESS:

                    reviewModel.getLikedReviews().remove(reviewId);
                    adapter.getUserLikedReviews().remove(reviewId);
                    adapter.notifyItemChanged(targetPosition);

                    break;
                case UserReviewModel.UNLIKE_RESULT_FAILED:

                    Toast.makeText(requireContext(),
                            "Removing like on review failed! Please try again", Toast.LENGTH_LONG).show();

                    Log.d(TAG, "failed unliking review: " + resultMap.get(-1));

                    break;

            }

        } else if (arg instanceof UserReview) {

            hideProgressDialog();

            reviews.set(0, (UserReview) arg);
            adapter.setUserHasReviewed(true);
            adapter.notifyItemChanged(0);

        } else if (arg instanceof ReviewSummary) {

            reviewSummary = (ReviewSummary) arg;

//            ratingTv.setText(String.valueOf(reviewSummary.getAverageRating()));
//            ratingsCountTv.setText(reviewSummary.getTotalReviews() + " Reviews");
//            averageRatingBar.setRating(reviewSummary.getAverageRating());
//
            fillReviewSummary();

            if (reviewSummaryAdapter == null) {
                reviewSummaryAdapter = new ReviewSummaryAdapter(reviewSummary.getRatingsMap(), reviewSummary.getTotalReviews());
                ratingsRv.setAdapter(reviewSummaryAdapter);
            } else {

                reviewSummaryAdapter.setRatingsMap(reviewSummary.getRatingsMap());
                reviewSummaryAdapter.setTotalReviews(reviewSummary.getTotalReviews());
                reviewSummaryAdapter.updateKeys();
//                reviewSummaryAdapter = new ReviewSummaryAdapter(reviewSummary.getRatingsMap(),reviewSummary.getTotalReviews());
                reviewSummaryAdapter.notifyDataSetChanged();
            }


        } else if (arg instanceof String) {

            Toast.makeText(getContext(),
                    "Failed while adding your review! Please try again", Toast.LENGTH_LONG).show();

            Log.d(TAG, "failed to publish review because: " + (String) arg);
        }

//        if(arg instanceof List){
//
//
//        }else if(arg instanceof ){
//
//        }

    }

    private void getReviews(boolean isInitial) {

        isLoadingReviews = true;

        showProgressDialog();

        Query currentQuery = reviewsQuery;
        if (lastReviewSnapshot != null) {
            currentQuery = currentQuery.startAfter(lastReviewSnapshot);
        }
//        if(category!=null && !category.isEmpty()){
//            currentQuery = currentQuery.whereEqualTo("category",category);
//        }

        currentQuery.get().addOnSuccessListener(snapshots -> {

            if (!snapshots.isEmpty()) {

//                if(ratingsRv.getVisibility() == View.INVISIBLE){
//                    restaurantMenuRv.setVisibility(View.VISIBLE);
//                }


                lastReviewSnapshot = snapshots.getDocuments().get(snapshots.size() - 1);

                Log.d("ttt", "snapshots not empty");

                if (isInitial) {
//                    restaurantMenuRv.setVisibility(View.VISIBLE);
                    reviews.addAll(1, snapshots.toObjects(UserReview.class));
                } else {
                    reviews.addAll(reviews.size() - 1, snapshots.toObjects(UserReview.class));
                }
            }
//            else if(restaurantMenuRv.getVisibility() == View.VISIBLE){
//
//                restaurantMenuRv.setVisibility(View.INVISIBLE);
//
//            }

        }).addOnCompleteListener(task -> {

            Log.d("ttt", "addOnCompleteListener");


            if (task.isSuccessful() && task.getResult() != null) {
                int size = task.getResult().size();

                Log.d("ttt", "task succesfull size: " + size);

                if (isInitial) {
//                        restaurantMenuRv.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    if (reviews.size() > 1) {
                        if (reviews.size() - 1 == REVIEWS_PAGE_LIMIT && scrollListener == null) {
                            reviewsRv.addOnScrollListener(scrollListener = new ReviewsScrollListener());
                        }

                    }
                } else {

                    if (!task.getResult().isEmpty()) {

                        adapter.notifyItemRangeInserted(
                                reviews.size() - size, size);

                        if (task.getResult().size() < REVIEWS_PAGE_LIMIT && scrollListener != null) {
                            reviewsRv.removeOnScrollListener(scrollListener);
                        }
                    } else if (scrollListener != null) {

                        reviewsRv.removeOnScrollListener(scrollListener);
                    }
                }
            } else {
                Log.d("ttt", "task failed");
            }

//            if(reviews.isEmpty() && noMenuItemTv.getVisibility() == View.GONE){
//                noMenuItemTv.setVisibility(View.VISIBLE);
//            }else if(!menuItems.isEmpty() && noMenuItemTv.getVisibility() == View.VISIBLE){
//                noMenuItemTv.setVisibility(View.GONE);
//            }

            isLoadingReviews = false;
            hideProgressDialog();

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("ttt", "failed: " + e.getMessage());
                hideProgressDialog();
            }
        });


    }

    private class ReviewsScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingReviews &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at bottom");

                getReviews(false);

            }
        }
    }
}