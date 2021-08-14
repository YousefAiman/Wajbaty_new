package com.developers.wajbaty.BroadcastReceivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.developers.wajbaty.Activities.MainActivity;
import com.developers.wajbaty.Activities.MessagingActivity;

import java.util.List;

public class NotificationClickReceiver extends BroadcastReceiver {

    private SharedPreferences sharedPreferences;

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.hasExtra("messagingBundle")) {

            if (!isAppIsInBackground(context)) {

                if (sharedPreferences == null) {
                    sharedPreferences = context.getSharedPreferences("Wajbaty", Context.MODE_PRIVATE);
                }

                Intent intent1 = new Intent(context, MessagingActivity.class)
                        .putExtra("messagingBundle",
                                intent.getBundleExtra("messagingBundle"));

                if (sharedPreferences.contains("currentMessagingUserId")) {
                    final Bundle messagingBundle = intent.getBundleExtra("messagingBundle");
                    if (messagingBundle.getString("messagingUID")
                            .equals(sharedPreferences.getString("currentMessagingUserId", "")) &&
                            messagingBundle.getLong("destinationUID") ==
                                    sharedPreferences.getLong("currentMessagingDeliveryID", 0)) {
                        Log.d("ttt", "this messaging activity is already open man");
                        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    } else {
                        Log.d("ttt", "current messaging is not this");
                        intent1.setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                } else {
                    Log.d("ttt", "no current messaging in shared");
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                context.startActivity(intent1);
                Log.d("ttt", "clicked notificaiton while app is running");
            } else {

                context.startActivity(new Intent(context, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("messagingBundle",
                                intent.getBundleExtra("messagingBundle")));

                Log.d("ttt", "clicked notificaiton while app isn't running");
            }


        }

    }

}
