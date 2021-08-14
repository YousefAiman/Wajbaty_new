package com.developers.wajbaty.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.developers.wajbaty.Models.RestaurantSearchResult;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NearbySearchAdapter extends RecyclerView.Adapter<NearbySearchAdapter.NearbySearchVH> {

    private static NearbySearchListener nearbySearchListener;
    private static int orangeColor;
    private final ArrayList<RestaurantSearchResult> searchResults;

    public NearbySearchAdapter(ArrayList<RestaurantSearchResult> searchResults,
                               NearbySearchListener nearbySearchListener,
                               Context context) {
        this.searchResults = searchResults;
        NearbySearchAdapter.nearbySearchListener = nearbySearchListener;
        orangeColor = ResourcesCompat.getColor(context.getResources(), R.color.orange, null);
    }

    @NonNull
    @Override
    public NearbySearchVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NearbySearchVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NearbySearchVH holder, int position) {

        holder.bind(searchResults.get(position));

        if (position == searchResults.size() - 1) {
            holder.itemView.findViewById(R.id.searchDividerView).setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public interface NearbySearchListener {
        void onSearchResultClicked(int position);
    }

    public static class NearbySearchVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView restaurantSearchImageIv;
        private final TextView restaurantSearchNameTv, restaurantSearchDistanceTv;
        private CircularProgressDrawable progressDrawable;

        public NearbySearchVH(@NonNull View itemView) {
            super(itemView);

            restaurantSearchImageIv = itemView.findViewById(R.id.restaurantSearchImageIv);
            restaurantSearchNameTv = itemView.findViewById(R.id.restaurantSearchNameTv);
            restaurantSearchDistanceTv = itemView.findViewById(R.id.restaurantSearchDistanceTv);

            itemView.setOnClickListener(this);

        }

        private void bind(RestaurantSearchResult searchResult) {

            if (searchResult.getRestaurantImageURL() != null && !searchResult.getRestaurantImageURL().isEmpty()) {


                if (progressDrawable == null) {
                    progressDrawable = new CircularProgressDrawable(itemView.getContext());
                    progressDrawable.setColorSchemeColors(orangeColor);
                    progressDrawable.setStyle(CircularProgressDrawable.LARGE);
                    progressDrawable.start();
                }

                if (!progressDrawable.isRunning()) {
                    progressDrawable.start();
                }


                Picasso.get().load(searchResult.getRestaurantImageURL()).fit().centerCrop()
                        .placeholder(progressDrawable).into(restaurantSearchImageIv);
            }

            restaurantSearchNameTv.setText(searchResult.getRestaurantName());
            restaurantSearchDistanceTv.setText(searchResult.getRestaurantDistance());

//            if(getAdapterPosition() == )
        }

        @Override
        public void onClick(View v) {

            nearbySearchListener.onSearchResultClicked(getAdapterPosition());

        }
    }

}
