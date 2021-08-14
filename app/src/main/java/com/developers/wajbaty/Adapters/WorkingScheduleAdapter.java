package com.developers.wajbaty.Adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.developers.wajbaty.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class WorkingScheduleAdapter extends RecyclerView.Adapter<WorkingScheduleAdapter.DayScheduleVh> {

    public static final int FIRST_START = 1, FIRST_END = 2, SECOND_START = 3, SECOND_END = 4;
    private static final String TAG = "ScheduleActivity";
    private final Map<String, Map<String, Object>> scheduleMap;
    private final Context context;
    private final SimpleDateFormat dateFormat;
    private final String[] keyArray;
    private int greyColor, blackColor;

    public WorkingScheduleAdapter(Context context, Map<String, Map<String, Object>> scheduleMap) {
        this.context = context;
        this.scheduleMap = scheduleMap;
        keyArray = scheduleMap.keySet().toArray(new String[0]);
        dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public DayScheduleVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DayScheduleVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_work_time_schedule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DayScheduleVh holder, int position) {
        holder.bind(position);
    }


    @Override
    public int getItemCount() {
        return scheduleMap.size();
    }

    private void pickTime(Context context, TextView timeTv, int type, int position) {

        final String dayName = keyArray[position];

        int preSelectedHours = -1, preSelectedMinutes = -1;

        if (scheduleMap.get(dayName).containsKey(String.valueOf(type))) {

            final long timeInMinutes = ((long) scheduleMap.get(dayName).get(String.valueOf(type)) / 1000) / 60;

            preSelectedHours = (int) (timeInMinutes / 60);

            preSelectedMinutes = (int) (timeInMinutes - (preSelectedHours * 60));

            Log.d(TAG, "preSelectedHours: " + preSelectedHours);
            Log.d(TAG, "preSelectedMinutes: " + preSelectedMinutes);
        }

        final TimePickerDialog timePickerDialog =
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        Log.d(TAG, "hourOfDay: " + hourOfDay);

                        final long chosenTime = ((hourOfDay * 60) + minute) * 60 * 1000L;

//                  String complementaryTimeKey;

                        Log.d(TAG, "chosen Time: " + chosenTime);

                        switch (type) {

                            case FIRST_START:

//                        complementaryTimeKey = dayName + "-" +FIRST_END;

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(FIRST_END))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(FIRST_END)) <= chosenTime) {

                                        Toast.makeText(context,
                                                "The first opening time on " + dayName + " can't be later than or equal to first closing time",
                                                Toast.LENGTH_LONG).show();

                                        Log.d(TAG, "start time for first time at: " + dayName + " can't be later then or equal to end time");
                                        return;

                                    }

                                }

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(SECOND_START))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(SECOND_START)) <= chosenTime) {

                                        Toast.makeText(context,
                                                "The first opening time on " + dayName + " can't be later than or equal to second opening time",
                                                Toast.LENGTH_LONG).show();


                                        Log.d(TAG, "start time for first time at: " + dayName + " can't be later then or equal to second start time");
                                        return;

                                    }

                                }

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(SECOND_END))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(SECOND_END)) <= chosenTime) {

                                        Toast.makeText(context,
                                                "The first opening time on " + dayName + " can't be later than or equal to second closing time",
                                                Toast.LENGTH_LONG).show();

                                        Log.d(TAG, "start time for first time at: " + dayName + " can't be later then or equal to second end time");
                                        return;

                                    }

                                }


                                break;

                            case FIRST_END:


                                if (scheduleMap.get(dayName).containsKey(String.valueOf(FIRST_START))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(FIRST_START)) >= chosenTime) {

                                        Toast.makeText(context,
                                                "The first closing time on " + dayName + " can't be earlier than or equal to first opening time",
                                                Toast.LENGTH_LONG).show();

                                        Log.d(TAG, "end time for first end time at: " + dayName + " can't be earlier then or equal to start time");
                                        return;

                                    }

                                }

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(SECOND_START))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(SECOND_START)) <= chosenTime) {

                                        Toast.makeText(context,
                                                "The first closing time on " + dayName + " can't be later than or equal to second opening time",
                                                Toast.LENGTH_LONG).show();

                                        Log.d(TAG, "end time for first time at: " + dayName + " can't be later then or equal to second first time");
                                        return;

                                    }

                                }


                                if (scheduleMap.get(dayName).containsKey(String.valueOf(SECOND_END))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(SECOND_END)) <= chosenTime) {

                                        Toast.makeText(context,
                                                "The first closing time on " + dayName + " can't be later than or equal to second closing time",
                                                Toast.LENGTH_LONG).show();


                                        Log.d(TAG, "end time for first time at: " + dayName + " can't be later then or equal to second end time");
                                        return;

                                    }

                                }


                                break;

                            case SECOND_START:

//                    complementaryTimeKey = dayName + "-" +SECOND_END;

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(SECOND_END))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(SECOND_END)) <= chosenTime) {

                                        Toast.makeText(context,
                                                "The second opening time on " + dayName + " can't be later than or equal to second closing time",
                                                Toast.LENGTH_LONG).show();

                                        Log.d(TAG, "start time for second start time at: " + dayName + " can't be later then or equal to end time");
                                        return;

                                    }

                                }

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(FIRST_START))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(FIRST_START)) >= chosenTime) {

                                        Toast.makeText(context,
                                                "The second opening time on " + dayName + " can't be earlier than or equal to first opening time",
                                                Toast.LENGTH_LONG).show();

                                        Log.d(TAG, "start time for second start time at: " + dayName + " can't be earlier than or equal to first start time");
                                        return;

                                    }

                                }

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(FIRST_END))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(FIRST_END)) >= chosenTime) {

                                        Toast.makeText(context,
                                                "The second opening time on " + dayName + " can't be earlier than or equal to first closing time",
                                                Toast.LENGTH_LONG).show();


                                        Log.d(TAG, "start time for second start time at: " + dayName + " can't be earlier than or equal to first end time");
                                        return;

                                    }

                                }

                                break;

                            case SECOND_END:


                                if (scheduleMap.get(dayName).containsKey(String.valueOf(SECOND_START))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(SECOND_START)) >= chosenTime) {

                                        Toast.makeText(context,
                                                "The second opening time on " + dayName + " can't be earlier than or equal to second closing time",
                                                Toast.LENGTH_LONG).show();

                                        Log.d(TAG, "end time for second start time at: " + dayName + " can't be earlier then or equal to  second end time");
                                        return;

                                    }

                                }

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(FIRST_START))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(FIRST_START)) >= chosenTime) {

                                        Toast.makeText(context,
                                                "The second opening time on " + dayName + " can't be earlier than or equal to first opening time",
                                                Toast.LENGTH_LONG).show();


                                        Log.d(TAG, "end time for second start time at: " + dayName + " can't be earlier than or equal to first start time");
                                        return;

                                    }

                                }

                                if (scheduleMap.get(dayName).containsKey(String.valueOf(FIRST_END))) {

                                    if ((long) scheduleMap.get(dayName).get(String.valueOf(FIRST_END)) >= chosenTime) {

                                        Toast.makeText(context,
                                                "The second opening time on " + dayName + " can't be earlier than or equal to first closing time",
                                                Toast.LENGTH_LONG).show();

                                        Log.d(TAG, "end time for second start time at: " + dayName + " can't be earlier than or equal to first end time");
                                        return;

                                    }

                                }


                                break;

                        }

                        timeTv.setText(dateFormat.format(new Date(chosenTime)));


                        scheduleMap.get(dayName).put(String.valueOf(type), chosenTime);

                    }
                },
                        preSelectedHours != -1 ? preSelectedHours : 8, preSelectedMinutes != -1 ? preSelectedMinutes : 8, false);

        timePickerDialog.show();


    }

    public class DayScheduleVh extends RecyclerView.ViewHolder implements View.OnClickListener,
            CompoundButton.OnCheckedChangeListener {

        private final TextView dayNameTv, dayFirstStartTime, dayFirstEndTime,
                daySecondStartTime, daySecondEndTime;
        private final CheckBox closedCheckBox;

        public DayScheduleVh(@NonNull View itemView) {
            super(itemView);
            dayNameTv = itemView.findViewById(R.id.dayNameTv);
            dayFirstStartTime = itemView.findViewById(R.id.dayFirstStartTime);
            dayFirstEndTime = itemView.findViewById(R.id.dayFirstEndTime);
            daySecondStartTime = itemView.findViewById(R.id.daySecondStartTime);
            daySecondEndTime = itemView.findViewById(R.id.daySecondEndTime);
            closedCheckBox = itemView.findViewById(R.id.closedCheckBox);
        }

        private void bind(int position) {

            if (position == scheduleMap.size() - 1) {
                itemView.findViewById(R.id.seperatorView).setVisibility(View.INVISIBLE);
            }

            dayNameTv.setText(keyArray[position].split("-")[1]);

            dayFirstStartTime.setOnClickListener(this);
            dayFirstEndTime.setOnClickListener(this);
            daySecondStartTime.setOnClickListener(this);
            daySecondEndTime.setOnClickListener(this);
            closedCheckBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == dayFirstStartTime.getId()) {

                pickTime(context, dayFirstStartTime, FIRST_START, getAdapterPosition());

            } else if (v.getId() == dayFirstEndTime.getId()) {

                pickTime(context, dayFirstEndTime, FIRST_END, getAdapterPosition());

            } else if (v.getId() == daySecondStartTime.getId()) {

                pickTime(context, daySecondStartTime, SECOND_START, getAdapterPosition());

            } else if (v.getId() == daySecondEndTime.getId()) {

                pickTime(context, daySecondEndTime, SECOND_END, getAdapterPosition());

            }

        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

//      if(!scheduleMap.containsKey(keyArray[getAdapterPosition()])){
//
////       final Map<String,Object> map = new HashMap<>();
////        map.put("isClosed",isChecked);
//        scheduleMap.get(keyArray[getAdapterPosition()]).put("isClosed",);
//
//      }else{
            Log.d(TAG, keyArray[getAdapterPosition()] + " is closed: " + isChecked);
            scheduleMap.get(keyArray[getAdapterPosition()]).put("isClosed", isChecked);
//      }

            dayFirstStartTime.setClickable(!isChecked);
            dayFirstEndTime.setClickable(!isChecked);
            daySecondStartTime.setClickable(!isChecked);
            daySecondEndTime.setClickable(!isChecked);

            int color;
            if (isChecked) {

                if (greyColor == 0) {
                    greyColor = ResourcesCompat.getColor(context.getResources(), R.color.light_grey, null);
                }

                color = greyColor;
            } else {

                if (blackColor == 0) {
                    blackColor = ResourcesCompat.getColor(context.getResources(), R.color.black, null);
                }

                color = blackColor;
            }

            dayFirstStartTime.setTextColor(color);
            dayFirstEndTime.setTextColor(color);
            daySecondStartTime.setTextColor(color);
            daySecondEndTime.setTextColor(color);


        }
    }
}
