package com.developers.wajbaty.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.developers.wajbaty.Adapters.SliderAdapter;
import com.developers.wajbaty.R;

import java.io.Serializable;
import java.util.Map;

public class SliderActivity extends AppCompatActivity implements View.OnClickListener {

    private SliderAdapter sliderAdapter;

    //views
    private ViewPager sliderViewPager;
    private Button registerBtn, signInBtn;
    private LinearLayout sliderDotsLinerLayout;
    private TextView skipTv;

    private Map<String, Object> addressMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        getSharedPreferences("Wajbaty", Context.MODE_PRIVATE)
                .edit().putBoolean("notFirstTime", true).apply();

        initializeObjects();

        getViews();

        setUpPager();
    }

    private void initializeObjects() {

        if (getIntent() != null && getIntent().hasExtra("addressMap")) {

            addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");

        } else {

            finish();
            return;
        }

        //pager
        final Integer[] images = new Integer[]{
                R.drawable.slider_order_illustration,
                R.drawable.slider_delivery_illustration,
                R.drawable.slider_restaurant_illustration,
                R.drawable.app_logo_round_icon};

        final String[] titles = new String[]{"Order Online", "Work as a delivery",
                "Register your restaurant"},
                descs = new String[]{"You can order from multiple restaurants at the same time " +
                        "and have it delivered to your doorstep",
                        "You can start working as a delivery driver and get paid in cash by customers",
                        "You can register your restaurant, start getting orders " +
                                "and get more traction for your restaurant"};

        sliderAdapter = new SliderAdapter(this, images, titles, descs);

    }

    private void getViews() {

        sliderViewPager = findViewById(R.id.sliderViewPager);
        registerBtn = findViewById(R.id.registerBtn);
        signInBtn = findViewById(R.id.signInBtn);
        sliderDotsLinerLayout = findViewById(R.id.sliderDotsLinerLayout);
        skipTv = findViewById(R.id.skipTv);

        registerBtn.setOnClickListener(this);
        signInBtn.setOnClickListener(this);
        skipTv.setOnClickListener(this);
        sliderViewPager.setAdapter(sliderAdapter);

    }

    private void setUpPager() {

        final Drawable nonactive_dot = ContextCompat.getDrawable(this, R.drawable.pager_indicator_inactive),
                full_dot = ContextCompat.getDrawable(this, R.drawable.pager_indicator_active);

        final float density = getResources().getDisplayMetrics().density;

        if (sliderAdapter.getCount() > 1) {

//             sliderViewPager.setOffscreenPageLimit(sliderAdapter.getCount() - 1);

            final ImageView[] dots = new ImageView[sliderAdapter.getCount()];

            final LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

            for (int i = 0; i < sliderAdapter.getCount(); i++) {

                dots[i] = new ImageView(this);

                if (i == 0) {
                    dots[0].setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.pager_indicator_active));
                } else {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.pager_indicator_inactive));
                }


                params.setMargins((int) (4 * density), 0, (int) (4 * density), 0);

                sliderDotsLinerLayout.addView(dots[i], params);

            }

            sliderViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                int previousDot = 0;

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {

                    dots[previousDot].setImageDrawable(nonactive_dot);

                    previousDot = position;

                    dots[position].setImageDrawable(full_dot);

                    if (position == sliderAdapter.getCount() - 1) {

                        signInBtn.setVisibility(View.VISIBLE);
                        registerBtn.setVisibility(View.VISIBLE);
                        skipTv.setVisibility(View.INVISIBLE);

                    } else if (registerBtn.getVisibility() == View.VISIBLE) {

                        signInBtn.setVisibility(View.INVISIBLE);
                        registerBtn.setVisibility(View.INVISIBLE);
                        skipTv.setVisibility(View.VISIBLE);
                    }


                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == signInBtn.getId()) {

            startActivity(new Intent(this, SigninActivity.class)
                    .putExtra("addressMap", (Serializable) addressMap));

            finish();
        } else if (v.getId() == registerBtn.getId()) {

            startActivity(new Intent(this, RegisterActivity.class)
                    .putExtra("addressMap", (Serializable) addressMap));

            finish();
        } else if (v.getId() == skipTv.getId()) {

            sliderViewPager.setCurrentItem(sliderAdapter.getCount() - 1, true);

        }


    }
}