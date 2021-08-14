package com.developers.wajbaty.PartneredRestaurant.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.Adapters.WorkingScheduleAdapter;
import com.developers.wajbaty.Fragments.ProgressDialogFragment;
import com.developers.wajbaty.Models.PartneredRestaurantModel;
import com.developers.wajbaty.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class RestaurantScheduleActivity extends AppCompatActivity implements View.OnClickListener,
        Observer {

    private static final String TAG = "ScheduleActivity";
    //views
    private ImageView backIv;
    private RecyclerView scheduleRv;
    private Button scheduleInputNextBtn;
    private TextView skipTv;

    //adapter
    private LinkedHashMap<String, Map<String, Object>> scheduleMap;

    //firebase
    private ProgressDialogFragment progressDialogFragment;

    private PartneredRestaurantModel restaurantModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_schedule);

        getViews();
        initializeScheduleAdapter();

        restaurantModel = new PartneredRestaurantModel();
        restaurantModel.addObserver(this);
    }

    private void getViews() {


        backIv = findViewById(R.id.backIv);
        backIv.setOnClickListener(this);

        skipTv = findViewById(R.id.skipTv);
        skipTv.setOnClickListener(this);

        scheduleRv = findViewById(R.id.scheduleRv);
        scheduleInputNextBtn = findViewById(R.id.scheduleInputNextBtn);
        scheduleInputNextBtn.setOnClickListener(this);
    }


    private void initializeScheduleAdapter() {

        scheduleMap = new LinkedHashMap<>();
        scheduleMap.put("1-Saturday", new HashMap<>());
        scheduleMap.put("2-Sunday", new HashMap<>());
        scheduleMap.put("3-Monday", new HashMap<>());
        scheduleMap.put("4-Tuesday", new HashMap<>());
        scheduleMap.put("5-Wednesday", new HashMap<>());
        scheduleMap.put("6-Thursday", new HashMap<>());
        scheduleMap.put("7-Friday", new HashMap<>());

        final WorkingScheduleAdapter adapter = new WorkingScheduleAdapter(this, scheduleMap);
        scheduleRv.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == backIv.getId()) {
            finish();
        } else if (v.getId() == scheduleInputNextBtn.getId()) {

//            final SimpleDateFormat dateFormat =
//                    new SimpleDateFormat("HH:mm",Locale.getDefault());
//            final HashMap<String,HashMap<String,Object>> schedule = new HashMap<>();
//            final HashMap<String,Object> timesSchedule = new HashMap<>();

            boolean atLeastOneOpen = false;

            for (String key : scheduleMap.keySet()) {

                Log.d(TAG, "for day: " + key);

                final Map<String, Object> dayMap = scheduleMap.get(key);

//                if(dayMap.containsKey("isClosed") && (boolean) dayMap.get("isClosed")){
//                    Log.d(TAG,"is closed on: "+key);
//                    return;
//                }

                if (!dayMap.containsKey("isClosed") || !(boolean) dayMap.get("isClosed")) {

//                    boolean atleastProvidedOneDaySchedule = false;

                    if (!((dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.FIRST_START))
                            && dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.FIRST_END)))
//                            || (dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_START))
//                            && dayMap.containsKey(String.valueOf(WorkingScheduleAdapter.SECOND_END)))
                    )) {

                        Toast.makeText(this,
                                "You need to at least add the first working shift schedule for: " + key, Toast.LENGTH_LONG).show();

                        return;
                    }

                    atLeastOneOpen = true;
                }
//                    if(dayMap.get(keyName) instanceof Boolean){
//                        continue;
//                    }
//
//
//                    String timeName = "UnKnown";
//
//                    switch (Integer.parseInt(keyName)){
//
//                        case WorkingScheduleAdapter.FIRST_START:
//
//                            timeName = "firstOpeningTime";
//
//                            break;
//                        case WorkingScheduleAdapter.FIRST_END:
//
//                            timeName = "firstClosingTime";
//
//                            break;
//                        case WorkingScheduleAdapter.SECOND_START:
//
//                            timeName = "secondOpeningTime";
//
//                            break;
//                        case WorkingScheduleAdapter.SECOND_END:
//
//                            timeName = "secondClosingTime";
//
//                            break;
//
//                    }

//                    Log.d(TAG,key + " -- "+ timeName + " at: "+dateFormat.format(dayMap.get(keyName)));
//                    if(scheduleMap.get(key) == -1L){
//
//                        Log.d(TAG,"is closed on: "+key);
//
//                        dayName = key;
//                        timesSchedule.put("isClosed",true);
//
//                    }else{
//
//                        dayName = key.split("-")[0];
//
//                        timesSchedule.put("isClosed",false);
//
//
//                        timesSchedule.put(timeName,scheduleMap.get(key));
//
//
//
//                        Log.d(TAG,dayName + " -- "+ timeName + " at: "+dateFormat.format(scheduleMap.get(key)));
//                    }
            }

//                schedule.put(dayName,timesSchedule);
            Log.d(TAG, "------------------------------------------------------------------------");

            if (atLeastOneOpen) {
//                Toast.makeText(this, "Adding restaruratn", Toast.LENGTH_SHORT).show();
                showProgressDialog();

//                for(int i=0;i<5;i++){

                restaurantModel.addRestaurantToFirebase(false, getIntent(), scheduleMap);

//                }

//                restaurantModel.addRestaurantToFirebase(false,getIntent(),scheduleMap);
            } else {
                Toast.makeText(this,
                        "You need to at least have one working day in your schedule",
                        Toast.LENGTH_SHORT).show();
            }
//            for(String key:schedule.keySet()){
//
//                Log.d(TAG,key + " - "+schedule.get(key).toString());
//
//            }
        } else if (v.getId() == skipTv.getId()) {

            showProgressDialog();
            restaurantModel.addRestaurantToFirebase(false, getIntent(), scheduleMap);

        }
    }

    private void showProgressDialog() {

        progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.setTitle("Creating Restaurant");
        progressDialogFragment.setMessage("Please wait");
        progressDialogFragment.show(getSupportFragmentManager(), "progress");

    }

    @Override
    public void update(Observable o, Object arg) {

        progressDialogFragment.dismiss();

        if (arg instanceof HashMap) {

            final HashMap<String, Object> restaurantResult = (HashMap<String, Object>) arg;

            if ((Boolean) restaurantResult.get("result") && restaurantResult.containsKey("id")) {

                final String restaurantId = (String) restaurantResult.get("id");

                final Map<String, Object> addressMap = (Map<String, Object>) getIntent().getSerializableExtra("addressMap");

                startActivity(new Intent(RestaurantScheduleActivity.this, RestaurantActivity.class)
                        .putExtra("currency", (String) addressMap.get("currency"))
                        .putExtra("ID", restaurantId));

//                finish();
            }

        } else if (arg instanceof String) {

            Toast.makeText(this, "error while uploading restaurant",
                    Toast.LENGTH_SHORT).show();

            Log.d("ttt", (String) arg);

        }

    }
}