package com.developers.wajbaty.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class PermissionRequester {


    public static boolean needsToRequestStoragePermissions(int code, Activity activity) {
        final String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(activity, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(permissions, code);
                return false;
            }
        }
        return true;
    }


}
