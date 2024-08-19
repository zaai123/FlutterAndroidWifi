package com.example.android_flutter_wifi.wifiUtils.wifiConnect;

import androidx.annotation.NonNull;

public interface ConnectionSuccessListener {
    void success();

    void failed(@NonNull ConnectionErrorCode errorCode);
}
