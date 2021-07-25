package com.developers.wajbaty.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.Models.offer.DiscountOffer;
import com.developers.wajbaty.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RestaurantsPagerAdapter extends PagerAdapter {

    private final ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries;
    private final RestaurantClickListener restaurantClickListener;

    public interface RestaurantClickListener{
        void onRestaurantClicked(int position);
    }

    public RestaurantsPagerAdapter(ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries,
                                   RestaurantClickListener restaurantClickListener){
        this.restaurantSummaries = restaurantSummaries;
        this.restaurantClickListener = restaurantClickListener;
    }

    @Override
    public int getCount() {
        return restaurantSummaries.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
       final View view = LayoutInflater.from(container.getContext())
               .inflate(R.layout.item_restaurant_card, null);

       final PartneredRestaurant.PartneredRestaurantSummary restaurantSummary
               = restaurantSummaries.get(position);

       final ImageView restaurantCardImageIv = view.findViewById(R.id.restaurantCardImageIv);
        final TextView restaurantCardNameTv = view.findViewById(R.id.restaurantCardNameTv);

        Picasso.get().load(restaurantSummary.getMainImage()).fit().centerCrop().into(restaurantCardImageIv);
        restaurantCardNameTv.setText(restaurantSummary.getName());

        view.setOnClickListener(v-> restaurantClickListener.onRestaurantClicked(position));
        container.addView(view);

       return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}