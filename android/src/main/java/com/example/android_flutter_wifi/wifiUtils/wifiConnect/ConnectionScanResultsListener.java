package com.example.android_flutter_wifi.wifiUtils.wifiConnect;


import android.net.wifi.ScanResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface ConnectionScanResultsListener {
    @Nullable
    ScanResult onConnectWithScanResult(@NonNull List<ScanResult> scanResults);
}
