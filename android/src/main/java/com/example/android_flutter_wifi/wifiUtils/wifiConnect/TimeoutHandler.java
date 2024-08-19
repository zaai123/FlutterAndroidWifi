package com.example.android_flutter_wifi.wifiUtils.wifiConnect;

import static com.example.android_flutter_wifi.wifiUtils.ConnectorUtils.isAlreadyConnected;
import static com.example.android_flutter_wifi.wifiUtils.ConnectorUtils.reEnableNetworkIfPossible;
import static com.example.android_flutter_wifi.wifiUtils.WifiUtils.wifiLog;
import static com.example.android_flutter_wifi.wifiUtils.utils.Elvis.of;
import static com.example.android_flutter_wifi.wifiUtils.utils.VersionUtils.isAndroidQOrLater;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;

import com.example.android_flutter_wifi.wifiUtils.WeakHandler;


public class TimeoutHandler {
    private final WifiManager mWifiManager;
    private final WeakHandler mHandler;
    private final WifiConnectionCallback mWifiConnectionCallback;
    private ScanResult mScanResult;

    private final Runnable timeoutCallback = new Runnable() {
        @Override
        public void run() {
            wifiLog("Connection Timed out...");

            if (!isAndroidQOrLater()) {
                reEnableNetworkIfPossible(mWifiManager, mScanResult);
            }
            if (isAlreadyConnected(mWifiManager, of(mScanResult).next(scanResult -> scanResult.BSSID).get())) {
                mWifiConnectionCallback.successfulConnect();
            } else {
                mWifiConnectionCallback.errorConnect(ConnectionErrorCode.TIMEOUT_OCCURRED);
            }

            mHandler.removeCallbacks(this);
        }
    };

    public TimeoutHandler(@NonNull WifiManager wifiManager, @NonNull WeakHandler handler, @NonNull final WifiConnectionCallback wifiConnectionCallback) {
        this.mWifiManager = wifiManager;
        this.mHandler = handler;
        this.mWifiConnectionCallback = wifiConnectionCallback;
    }

    public void startTimeout(final ScanResult scanResult, final long timeout) {
        // cleanup previous connection timeout handler
        mHandler.removeCallbacks(timeoutCallback);

        mScanResult = scanResult;
        mHandler.postDelayed(timeoutCallback, timeout);
    }

    public void stopTimeout() {
        mHandler.removeCallbacks(timeoutCallback);
    }
}
