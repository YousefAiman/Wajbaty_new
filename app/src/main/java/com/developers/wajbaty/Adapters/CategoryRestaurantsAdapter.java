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

import java.util.ArrayList;
import java.util.List;

public class CategoryRestaurantsAdapter extends RecyclerView.Adapter<CategoryRestaurantsAdapter.CategoryRestaurantItemVH> {

  private static final int OUTLINED_HEART = 1,FILLED_HEART = 2;


  private final ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries;
  private static CategoryRestaurantsListener categoryRestaurantsListener;
  private final List<String> likedMenuItems;

  public interface CategoryRestaurantsListener{
    void addOrRemoveFromFav(int position);
    void showRestaurant(int position);
  }

  public CategoryRestaurantsAdapter(ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries,
                                    CategoryRestaurantsListener categoryRestaurantsListener,
                                    List<String> likedMenuItems) {
    this.restaurantSummaries = restaurantSummaries;
    CategoryRestaurantsAdapter.categoryRestaurantsListener = categoryRestaurantsListener;
    this.likedMenuItems = likedMenuItems;
  }

  @NonNull
  @Override
  public CategoryRestaurantItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new CategoryRestaurantItemVH(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_category_restaurant, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull CategoryRestaurantItemVH holder, int position) {
    PartneredRestaurant.PartneredRestaurantSummary summary =
            restaurantSummaries.get(position);
    holder.bind(summary,
            likedMenuItems != null && likedMenuItems.contains(summary.getID()));
  }


  @Override
  public int getItemCount() {
    return restaurantSummaries.size();
  }

  public static class CategoryRestaurantItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView categoryRestaurantImageIv,categoryRestaurantFavIv;
    private final TextView categoryRestaurantNameTv,categoryRestaurantStatusTv;

    public CategoryRestaurantItemVH(@NonNull View itemView) {
      super(itemView);
      categoryRestaurantImageIv = itemView.findViewById(R.id.categoryRestaurantImageIv);
      categoryRestaurantFavIv = itemView.findViewById(R.id.categoryRestaurantFavIv);
      categoryRestaurantNameTv = itemView.findViewById(R.id.categoryRestaurantNameTv);
      categoryRestaurantStatusTv = itemView.findViewById(R.id.categoryRestaurantStatusTv);

      itemView.setOnClickListener(this);
      categoryRestaurantFavIv.setOnClickListener(this);
      categoryRestaurantFavIv.setTag(OUTLINED_HEART);

    }

    private void bind(PartneredRestaurant.PartneredRestaurantSummary restaurantSummary,
                      boolean isFavored){

      Picasso.get().load(restaurantSummary.getMainImage()).fit().centerCrop().into(categoryRestaurantImageIv);
      categoryRestaurantNameTv.setText(restaurantSummary.getName());
      categoryRestaurantStatusTv.setText(restaurantSummary.getStatus());



      final int tag = ((int)categoryRestaurantFavIv.getTag());

      if(isFavored && tag != FILLED_HEART){
        Log.d("ttt","making heart filled");
        categoryRestaurantFavIv.setImageResource(R.drawable.heart_filled_icon);
        categoryRestaurantFavIv.setTag(FILLED_HEART);
      }else if(!isFavored && tag != OUTLINED_HEART){
        Log.d("ttt","making heart outlined");
        categoryRestaurantFavIv.setImageResource(R.drawable.heart_outlined_icon);
        categoryRestaurantFavIv.setTag(OUTLINED_HEART);
      }

    }

    @Override
    public void onClick(View v) {

      if(v.getId() == categoryRestaurantFavIv.getId()){

        categoryRestaurantsListener.addOrRemoveFromFav(getAdapterPosition());

      }else {

        categoryRestaurantsListener.showRestaurant(getAdapterPosition());

      }

    }
  }



}
