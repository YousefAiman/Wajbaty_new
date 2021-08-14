package com.developers.wajbaty.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeFormatter {

//  public static final long SECOND_MILLIS = 1000,
//          MINUTE_MILLIS = 60 * SECOND_MILLIS,
//          HOUR_MILLIS = 60 * MINUTE_MILLIS,
//          DAY_MILLIS = 24 * HOUR_MILLIS,
//          WEEK_MILLIS = 7 * DAY_MILLIS,
//          MONTH_MILLIS = 30 * DAY_MILLIS,
//          YEAR_MILLIS = 12 * MONTH_MILLIS;

    public static final String
            HOUR_MINUTE = "h:mm a",
            WEEK_DAY_HOUR_MINUTE = "EEE h:mm a",
            DAY_HOUR_MINUTE = "dd h:mm a",
            MONTH_DAY_HOUR_MINUTE = "MMM dd h:mm a",
            MONTH_DAY_YEAR_HOUR_MINUTE = "dd/MM/yyyy h:mm a";

    public static String formatTime(long time) {

        String format;

        Calendar currentCalendar = Calendar.getInstance();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        if (calendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)) {
            format = MONTH_DAY_YEAR_HOUR_MINUTE;
        } else if (calendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)
                || calendar.get(Calendar.DAY_OF_MONTH) != currentCalendar.get(Calendar.DAY_OF_MONTH)) {
            format = MONTH_DAY_HOUR_MINUTE;
        } else if (calendar.get(Calendar.DAY_OF_WEEK) != currentCalendar.get(Calendar.DAY_OF_WEEK)) {
            format = WEEK_DAY_HOUR_MINUTE;
        } else {
            format = HOUR_MINUTE;
        }


        return new SimpleDateFormat(format, Locale.getDefault()).format(time);
    }


    public static String formatWithPattern(long time, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(time);
    }

}
