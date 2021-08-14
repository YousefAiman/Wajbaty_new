package com.developers.wajbaty.PartneredRestaurant.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.viewpager2.widget.ViewPager2;

import com.developers.wajbaty.Activities.MapsActivity;
import com.developers.wajbaty.Adapters.FragmentsPagerAdapter;
import com.developers.wajbaty.Adapters.WorkingScheduleAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.Models.PartneredRestaurantModel;
import com.developers.wajbaty.PartneredRestaurant.Fragments.FirebaseReviewsFragment;
import com.developers.wajbaty.PartneredRestaurant.Fragments.RestaurantInfoFragment;
import com.developers.wajbaty.PartneredRestaurant.Fragments.RestaurantMenuFragment;
import com.developers.wajbaty.R;
import com.developers.wajbaty.Utils.FullScreenImagesUtil;
import com.developers.wajbaty.Utils.GlobalVariables;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class RestaurantActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener,
        View.OnClickListener, Observer {


    private static final String TAG = "RestaurantActivity";

    //views
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView restaurantMainIv;
    private Toolbar restaurantToolbar;
    private TextView statusToolbarTv;
    private FloatingActionButton markLocationFb;

    private TabLayout restaurantTabLayout;
    private ViewPager2 restaurantViewPager;

    //firebaes
    private FirebaseFirestore firestore;
    private DocumentReference restaurantRef;
    private PartneredRestaurant restaurant;
    private PartneredRestaurantModel partneredRestaurantModel;
    private String currentOpenTimeRange;
    private boolean alreadyFavored;
    private ProgressDialogFragment progressDialog;
    private ArrayList<String> likedMenuItems;
    private Task<?> taskWaitingFor;

    private String ID;
    private String currentUid;

    private boolean onSavedState;

    public static void main(String[] args) {

        System.out.println(new SimpleDateFormat("EEEE", Locale.getDefault())
                .format(System.currentTimeMillis()));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        showProgressDialog();

        intializeObjects();

        getViews();

        checkUserLiked();

        fetchRestaurantInfo();

        addListeners();


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        onSavedState = true;
    }

    private void intializeObjects() {

        //        ID  = getIntent().getStringExtra("ID");
//        ID  = "b60dc8bd-4756-4f9f-b7a2-04c92c97167d";

        final Intent intent = getIntent();

        if (intent != null && intent.hasExtra("ID")) {
            ID = intent.getStringExtra("ID");
        } else {
            Toast.makeText(this,
                    "An error occurred while trying to view restaurant! Please try again",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        partneredRestaurantModel = new PartneredRestaurantModel(ID);
        partneredRestaurantModel.addObserver(this);

        firestore = FirebaseFirestore.getInstance();

    }

    private void checkUserLiked() {

        firestore.collection("Users")
                .document(getCurrentUid())
                .collection("Favorites")
                .document("FavoriteRestaurants")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists() & snapshot.contains("FavoriteRestaurants")) {
                    alreadyFavored = ((List<String>) snapshot.get("FavoriteRestaurants")).contains(ID);
                    if (alreadyFavored) {
                        changeFavIcon(R.drawable.heart_filled_icon);
                    }
                }
            }
        });

    }

    private String getCurrentUid() {

        if (currentUid == null)
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return currentUid;

    }

    private void showProgressDialog() {

        if (progressDialog == null) {
            progressDialog = new ProgressDialogFragment();
        }
        if (!onSavedState) {
            progressDialog.show(getSupportFragmentManager(), "progress");
        }
    }

    private void hideProgressDialog() {


        if (progressDialog != null && progressDialog.isVisible()) {


            progressDialog.dismiss();
        }

    }


    private void fetchRestaurantInfo() {

        if (ID != null) {

//            final boolean[] waitForSchedule = {false};

            restaurantRef = firestore.collection("PartneredRestaurant").document(ID);

            restaurantRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                private Map<String, Object> scheduleMap;

                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if (documentSnapshot.exists()) {
                        restaurant = documentSnapshot.toObject(PartneredRestaurant.class);

                        restaurantRef.collection("Lists")
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot snapshots) {

                                for (DocumentSnapshot snapshot : snapshots) {

                                    switch (snapshot.getId()) {

                                        case "SocialMediaLinks":

                                            restaurant.setSocialMediaLinks(snapshot.getData());

                                            break;

                                        case "ServiceOptions":

                                            restaurant.setServiceOptions((List<String>) snapshot.get("ServiceOptions"));

                                            break;

                                        case "ContactInformation":

                                            restaurant.setContactInformation(snapshot.getData());


                                            break;

                                        case "AdditionalServices":

                                            restaurant.setServiceOptions((List<String>) snapshot.get("AdditionalServices"));

                                            break;

                                        case "Schedule":

                                            scheduleMap = snapshot.getData();

                                            break;

                                    }
                                }
                            }
                        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {

                                    if (scheduleMap != null) {
                                        final LinkedHashMap<String, Map<String, Object>> scheduleObjectMap = new LinkedHashMap<>();

                                        for (String weekDay : scheduleMap.keySet()) {
                                            scheduleObjectMap.put(weekDay.split("-")[1],
                                                    (Map<String, Object>) scheduleMap.get(weekDay));
                                        }

                                        restaurant.setSchedule(scheduleObjectMap);
                                    }

                                    firestore.collection("Users")
                                            .document(getCurrentUid())
                                            .collection("Favorites")
                                            .document(ID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot snapshot) {
                                            if (snapshot != null && snapshot.exists()) {
                                                likedMenuItems = (ArrayList<String>) snapshot.get("FavoriteMenuItems");
                                            }
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (restaurant.getSchedule() != null) {

//                                                    new Thread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//
                                                    final int status = getRestaurantStatus();

                                                    switch (status) {

                                                        case PartneredRestaurant.STATUS_OPEN:

                                                            statusToolbarTv.setText("Open " + currentOpenTimeRange);

                                                            statusToolbarTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_circle,
                                                                    0, 0, 0);


                                                            break;

                                                        case PartneredRestaurant.STATUS_CLOSED:

                                                            statusToolbarTv.setText("Closed");
                                                            statusToolbarTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_circle,
                                                                    0, 0, 0);

                                                            break;

                                                        case PartneredRestaurant.STATUS_SHUTDOWN:

                                                            statusToolbarTv.setText("Shut down");
                                                            break;


                                                        case PartneredRestaurant.STATUS_UNKNOWN:

                                                            statusToolbarTv.setVisibility(View.GONE);

                                                            break;
                                                    }


                                                    fillRestaurantInfo();

                                                    setupViewPager(status);

//                                                        }
//                                                    }).start();
                                                } else {

                                                    statusToolbarTv.setVisibility(View.GONE);
                                                    fillRestaurantInfo();

                                                    setupViewPager(PartneredRestaurant.STATUS_UNKNOWN);

                                                }


                                            } else {
                                                finish();
                                            }

                                        }
                                    });
                                }


                            }
                        });

//                        scheduleRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                            @Override
//                            public void onSuccess(DocumentSnapshot snapshot) {
//                                if(snapshot.exists()){
//
//                                    waitForSchedule[0] = true;
//                                }
//                            }
//                        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                                if(task.isSuccessful() && task.getResult().exists()){
//
//
//
//
////                                    final String dayOfTHeWeek =
////                                            Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_WEEK);
//
////                                    if()
//
//                                }else{
//
//
//                                    statusToolbarTv.setVisibility(View.GONE);
//                                    fillRestaurantInfo();
//
//                                    setupViewPager(PartneredRestaurant.STATUS_UNKNOWN);
//
//
//                                }
//
//
//                            }
//                        });
                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

//                    if(task.isSuccessful() && restaurant!=null && !waitForSchedule[0]){
//
//                        statusToolbarTv.setVisibility(View.GONE);
//
//                        fillRestaurantInfo();
//
//                        setupViewPager(PartneredRestaurant.STATUS_UNKNOWN);
//
//                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RestaurantActivity.this,
                            "An error occurred while fetching restaurant info",
                            Toast.LENGTH_SHORT).show();

                    finish();
                }
            });

//            restaurantTasks[1] = restaurantRef.collection("Lists")
//                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot snapshots) {
//
//                            for(DocumentSnapshot snapshot:snapshots){
//
//                                switch (snapshot.getId()){
//
//                                    case "SocialMediaLinks":
//
//                                        restaurant.setSocialMediaLinks(snapshot.getData());
//
//                                        break;
//
//                                    case "ServiceOptions":
//
//                                        restaurant.setServiceOptions((List<String>) snapshot.get("ServiceOptions"));
//
//                                        break;
//
//                                    case "ContactInformation":
//
//                                        restaurant.setContactInformation(snapshot.getData());
//
//
//                                        break;
//
//                                    case "AdditionalServices":
//
//                                        restaurant.setServiceOptions((List<String>) snapshot.get("AdditionalServices"));
//
//                                        break;
//
//                                }
//                            }
//                        }
//                    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                        }
//                    });
//
//            restaurantTasks[2] = firestore.collection("Users")
//                            .document(getCurrentUid())
//                            .collection("Favorites")
//                            .document(ID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot snapshot) {
//                            if(snapshot!=null && snapshot.exists()){
//                                likedMenuItems = (List<String>) snapshot.get("FavoriteMenuItems");
//                            }
//                        }
//                    });
//
//            Tasks.whenAllComplete(restaurantTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
//                @Override
//                public void onComplete(@NonNull Task<List<Task<?>>> task) {
//
//                    boolean allSuccess = true;
//                    for(Task<?> resultTask:task.getResult()){
//                        if(resultTask.getException()!=null){
//                            allSuccess = false;
//                            break;
//                        }
//                    }
//
//                    if(allSuccess){
//                            if(restaurant.getSchedule()!=null){
//
//
//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//
//                                        final int status = getRestaurantStatus();
//
//                                        new Handler().post(new Runnable() {
//                                            @Override
//                                            public void run() {
//
//                                                switch (status){
//
//                                                    case PartneredRestaurant.STATUS_OPEN:
//
//                                                        statusToolbarTv.setText("Open "+currentOpenTimeRange);
//
//                                                        statusToolbarTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green_circle,
//                                                                0,0,0);
//
//                                                        break;
//
//                                                    case PartneredRestaurant.STATUS_CLOSED:
//
//
//                                                        statusToolbarTv.setText("Closed");
//                                                        statusToolbarTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.red_circle,
//                                                                0,0,0);
//
//                                                        break;
//
//                                                    case PartneredRestaurant.STATUS_SHUTDOWN:
//
//                                                        statusToolbarTv.setText("Shut down");
//
//                                                        break;
//
//
//                                                    case PartneredRestaurant.STATUS_UNKNOWN:
//
//                                                        statusToolbarTv.setVisibility(View.GONE);
//
//                                                        break;
//                                                }
//
//
//                                                fillRestaurantInfo();
//
//                                                setupViewPager(status);
//
//                                            }
//                                        });
//                                    }
//                                }).start();
//                            }else{
//
//                                statusToolbarTv.setVisibility(View.GONE);
//                                fillRestaurantInfo();
//
//                                setupViewPager(PartneredRestaurant.STATUS_UNKNOWN);
//
//                            }
//
//
//                    }else{
//                        finish();
//                    }
//
//                }
//            });

        }

    }


    private void getViews() {

        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        restaurantMainIv = findViewById(R.id.restaurantMainIv);
        restaurantToolbar = findViewById(R.id.restaurantToolbar);
        statusToolbarTv = findViewById(R.id.statusToolbarTv);
        markLocationFb = findViewById(R.id.markLocationFb);
        restaurantTabLayout = findViewById(R.id.restaurantTabLayout);
        restaurantViewPager = findViewById(R.id.restaurantViewPager);

        if (GlobalVariables.getCurrentRestaurantId() == null) {
            restaurantToolbar.inflateMenu(R.menu.restaurant_customer_menu);
        }

    }

    private void addListeners() {

        restaurantMainIv.setOnClickListener(this);
        restaurantToolbar.setNavigationOnClickListener(v -> finish());
        restaurantToolbar.setOnMenuItemClickListener(this);

    }

    private void fillRestaurantInfo() {

        hideProgressDialog();

        if (restaurant.getMainImage() != null && !restaurant.getMainImage().isEmpty()) {

            final CircularProgressDrawable progressDrawable = new CircularProgressDrawable(this);
            progressDrawable.setColorSchemeColors(ResourcesCompat.getColor(getResources(), R.color.orange, null));
            progressDrawable.setStyle(CircularProgressDrawable.LARGE);
            progressDrawable.start();

            Picasso.get().load(restaurant.getMainImage()).fit().centerCrop()
                    .placeholder(progressDrawable).into(restaurantMainIv);
        }

        collapsingToolbar.setTitle(restaurant.getName());

//        if(restaurant.getCoordinates()!=null &&
//                restaurant.getCoordinates().containsKey("lat") &&
//                restaurant.getCoordinates().containsKey("lng")){
//
//
//
//            markLocationFb.setOnClickListener(this);
//        }

        if (restaurant.getLat() != 0 && restaurant.getLng() != 0) {
            markLocationFb.setOnClickListener(this);
        }


//        if(restaurant.getStatus() == PartneredRestaurant.STATUS_OPEN){
//
//
//
//        }else{
//
////            statusToolbarTv.setText("Closed");
////            statusToolbarTv.set(R.drawable.red_circle,null,null,null);
//
//        }
    }

    private void setupViewPager(int status) {

        final Integer[] icons = {R.drawable.info_icon, R.drawable.menu_icon, R.drawable.star_icon};
        final String[] titles = {"About", "Menu", "Reviews"};

        Log.d("ttt", "restaurant activity currency: " +
                getIntent().getStringExtra("currency"));


        final Fragment[] fragments = new Fragment[]{new RestaurantInfoFragment(restaurant, status),
                RestaurantMenuFragment.newInstance(restaurant, likedMenuItems, getIntent().getStringExtra("currency")),
//                new RestaurantReviewsFragment(restaurant)
                new FirebaseReviewsFragment(restaurantRef,
                        restaurantRef.collection("Reviews"),
                        restaurant.getReviewSummary())
        };

//        final List<Fragment> fragments = new ArrayList<>();
//        fragments.add(new RestaurantInfoFragment(restaurant,status));
//        fragments.add(new RestaurantMenuFragment(restaurant));
//        fragments.add(new RestaurantReviewsFragment());
//        fragments.add(new RestaurantInfoFragment());
//        fragments.add(new RestaurantInfoFragment());

        restaurantViewPager.setAdapter(new FragmentsPagerAdapter(this, fragments));

        new TabLayoutMediator(restaurantTabLayout, restaurantViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {

                tab.setIcon(icons[position]);
//                tab.setText(titles[position]);

            }
        }).attach();

        restaurantTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.setText(titles[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setText("");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        restaurantTabLayout.getTabAt(0).setText(titles[0]);
//        restaurantTabLayout.select;
//    restaurantTabLayout.setupWithViewPager(restaurantViewPager);


    }

    private int getRestaurantStatus() {


        final long currentTime = System.currentTimeMillis();

        final String dayName = new SimpleDateFormat("EEEE", Locale.getDefault())
                .format(currentTime);


        if (restaurant.getSchedule().containsKey(dayName)) {

            final Map<String, Object> dayMap =
                    restaurant.getSchedule().get(dayName);

            if (dayMap.containsKey("isClosed") && (boolean) dayMap.get("isClosed")) {

                return PartneredRestaurant.STATUS_CLOSED;

            } else if (dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.FIRST_START)) &&
                    dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.FIRST_END))) {

                final long firstStart
                        = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.FIRST_START));

                final long firstEnd
                        = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.FIRST_END));

                final SimpleDateFormat hourMinuteFormat =
                        new SimpleDateFormat("h:mm a", Locale.getDefault());

                final Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.setTime(new Date(currentTime));

                final int year = calendar.get(Calendar.YEAR),
                        month = calendar.get(Calendar.MONTH),
                        day = calendar.get(Calendar.DATE);

                calendar.set(year, month, day, 0, 0, 0);
                final long elapsedTimeOfDay = currentTime - calendar.getTimeInMillis();
                Log.d(TAG, "elapsedTimeOfDay: " + elapsedTimeOfDay);

                if (elapsedTimeOfDay > firstStart && elapsedTimeOfDay < firstEnd) {

                    currentOpenTimeRange = hourMinuteFormat.format(firstStart) + " - " + hourMinuteFormat.format(firstEnd);

                    Log.d(TAG, "currentOpenTimeRange: " + currentOpenTimeRange);

                    return PartneredRestaurant.STATUS_OPEN;

                } else if (dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_START)) &&
                        dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_END))) {


                    final long secondStart
                            = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.SECOND_START));

                    final long secondEnd
                            = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.SECOND_END));

                    if (elapsedTimeOfDay > secondStart && elapsedTimeOfDay < secondEnd) {

                        currentOpenTimeRange = hourMinuteFormat.format(secondStart) + " - " + hourMinuteFormat.format(secondEnd);

                        Log.d(TAG, "currentOpenTimeRange: " + currentOpenTimeRange);

                        return PartneredRestaurant.STATUS_OPEN;

                    } else {
                        return PartneredRestaurant.STATUS_CLOSED;
                    }

                } else {
                    return PartneredRestaurant.STATUS_CLOSED;
                }

            } else {
                return PartneredRestaurant.STATUS_UNKNOWN;
            }

        } else {
            return PartneredRestaurant.STATUS_UNKNOWN;
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {


        if (item.getItemId() == R.id.fav_action) {

            showProgressDialog();

            partneredRestaurantModel.favRestaurant(alreadyFavored);

            return true;
        }

        return false;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == markLocationFb.getId()) {

            final Intent restaurantIntent = new Intent(this, MapsActivity.class);
            restaurantIntent.putExtra("restaurantName", restaurant.getName());
            restaurantIntent.putExtra("lat", restaurant.getLat());
            restaurantIntent.putExtra("lng", restaurant.getLng());
//            restaurantIntent.putExtra("destinationMap",(Serializable) restaurant.getCoordinates());
            startActivity(restaurantIntent);

        } else if (v.getId() == restaurantMainIv.getId()) {

            FullScreenImagesUtil.showImageFullScreen(this, restaurant.getMainImage(), null);

        }

    }

    @Override
    public void update(Observable o, Object arg) {


        if (arg instanceof HashMap) {


            final HashMap<Integer, Object> resultMap = (HashMap<Integer, Object>) arg;

            final int key = resultMap.keySet().iterator().next();
            final Object result = resultMap.get(key);

            if (key == PartneredRestaurantModel.TYPE_FAVORITE) {

                hideProgressDialog();

                if (result instanceof Boolean && (boolean) result) {


                    changeFavIcon(alreadyFavored ? R.drawable.heart_outlined_icon : R.drawable.heart_filled_icon);

                    alreadyFavored = !alreadyFavored;

                } else if (result instanceof String) {

                    Toast.makeText(this,
                            "Adding to favorite failed! Please try again",
                            Toast.LENGTH_LONG).show();

                    Log.d(TAG, "failed to fav restautant: " + result);
                }

            }

        }

    }


    private void changeFavIcon(int icon) {
        restaurantToolbar.getMenu().findItem(R.id.fav_action).setIcon(icon);
    }
}