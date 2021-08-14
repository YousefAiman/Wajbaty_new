package com.developers.wajbaty.PartneredRestaurant.Fragments;

import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.developers.wajbaty.Adapters.AlbumAdapter;
import com.developers.wajbaty.Adapters.ImagePagerAdapter;
import com.developers.wajbaty.Adapters.WorkingScheduleAdapter;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.FullScreenImagesUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;


public class RestaurantInfoFragment extends Fragment implements AlbumAdapter.AlbumClickListener {

    private final PartneredRestaurant restaurant;
    private final int status;
    private ImagePagerAdapter imagePagerAdapter;
    //views
    private ViewPager viewPager;
    private LinearLayout dotsLinear, contactInfoLl, SocialMediaLl;
    private TextView addressTv, statusTv, serviceOptionsTv, scheduleTv, aboutTv, additionalServicesTv;
    private RecyclerView restaurantAlbumRv;

    //album
    private AlbumAdapter albumAdapter;

    private float density;
    private int lightBlack;

    public RestaurantInfoFragment(PartneredRestaurant restaurant, int status) {
        this.restaurant = restaurant;
        this.status = status;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (restaurant.getBannerImages() != null && !restaurant.getBannerImages().isEmpty()) {
            imagePagerAdapter = new ImagePagerAdapter(restaurant.getBannerImages(), R.layout.card_item_image_page, 0.8f, requireContext());
        }

        if (restaurant.getAlbumImages() != null && !restaurant.getAlbumImages().isEmpty()) {
            albumAdapter = new AlbumAdapter(restaurant.getAlbumImages(), this, requireContext());
        }

        density = getResources().getDisplayMetrics().density;

//        DocumentReference restaurantRef = FirebaseFirestore.getInstance().document(restaurant.getID());

//        restaurantRef.collection("Lists").document()


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant_info, container, false);

//        ((NestedScrollView)view.findViewById(R.id.restaurantNestedScrollView)).setNestedScrollingEnabled(true);

        viewPager = view.findViewById(R.id.restaurantBannerViewPager);
        dotsLinear = view.findViewById(R.id.dotsLinear);
        addressTv = view.findViewById(R.id.addressTv);
        statusTv = view.findViewById(R.id.statusTv);
        serviceOptionsTv = view.findViewById(R.id.serviceOptionsTv);
        scheduleTv = view.findViewById(R.id.scheduleTv);
        aboutTv = view.findViewById(R.id.aboutTv);
        contactInfoLl = view.findViewById(R.id.contactInfoLl);
        SocialMediaLl = view.findViewById(R.id.SocialMediaLl);
        additionalServicesTv = view.findViewById(R.id.additionalServicesTv);
        restaurantAlbumRv = view.findViewById(R.id.restaurantAlbumRv);

        if (imagePagerAdapter != null) {

            viewPager.setAdapter(imagePagerAdapter);

            createPagerDotsSlider();

        } else {

            view.findViewById(R.id.albumNestedHost).setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            dotsLinear.setVisibility(View.GONE);
        }

        if (albumAdapter != null) {
            restaurantAlbumRv.setAdapter(albumAdapter);
        } else {
            restaurantAlbumRv.setVisibility(View.GONE);
            view.findViewById(R.id.albumNestedHost).setVisibility(View.GONE);
        }


//        viewPager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                final int action = event.getAction();
//
//                if(viewPager.canScrollHorizontally(ViewPager.FOCUS_FORWARD)){
//                    if(action == MotionEvent.ACTION_MOVE){
//                        viewPager.getParent().requestDisallowInterceptTouchEvent(true);
//                    }
//
//                    return false;
//                }else{
//
//                    if(action == MotionEvent.ACTION_MOVE){
//                        viewPager.getParent().requestDisallowInterceptTouchEvent(false);
//                    }
//
//                    viewPager.setOnTouchListener(null);
//                    return true;
//                }
//            }
//        });


        return view;
    }

    private void createPagerDotsSlider() {

        viewPager.setPageMargin((int) (20 * getResources().getDisplayMetrics().density));

        if (restaurant.getBannerImages().size() > 1) {


            viewPager.setOffscreenPageLimit(imagePagerAdapter.getCount() - 1);

            final ImageView[] dots = new ImageView[imagePagerAdapter.getCount()];

            final LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

            for (int i = 0; i < imagePagerAdapter.getCount(); i++) {

                dots[i] = new ImageView(getContext());

                if (i == 0) {
                    dots[0].setImageDrawable(ContextCompat.getDrawable(requireContext(),
                            R.drawable.pager_indicator_active));
                } else {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getContext(),
                            R.drawable.pager_indicator_inactive));
                }


                params.setMargins((int) (4 * density), 0, (int) (4 * density), 0);

                dotsLinear.addView(dots[i], params);

            }

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                int previousDot = 0;

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {

                    dots[previousDot].setImageDrawable
                            (ContextCompat.getDrawable(requireContext(), R.drawable.pager_indicator_inactive));

                    previousDot = position;

                    dots[position].setImageDrawable
                            (ContextCompat.getDrawable(requireContext(), R.drawable.pager_indicator_active));

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fillInfo();
    }

    private void fillInfo() {

        addressTv.setText(restaurant.getFullAddress());

        String statusText;
        switch (status) {

            case PartneredRestaurant.STATUS_CLOSED:

                statusText = "Restaurant is currently closed";

                break;

            case PartneredRestaurant.STATUS_OPEN:

                statusText = "Restaurant is currently open";

                break;

            case PartneredRestaurant.STATUS_SHUTDOWN:

                statusText = "This Restaurant is shutdown";

                break;

            default:

                statusText = "Unknown Restaurant Status";

                break;

        }

        statusTv.setText(statusText);

        String serviceOptionsFull = "";

        if (restaurant.getServiceOptions().size() > 1) {
            for (int i = 0; i < restaurant.getServiceOptions().size() - 1; i++) {
                serviceOptionsFull = serviceOptionsFull.concat(restaurant.getServiceOptions().get(i) + ", ");
            }
        }

        serviceOptionsFull = serviceOptionsFull.concat(restaurant.getServiceOptions().get(restaurant.getServiceOptions().size() - 1));

        serviceOptionsTv.setText(serviceOptionsFull);


        if (restaurant.getSchedule() != null) {

            final String[] scheduleFullText = {""};


            final Thread scheduleThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    final SimpleDateFormat hourMinuteFormat =
                            new SimpleDateFormat("h:mm a", Locale.getDefault());
//            final long currentTime = System.currentTimeMillis();
//
//            final Calendar calendar = Calendar.getInstance(Locale.getDefault());
//            calendar.setTime(new Date(currentTime));
//
//            final int year = calendar.get(Calendar.YEAR),
//                    month = calendar.get(Calendar.MONTH),
//                    day = calendar.get(Calendar.DATE);
//
//            calendar.set(year,month,day,0,0,0);
//            final long elapsedTimeOfDay = currentTime - calendar.getTimeInMillis();

                    for (String key : restaurant.getSchedule().keySet()) {

                        final Map<String, Object> dayMap = restaurant.getSchedule().get(key);

                        if (dayMap.containsKey("isClosed") && (boolean) dayMap.get("isClosed")) {

                            scheduleFullText[0] = scheduleFullText[0].concat(key + " " + "closed");

                        } else {

                            if (dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.FIRST_START)) &&
                                    dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.FIRST_END))) {

                                final long firstStart
                                        = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.FIRST_START));

                                final long firstEnd
                                        = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.FIRST_END));

                                scheduleFullText[0] = scheduleFullText[0].concat(key.substring(key.indexOf("-") + 1) + " " +
                                        hourMinuteFormat.format(firstStart) + " - " + hourMinuteFormat.format(firstEnd));

                            }

                            if (dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_START)) &&
                                    dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_END))) {

                                final long secondStart
                                        = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.SECOND_START));

                                final long secondEnd
                                        = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.SECOND_END));

                                scheduleFullText[0] = scheduleFullText[0].concat(" & " +
                                        hourMinuteFormat.format(secondStart) + " - " + hourMinuteFormat.format(secondEnd));

                            }
                        }


                        scheduleFullText[0] = scheduleFullText[0].concat("\n");

                    }


//                    final String finalScheduleFullText = scheduleFullText;
//                    scheduleTv.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            scheduleTv.setText(finalScheduleFullText);
//                        }
//                    });

                }
            });

            scheduleThread.start();

            try {
                scheduleThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                scheduleTv.setText(scheduleFullText[0]);
            }


        } else {

            scheduleTv.setText("Restaurant hasn't added a schedule yet!");

        }

        aboutTv.setText(restaurant.getDescription());

        if (restaurant.getContactInformation() != null) {
            fillLayoutFromMap(contactInfoLl, (Map) restaurant.getContactInformation());
        } else {
            getView().findViewById(R.id.contactInfoNameTv).setVisibility(View.GONE);
            getView().findViewById(R.id.contactInfoSeperator).setVisibility(View.GONE);
            contactInfoLl.setVisibility(View.GONE);
        }


        if (restaurant.getSocialMediaLinks() != null) {
            fillLayoutFromMap(SocialMediaLl, (Map) restaurant.getSocialMediaLinks());
        } else {
            getView().findViewById(R.id.socialMediaNameTv).setVisibility(View.GONE);
            getView().findViewById(R.id.socialMediaSeperator).setVisibility(View.GONE);
            SocialMediaLl.setVisibility(View.GONE);

        }


        if (restaurant.getAdditionalServices() != null) {
            for (String service : restaurant.getAdditionalServices()) {
                additionalServicesTv.setText("- " + service + "\n");
            }
        } else {

            getView().findViewById(R.id.additionalServicesNameTv).setVisibility(View.GONE);
            getView().findViewById(R.id.additionalServicesSeperator).setVisibility(View.GONE);
            additionalServicesTv.setVisibility(View.GONE);
        }

    }


    private void fillLayoutFromMap(LinearLayout linearLayout, Map<String, String> map) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (lightBlack == 0) {
                    lightBlack = ResourcesCompat.getColor(getResources(), R.color.light_black, null);
                }

                LinearLayout.LayoutParams params = null;


                if (map.size() > 1) {
                    params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                }


                for (String key : map.keySet()) {

                    final TextView tv = new TextView(requireContext());
                    tv.setTextColor(lightBlack);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    tv.setLinkTextColor(ResourcesCompat.getColor(getResources(), R.color.light_blue_600, null));
                    tv.setClickable(true);
                    tv.setFocusable(true);
                    tv.setAutoLinkMask(Linkify.ALL);

                    tv.setText(Html.fromHtml("<font color='#FF8000'> " + key + ": </font>" +
                            map.get(key)));

                    if (params != null) {
                        params.setMargins((int) (4 * density), 0, (int) (4 * density), 0);
                        final LinearLayout.LayoutParams finalParams = params;
                        linearLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                linearLayout.addView(tv, finalParams);

                            }
                        });
                    } else {
                        linearLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                linearLayout.addView(tv);
                            }
                        });

                    }
                }

            }
        }).start();

    }

    @Override
    public void onImageClicked(int position) {

        FullScreenImagesUtil.showImageFullScreen(requireContext(),
                restaurant.getAlbumImages().get(position), null);

    }
}