package com.developers.wajbaty.PartneredRestaurant.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.developers.wajbaty.Adapters.FragmentsPagerAdapter;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class RestaurantReviewsFragment extends Fragment implements View.OnClickListener {

    private final PartneredRestaurant restaurant;

    //views
    private Button appReviewsBtn;
    private Button googleReviewsBtn;
    private ViewPager2 reviewsViewPager;

    //viewpager
    private FragmentsPagerAdapter tabAdapter;


    public RestaurantReviewsFragment(PartneredRestaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final CollectionReference restaurantRef =
                FirebaseFirestore.getInstance().collection("PartneredRestaurant");

        Fragment[] reviewFragments = new Fragment[]{
                new FirebaseReviewsFragment(restaurantRef.document(restaurant.getID()),
                        restaurantRef.document(restaurant.getID()).collection("Reviews"),
                        restaurant.getReviewSummary()),
                new FirebaseReviewsFragment(restaurantRef.document(restaurant.getID()),
                        restaurantRef.document(restaurant.getID()).collection("Reviews"),
                        restaurant.getReviewSummary())};

        tabAdapter = new FragmentsPagerAdapter(requireActivity(), reviewFragments);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_restaurant_reviews, container, false);
        appReviewsBtn = view.findViewById(R.id.appReviewsBtn);
        googleReviewsBtn = view.findViewById(R.id.googleReviewsBtn);
        reviewsViewPager = view.findViewById(R.id.reviewsViewPager);

        reviewsViewPager.setAdapter(tabAdapter);

        appReviewsBtn.setOnClickListener(this);
        googleReviewsBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onClick(View v) {

        if (v.getId() == appReviewsBtn.getId()) {

            if (reviewsViewPager.getCurrentItem() != 0) {

                reviewsViewPager.setCurrentItem(0);
                appReviewsBtn.setBackgroundResource(R.drawable.option_checked_background_bordered);
                googleReviewsBtn.setBackgroundResource(R.drawable.option_un_checked_background);

            }

        } else if (v.getId() == googleReviewsBtn.getId()) {

            if (reviewsViewPager.getCurrentItem() != 1) {

                reviewsViewPager.setCurrentItem(1);
                googleReviewsBtn.setBackgroundResource(R.drawable.option_checked_background_bordered);
                appReviewsBtn.setBackgroundResource(R.drawable.option_un_checked_background);
            }


        }


    }
}