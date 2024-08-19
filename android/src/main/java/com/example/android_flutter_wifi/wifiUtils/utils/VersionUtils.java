package com.example.android_flutter_wifi.wifiUtils.utils;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

public class VersionUtils {
    private VersionUtils() {

    }

    public static boolean isJellyBeanOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean isLollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isMarshmallowOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isAndroidQOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean is29AndAbove(){
        return  Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static Intent getPanelIntent(){
        return new Intent(Settings.Panel.ACTION_WIFI);
    }
}
