package com.developers.wajbaty.Customer.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.FavoriteRestaurantsAdapter;
import com.developers.wajbaty.Adapters.WorkingScheduleAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.PartneredRestaurant;
import com.developers.wajbaty.Models.PartneredRestaurantModel;
import com.developers.wajbaty.PartneredRestaurant.Activities.RestaurantActivity;
import com.developers.wajbaty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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

public class FavoriteRestaurantsFragment extends Fragment
        implements FavoriteRestaurantsAdapter.FavoriteRestaurantsListener, Observer {


    private static final int FAVORITE_ITEM_LIMIT = 10;
    private static final String TAG = "FavResFragment";

    //views
    private RecyclerView favoritesRv;
    private TextView noFavItemTv;
    private ProgressBar favoritesProgressBar;

    //adapter
    private FavoriteRestaurantsAdapter favoriteRestaurantsAdapter;
    private ArrayList<PartneredRestaurant.PartneredRestaurantSummary> restaurantSummaries;


    //firebase
    private ArrayList<String> favRestaurantsIds;
    private FirebaseFirestore firestore;
    private String currentUid;
    private Query mainQuery;
    private boolean isLoadingItems;
    private ScrollListener scrollListener;

    private String currentOpenTimeRange;
    private ProgressDialogFragment progressDialogFragment;

    public FavoriteRestaurantsFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        restaurantSummaries = new ArrayList<>();

        favoriteRestaurantsAdapter = new FavoriteRestaurantsAdapter(restaurantSummaries, this, requireContext());

        mainQuery = firestore.collection("PartneredRestaurant");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        favoritesRv = view.findViewById(R.id.favoritesRv);
        noFavItemTv = view.findViewById(R.id.noFavItemTv);
        favoritesProgressBar = view.findViewById(R.id.favoritesProgressBar);

        favoritesRv.setAdapter(favoriteRestaurantsAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getFavRestaurantsIds();

    }


    private void getFavRestaurantsIds() {

        firestore.collection("Users")
                .document(currentUid)
                .collection("Favorites")
                .document("FavoriteRestaurants")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                favRestaurantsIds = (ArrayList<String>) snapshot.get("FavoriteRestaurants");
            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (favRestaurantsIds != null && !favRestaurantsIds.isEmpty()) {

                    Log.d(TAG, "favRestaurantsIds size: " + favRestaurantsIds.size());

                    getFavoriteRestaurants(true);
                } else {

                    Log.d(TAG, "favRestaurantsIds is null or empty");

                    favoritesProgressBar.setVisibility(View.GONE);
                    favoritesRv.setVisibility(View.INVISIBLE);
                    noFavItemTv.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private void getFavoriteRestaurants(boolean isInitial) {

//        showProgressBar();

        if (favoritesProgressBar.getVisibility() == View.GONE) {
            favoritesProgressBar.setVisibility(View.VISIBLE);
        }

        favoritesProgressBar.setVisibility(View.VISIBLE);

        Log.d(TAG, "getFavoriteRestaurants");

        isLoadingItems = true;

        Query currentQuery = null;

        if (restaurantSummaries.isEmpty()) {

            Log.d(TAG, "review summaries is empty");

            if (favRestaurantsIds.size() > 10) {

                Log.d(TAG, "favRestaurantsIds.size() > 10");

                currentQuery = mainQuery.whereIn("ID", favRestaurantsIds.subList(0, 10));
            } else {

                Log.d(TAG, "favRestaurantsIds.size() < 10");

                currentQuery = mainQuery.whereIn("ID", favRestaurantsIds);
            }

        } else if (favRestaurantsIds.size() >= restaurantSummaries.size() + 10) {

            currentQuery = mainQuery.whereIn("ID", favRestaurantsIds.subList(
                    restaurantSummaries.size(), restaurantSummaries.size() + 10));

        } else if (favRestaurantsIds.size() > restaurantSummaries.size()) {

            currentQuery = mainQuery.whereIn("ID", favRestaurantsIds.subList(
                    restaurantSummaries.size(), favRestaurantsIds.size()));
        }

        final List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        currentQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {

                if (!snapshots.isEmpty()) {

                    Log.d(TAG, "snapshots size: " + snapshots.size());

                    if (favoritesRv.getVisibility() == View.INVISIBLE) {
                        favoritesRv.setVisibility(View.VISIBLE);
                    }

                    final List<DocumentSnapshot> documentSnapshots = snapshots.getDocuments();

//                    if (isInitial) {

                    for (int i = 0; i < documentSnapshots.size(); i++) {

                        final DocumentSnapshot snapshot = documentSnapshots.get(i);

                        final PartneredRestaurant.PartneredRestaurantSummary restaurantSummary =
                                snapshot.toObject(PartneredRestaurant.PartneredRestaurantSummary.class);

                        final String[] status = new String[1];

                        tasks.add(snapshot.getReference().collection("Lists")
                                .document("Schedule")
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot snapshot) {

                                        Log.d(TAG, "getting schedule successeded");
                                        final Map<String, Object> scheduleMap = snapshot.getData();

                                        final LinkedHashMap<String, Map<String, Object>> scheduleObjectMap = new LinkedHashMap<>();


                                        for (String weekDay : scheduleMap.keySet()) {
                                            scheduleObjectMap.put(weekDay.split("-")[1],
                                                    (Map<String, Object>) scheduleMap.get(weekDay));
                                        }

                                        final ScheduleTask scheduleTask = new ScheduleTask(scheduleObjectMap);

                                        final Thread thread = new Thread(scheduleTask);

                                        thread.start();

                                        try {
                                            thread.join();

                                            status[0] = scheduleTask.getStatusFormatted();

//                                                final int statusCode = scheduleTask.getStatus();
//                                                Log.d(TAG,"statusCode: "+statusCode);

                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        Log.d(TAG, "getting schedule onComplete");

                                        restaurantSummary.setStatus(status[0]);

                                        restaurantSummaries.add(restaurantSummary);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "getting schedule failed: " + e.getMessage());
                                    }
                                }));
                    }

//                    restaurantSummaries.addAll(snapshots.toObjects(PartneredRestaurant.PartneredRestaurantSummary.class));
//                    } else {
//                        restaurantSummaries.addAll(restaurantSummaries.size() - 1,
//                                snapshots.toObjects(PartneredRestaurant.PartneredRestaurantSummary.class));
//                    }
                } else if (restaurantSummaries.isEmpty() && favoritesRv.getVisibility() == View.VISIBLE) {

                    favoritesRv.setVisibility(View.INVISIBLE);

                }

            }
        }).addOnCompleteListener(task -> {

            Log.d(TAG, "task completeted");

            if (task.isSuccessful() && task.getResult() != null) {

                Tasks.whenAllComplete(tasks).addOnSuccessListener(new OnSuccessListener<List<Task<?>>>() {
                    @Override
                    public void onSuccess(List<Task<?>> tasks) {

                        Log.d(TAG, "tasks whenAllComplete success");

                        if (isInitial) {

                            if (!restaurantSummaries.isEmpty()) {
                                favoriteRestaurantsAdapter.notifyDataSetChanged();

                                if (restaurantSummaries.size() == FAVORITE_ITEM_LIMIT && scrollListener == null) {
                                    favoritesRv.addOnScrollListener(scrollListener = new ScrollListener());
                                }
                            }

                        } else {

                            if (!task.getResult().isEmpty()) {

                                int size = task.getResult().size();

                                favoriteRestaurantsAdapter.notifyItemRangeInserted(
                                        restaurantSummaries.size() - size, size);

                                if (task.getResult().size() < FAVORITE_ITEM_LIMIT && scrollListener != null) {
                                    favoritesRv.removeOnScrollListener(scrollListener);
                                    scrollListener = null;
                                }
                            }
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {

                        if (restaurantSummaries.isEmpty() && noFavItemTv.getVisibility() == View.GONE) {

                            noFavItemTv.setVisibility(View.VISIBLE);
                        } else if (!restaurantSummaries.isEmpty() && noFavItemTv.getVisibility() == View.VISIBLE) {
                            noFavItemTv.setVisibility(View.GONE);
                        }

                        isLoadingItems = false;


                        if (favoritesProgressBar.getVisibility() == View.VISIBLE) {
                            favoritesProgressBar.setVisibility(View.GONE);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "tasks whenAllComplete failed: " + e.getMessage());
                    }
                });

            } else {

                favoritesProgressBar.setVisibility(View.GONE);

            }

//            hideProgressbar();

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d(TAG, "task failed: " + e.getMessage());

//                hideProgressbar();
            }
        });


    }

    @Override
    public void removeFromFav(int position) {

        if (restaurantSummaries.size() > position) {
            showProgressDialog();

            final PartneredRestaurantModel model =
                    new PartneredRestaurantModel(restaurantSummaries.get(position).getID());

            model.addObserver(this);
            model.favRestaurant(true);
        }


    }

    @Override
    public void showRestaurant(int position) {

        startActivity(new Intent(requireContext(), RestaurantActivity.class)
                .putExtra("ID", restaurantSummaries.get(position).getID()));

    }

    @Override
    public void update(Observable o, Object arg) {

        if (arg instanceof HashMap) {

            final Thread resultThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    final HashMap<Integer, Object> resultMap = (HashMap<Integer, Object>) arg;

                    final int key = resultMap.keySet().iterator().next();
                    final Object result = resultMap.get(key);

                    if (key == PartneredRestaurantModel.TYPE_FAVORITE) {

                        if (result instanceof Boolean && (boolean) result) {

                            final String id = (String) resultMap.get(PartneredRestaurantModel.RESTAURANT_ID_CODE);

                            for (int i = 0; i < restaurantSummaries.size(); i++) {

                                if (restaurantSummaries.get(i).getID().equals(id)) {
                                    restaurantSummaries.remove(i);

                                    int finalI = i;
                                    favoritesRv.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            favoriteRestaurantsAdapter.notifyItemRemoved(finalI);

                                        }
                                    });

                                    break;
                                }

                            }


                        } else if (result instanceof String) {

                            Toast.makeText(requireContext(),
                                    "Removing restaurant from favorite failed! Please try again",
                                    Toast.LENGTH_LONG).show();

                            Log.d(TAG, "failed to fav restautant: " + result);
                        }


                    }

                }

            });

            resultThread.start();

            try {
                resultThread.join();

                hideProgressDialog();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void showProgressDialog() {
        if (progressDialogFragment == null) {
            progressDialogFragment = new ProgressDialogFragment();
        }
        progressDialogFragment.show(getChildFragmentManager(), "progress");
    }

//    private Thread getRestaurantStatus(Map<String,Map<String,Object>> schedule){
//
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//        final long currentTime = System.currentTimeMillis();
//
//        final String dayName =  new SimpleDateFormat("EEEE",Locale.getDefault())
//                .format(currentTime);
//
//        if(schedule.containsKey(dayName)){
//
//            final Map<String,Object> dayMap = schedule.get(dayName);
//
//            if(dayMap.containsKey("isClosed") && (boolean)dayMap.get("isClosed")){
//
//                return PartneredRestaurant.STATUS_CLOSED;
//
//            }else if(dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.FIRST_START)) &&
//                    dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.FIRST_END))){
//
//                final long firstStart
//                        = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.FIRST_START));
//
//                final long firstEnd
//                        = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.FIRST_END));
//
//                final SimpleDateFormat hourMinuteFormat =
//                        new SimpleDateFormat("h:mm a", Locale.getDefault());
//
//                final Calendar calendar = Calendar.getInstance(Locale.getDefault());
//                calendar.setTime(new Date(currentTime));
//
//                final int year = calendar.get(Calendar.YEAR),
//                        month = calendar.get(Calendar.MONTH),
//                        day = calendar.get(Calendar.DATE);
//
//                calendar.set(year,month,day,0,0,0);
//                final long elapsedTimeOfDay = currentTime - calendar.getTimeInMillis();
//                Log.d(TAG,"elapsedTimeOfDay: "+elapsedTimeOfDay);
//
//                if(elapsedTimeOfDay > firstStart && elapsedTimeOfDay < firstEnd){
//
//                    currentOpenTimeRange = hourMinuteFormat.format(firstStart) +" - "+hourMinuteFormat.format(firstEnd);
//
//                    return PartneredRestaurant.STATUS_OPEN;
//
//                }else if(dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_START)) &&
//                        dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_END))){
//
//
//                    final long secondStart
//                            = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.SECOND_START));
//
//                    final long secondEnd
//                            = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.SECOND_END));
//
//                    if(elapsedTimeOfDay > secondStart && elapsedTimeOfDay < secondEnd){
//
//                        currentOpenTimeRange = hourMinuteFormat.format(secondStart) +" - "+hourMinuteFormat.format(secondEnd);
//
//                        return PartneredRestaurant.STATUS_OPEN;
//
//                    }else{
//                        return PartneredRestaurant.STATUS_CLOSED;
//                    }
//
//                }else{
//                    return PartneredRestaurant.STATUS_CLOSED;
//                }
//
//            }else{
//                return PartneredRestaurant.STATUS_UNKNOWN;
//            }
//
//        }else{
//            return PartneredRestaurant.STATUS_UNKNOWN;
//        }
//
//            }
//        });
//
//        return thread;
//    }

    private void hideProgressDialog() {

        if (progressDialogFragment != null) {
            progressDialogFragment.dismiss();
        }

    }

    private static class ScheduleTask implements Runnable {

        private final Map<String, Map<String, Object>> schedule;
        private volatile int status;

        private volatile String statusFormatted;
        private String currentOpenTimeRange;

        public ScheduleTask(Map<String, Map<String, Object>> schedule) {
            this.schedule = schedule;
        }

        @Override
        public void run() {

            status = getScheduleStatus();

            statusFormatted = getStatusString();
        }

        private String getStatusString() {

            switch (status) {

                case PartneredRestaurant.STATUS_OPEN:

                    return "Open " + currentOpenTimeRange;

                case PartneredRestaurant.STATUS_CLOSED:

                    return "Closed";

                case PartneredRestaurant.STATUS_SHUTDOWN:

                    return "Shut down";
            }

            return "";
        }

        private int getScheduleStatus() {
            final long currentTime = System.currentTimeMillis();

            final String dayName = new SimpleDateFormat("EEEE", Locale.getDefault())
                    .format(currentTime);

            if (schedule.containsKey(dayName)) {

                final Map<String, Object> dayMap = schedule.get(dayName);

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

                        return PartneredRestaurant.STATUS_OPEN;

                    } else if (dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_START)) &&
                            dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_END))) {


                        final long secondStart
                                = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.SECOND_START));

                        final long secondEnd
                                = (long) dayMap.get(String.valueOf(WorkingScheduleAdapter.SECOND_END));

                        if (elapsedTimeOfDay > secondStart && elapsedTimeOfDay < secondEnd) {

                            currentOpenTimeRange = hourMinuteFormat.format(secondStart) + " - " + hourMinuteFormat.format(secondEnd);

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


        public int getStatus() {
            return status;
        }

        public String getStatusFormatted() {
            return statusFormatted;
        }

    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d(TAG, "is at bottom");
                getFavoriteRestaurants(false);
//                getMenuItemsForCategory(false);

            }
        }
    }

}