package com.developers.wajbaty.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.R;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public class ReviewSummaryAdapter extends RecyclerView.Adapter<ReviewSummaryAdapter.ReviewProgressVH> {

    private int totalReviews;
    private HashMap<String, Long> ratingsMap;
    private String[] keys;

    public ReviewSummaryAdapter(HashMap<String, Long> ratingsMap, int totalReviews) {
        this.setRatingsMap(ratingsMap);
        this.setTotalReviews(totalReviews);

        for (String key : ratingsMap.keySet()) {
            Log.d("ttt", key + " count: " + ratingsMap.get(key));
        }

        updateKeys();


    }


    public void updateKeys() {

        final Set<String> keySet = getRatingsMap().keySet();

        keys = keySet.toArray(new String[5]);

        Arrays.sort(getKeys(), new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.parseInt(o2) - Integer.parseInt(o1);
            }
        });

    }

    @NonNull
    @Override
    public ReviewProgressVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReviewProgressVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating_progress_indicator, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewProgressVH holder, int position) {

        holder.bind(getKeys()[position], getRatingsMap().get(getKeys()[position]));

    }


    @Override
    public int getItemCount() {
        return getRatingsMap().size();
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public HashMap<String, Long> getRatingsMap() {
        return ratingsMap;
    }

    public void setRatingsMap(HashMap<String, Long> ratingsMap) {
        this.ratingsMap = ratingsMap;
    }

    public String[] getKeys() {
        return keys;
    }

    public class ReviewProgressVH extends RecyclerView.ViewHolder {

        private final RatingBar ratingBar;
        private final ProgressBar progressBar;

        public ReviewProgressVH(@NonNull View itemView) {
            super(itemView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        private void bind(String rating, Long count) {

            ratingBar.setNumStars(Integer.parseInt(rating));

            final int progress = (int) (((float) count / getTotalReviews()) * 100f);

            progressBar.setProgress(progress);

            Log.d("ttt", rating + " percentage: " + progress);
        }

    }
}
