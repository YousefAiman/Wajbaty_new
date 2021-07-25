package com.developers.wajbaty.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class FavoriteRestaurantsAdapter extends RecyclerView.Adapter<FavoriteRestaurantsAdapter.FavRestaurantItemVH> {

  private final ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries;
  private static FavoriteRestaurantsListener favoriteRestaurantsListener;

  public interface FavoriteRestaurantsListener{
    void removeFromFav(int position);
    void showRestaurant(int position);
  }

  public FavoriteRestaurantsAdapter(ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries,
                                    FavoriteRestaurantsListener favoriteRestaurantsListener) {
    this.restaurantSummaries = restaurantSummaries;
    FavoriteRestaurantsAdapter.favoriteRestaurantsListener = favoriteRestaurantsListener;
  }

  @NonNull
  @Override
  public FavRestaurantItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new FavRestaurantItemVH(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_favorite_restaurant, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull FavRestaurantItemVH holder, int position) {
    holder.bind(restaurantSummaries.get(position));
  }


  @Override
  public int getItemCount() {
    return restaurantSummaries.size();
  }

  public static class FavRestaurantItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView favoriteRestaurantImageIv,favoriteRestaurantFavIv;
    private final TextView favoriteRestaurantNameTv,favoriteRestaurantStatusTv;

    public FavRestaurantItemVH(@NonNull View itemView) {
      super(itemView);
      favoriteRestaurantImageIv = itemView.findViewById(R.id.favoriteRestaurantImageIv);
      favoriteRestaurantFavIv = itemView.findViewById(R.id.favoriteRestaurantFavIv);
      favoriteRestaurantNameTv = itemView.findViewById(R.id.favoriteRestaurantNameTv);
      favoriteRestaurantStatusTv = itemView.findViewById(R.id.favoriteRestaurantStatusTv);

      itemView.setOnClickListener(this);
      favoriteRestaurantFavIv.setOnClickListener(this);
    }

    private void bind(PartneredRestaurant.PartneredRestaurantSummary restaurantSummary){

      Picasso.get().load(restaurantSummary.getMainImage()).fit().centerCrop().into(favoriteRestaurantImageIv);
      favoriteRestaurantNameTv.setText(restaurantSummary.getName());

      favoriteRestaurantStatusTv.setText(restaurantSummary.getStatus());

    }

    @Override
    public void onClick(View v) {

      if(v.getId() == favoriteRestaurantFavIv.getId()){

        favoriteRestaurantsListener.removeFromFav(getAdapterPosition());

      }else {

        favoriteRestaurantsListener.showRestaurant(getAdapterPosition());

      }

    }
  }



}
