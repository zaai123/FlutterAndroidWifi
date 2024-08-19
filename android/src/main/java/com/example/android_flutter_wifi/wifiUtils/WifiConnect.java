package com.example.android_flutter_wifi.wifiUtils;

import android.content.Context;
import android.util.Log;

import com.example.android_flutter_wifi.wifiUtils.wifiConnect.ConnectionSuccessListener;

import java.util.concurrent.CountDownLatch;

public class WifiConnect extends Thread/* implements ConnectionSuccessListener*/ {

    CountDownLatch countDownLatch;
    Context context;
    String ssid;
    String password;

    ConnectionSuccessListener connectionSuccessListener;

    public WifiConnect(CountDownLatch countDownLatch, Context context, String ssid, String password, ConnectionSuccessListener connectionSuccessListener) {
        this.countDownLatch = countDownLatch;
        this.context = context;
        this.ssid = ssid;
        this.password = password;
        this.connectionSuccessListener = connectionSuccessListener;
    }

    @Override
    public void run() {
        //Looper.prepare();
        Log.e("Enter here", "Enter here in the run method");
        WifiUtils.withContext(context).connectWith(ssid, password).onConnectionResult(this.connectionSuccessListener).start();
    }



/*    @Override
    public void success() {
        Log.e("Success", "Success");
        countDownLatch.countDown();
        ;
    }

    @Override
    public void failed(@NonNull ConnectionErrorCode errorCode) {
        Log.e("Success", "Error");
        countDownLatch.countDown();
    }*/
}
