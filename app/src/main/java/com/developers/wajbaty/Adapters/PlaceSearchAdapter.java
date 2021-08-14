package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.PlaceSearchResult;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PlaceSearchAdapter extends RecyclerView.Adapter<PlaceSearchAdapter.PlaceSearchVH> {

    private static PlaceSearchListener placeSearchListener;
    private final ArrayList<PlaceSearchResult> searchResults;

    public PlaceSearchAdapter(ArrayList<PlaceSearchResult> searchResults, PlaceSearchListener placeSearchListener) {
        this.searchResults = searchResults;
        PlaceSearchAdapter.placeSearchListener = placeSearchListener;
    }

    @NonNull
    @Override
    public PlaceSearchVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceSearchVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceSearchVH holder, int position) {

        holder.bind(searchResults.get(position));

        final View searchDividerView = holder.itemView.findViewById(R.id.searchDividerView);
        if (position == searchResults.size() - 1) {
            if (searchDividerView.getVisibility() == View.VISIBLE) {
                searchDividerView.setVisibility(View.GONE);
            }
        } else if (searchDividerView.getVisibility() == View.GONE) {
            searchDividerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public interface PlaceSearchListener {
        void onSearchResultClicked(int position);
    }

    public static class PlaceSearchVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView placeSearchImageIv;
        private final TextView placeSearchNameTv, placeSearchAddressTv;

        public PlaceSearchVH(@NonNull View itemView) {
            super(itemView);

            placeSearchImageIv = itemView.findViewById(R.id.placeSearchImageIv);
            placeSearchNameTv = itemView.findViewById(R.id.placeSearchNameTv);
            placeSearchAddressTv = itemView.findViewById(R.id.placeSearchAddressTv);

            itemView.setOnClickListener(this);

        }

        private void bind(PlaceSearchResult searchResult) {

            if (searchResult.getImageURL() != null && !searchResult.getImageURL().isEmpty()) {
                Picasso.get().load(searchResult.getImageURL()).fit().centerCrop().into(placeSearchImageIv);
            }

            placeSearchNameTv.setText(searchResult.getName());
            placeSearchAddressTv.setText(searchResult.getFormattedAddress());

//            if(getAdapterPosition() == )
        }

        @Override
        public void onClick(View v) {

            placeSearchListener.onSearchResultClicked(getAdapterPosition());

        }
    }

}
