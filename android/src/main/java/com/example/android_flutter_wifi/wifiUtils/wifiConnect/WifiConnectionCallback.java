package com.example.android_flutter_wifi.wifiUtils.wifiConnect;

import androidx.annotation.NonNull;

public interface WifiConnectionCallback {
    void successfulConnect();

    void errorConnect(@NonNull ConnectionErrorCode connectionErrorCode);
}
