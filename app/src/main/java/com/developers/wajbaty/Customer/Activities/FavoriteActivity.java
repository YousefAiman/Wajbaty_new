package com.developers.wajbaty.Customer.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.developers.wajbaty.Adapters.FragmentsPagerAdapter;
import com.developers.wajbaty.Customer.Fragments.FavoriteMenuItemsFragment;
import com.developers.wajbaty.Customer.Fragments.FavoriteRestaurantsFragment;
import com.developers.wajbaty.R;

public class FavoriteActivity extends AppCompatActivity implements View.OnClickListener {


    private Button favRestaurantBtn, favMenuItemsBtn;
    private ViewPager2 favoritesViewPager;


    //pager
    private FragmentsPagerAdapter pagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initializeObjects();
        getViews();

    }


    private void initializeObjects() {

        final Fragment[] fragments = {new FavoriteRestaurantsFragment(), new FavoriteMenuItemsFragment()};
        pagerAdapter = new FragmentsPagerAdapter(this, fragments);

    }


    private void getViews() {

        //views
        Toolbar favoriteToolbar = findViewById(R.id.favoriteToolbar);
        favoritesViewPager = findViewById(R.id.favoritesViewPager);
        favRestaurantBtn = findViewById(R.id.favRestaurantBtn);
        favMenuItemsBtn = findViewById(R.id.favMenuItemsBtn);


        favoriteToolbar.setNavigationOnClickListener(v -> finish());
        favoritesViewPager.setAdapter(pagerAdapter);
        favRestaurantBtn.setOnClickListener(this);
        favMenuItemsBtn.setOnClickListener(this);

        favoritesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (position == 0) {
                    favRestaurantBtn.setBackgroundResource(R.drawable.option_checked_background_bordered);
                    favMenuItemsBtn.setBackgroundResource(R.drawable.option_un_checked_background);
                } else {
                    favMenuItemsBtn.setBackgroundResource(R.drawable.option_checked_background_bordered);
                    favRestaurantBtn.setBackgroundResource(R.drawable.option_un_checked_background);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }


    @Override
    public void onClick(View v) {

        if (v.getId() == favRestaurantBtn.getId()) {

            if (favoritesViewPager.getCurrentItem() != 0) {

                favoritesViewPager.setCurrentItem(0);
                favRestaurantBtn.setBackgroundResource(R.drawable.option_checked_background_bordered);
                favMenuItemsBtn.setBackgroundResource(R.drawable.option_un_checked_background);

            }

        } else if (v.getId() == favMenuItemsBtn.getId()) {

            if (favoritesViewPager.getCurrentItem() != 1) {

                favoritesViewPager.setCurrentItem(1);
                favMenuItemsBtn.setBackgroundResource(R.drawable.option_checked_background_bordered);
                favRestaurantBtn.setBackgroundResource(R.drawable.option_un_checked_background);
            }


        }

    }
}