package com.example.android_flutter_wifi.wifiUtils.wifiRemove;

import androidx.annotation.NonNull;

public interface RemoveSuccessListener {
    void success();

    void failed(@NonNull RemoveErrorCode errorCode);
}
