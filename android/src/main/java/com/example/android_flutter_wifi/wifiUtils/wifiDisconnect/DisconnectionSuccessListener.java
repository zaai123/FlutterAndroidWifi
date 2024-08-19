package com.example.android_flutter_wifi.wifiUtils.wifiDisconnect;

import androidx.annotation.NonNull;

public interface DisconnectionSuccessListener {
    void success();

    void failed(@NonNull DisconnectionErrorCode errorCode);
}