package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.RestaurantSearchResult;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NearbySearchAdapter extends RecyclerView.Adapter<NearbySearchAdapter.NearbySearchVH> {

    private final ArrayList<RestaurantSearchResult> searchResults;
    private static NearbySearchListener nearbySearchListener;

    public NearbySearchAdapter(ArrayList<RestaurantSearchResult> searchResults,NearbySearchListener nearbySearchListener) {
        this.searchResults = searchResults;
        NearbySearchAdapter.nearbySearchListener = nearbySearchListener;
    }

    public interface NearbySearchListener{
        void onSearchResultClicked(int position);
    }

    @NonNull
    @Override
    public NearbySearchVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NearbySearchVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant_search_result,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NearbySearchVH holder, int position) {

        holder.bind(searchResults.get(position));

        if(position == searchResults.size()-1){
            holder.itemView.findViewById(R.id.searchDividerView).setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public static class NearbySearchVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView restaurantSearchImageIv;
        private final TextView restaurantSearchNameTv,restaurantSearchDistanceTv;

        public NearbySearchVH(@NonNull View itemView) {
            super(itemView);

            restaurantSearchImageIv = itemView.findViewById(R.id.restaurantSearchImageIv);
            restaurantSearchNameTv = itemView.findViewById(R.id.restaurantSearchNameTv);
            restaurantSearchDistanceTv = itemView.findViewById(R.id.restaurantSearchDistanceTv);

            itemView.setOnClickListener(this);

        }

        private void bind(RestaurantSearchResult searchResult){

            if(searchResult.getRestaurantImageURL()!=null && !searchResult.getRestaurantImageURL().isEmpty()){
                Picasso.get().load(searchResult.getRestaurantImageURL()).fit().centerCrop().into(restaurantSearchImageIv);
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
