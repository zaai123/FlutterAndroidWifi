package com.example.android_flutter_wifi;

import static com.example.android_flutter_wifi.constants.Constants.DISABLE_WIFI_CALL;
import static com.example.android_flutter_wifi.constants.Constants.ENABLE_WIFI_CALL;
import static com.example.android_flutter_wifi.constants.Constants.IS_WIFI_ENABLED_CALL;
import static com.example.android_flutter_wifi.constants.Constants.WIFI_LIST_CALL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.example.android_flutter_wifi.constants.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * AndroidFlutterWifiPlugin
 */
public class AndroidFlutterWifiPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context mContext;
    WifiManager wifiManager;
    ConnectivityManager cm;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "android_flutter_wifi");
        channel.setMethodCallHandler(this);
        mContext = flutterPluginBinding.getApplicationContext();
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals(Constants.WIFI_INFO_CALL)) {
            result.success(getWifiInfo());
        } else if (call.method.equals(WIFI_LIST_CALL)) {
            result.success(getWifiScanResult());
        } else if (call.method.equals(Constants.IS_CONNECTED_CALL)) {
            result.success(isConnected());
        } else if (call.method.equals(Constants.IS_CONNECTION_FAST)) {
            result.success(isConnectionFast());
        } else if (call.method.equals(Constants.CONNECTION_TYPE_CALL)) {
            result.success(getConnectionType());
        } else if (call.method.equals(ENABLE_WIFI_CALL)) {
            enableWifi();
        } else if (call.method.equals(DISABLE_WIFI_CALL)) {
            disableWifi();
        } else if (call.method.equals(IS_WIFI_ENABLED_CALL)) {
            result.success(isWifiEnabled());
        } else {
            result.notImplemented();
        }
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        }
    }

    private boolean isConnectionFast() {
        Network network = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            network = cm.getActiveNetwork();
            int downSpeed = cm.getNetworkCapabilities(network).getLinkDownstreamBandwidthKbps();
            int upSpeed = cm.getNetworkCapabilities(network).getLinkUpstreamBandwidthKbps();
            if (downSpeed <= 550) {
                return false;
            } else {
                return true;
            }
        } else {
            return isConnectionFast(cm.getActiveNetworkInfo().getType(), cm.getActiveNetworkInfo().getSubtype());
        }

    }


    private String getConnectionType() {
        try {
            if (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
                return "wifi";
            } else if (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE) {
                return "mobile";
            } else {
                return "Not found";
            }
        } catch (Exception e) {
            return "Not Found";
        }
    }

    public boolean isConnected() {
        try {
            return cm.getActiveNetworkInfo().isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, String> getWifiInfo() {
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            Map<String, String> map = new HashMap<String, String>();
            map.put("ssid", wifiInfo.getSSID());
            map.put("mac", wifiInfo.getBSSID());
            map.put("ip", String.valueOf(wifiInfo.getIpAddress()));
            map.put("link_speed", String.valueOf(wifiInfo.getLinkSpeed()));
            map.put("network_id", String.valueOf(wifiInfo.getNetworkId()));
            return map;
        } else {
            return null;
        }
    }

    public List<Map<String, String>> getWifiScanResult() {
        List<ScanResult> wifiList = wifiManager.getScanResults();
        List<Map<String, String>> wifiMapList = new ArrayList<Map<String, String>>();

        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult scanResult = wifiList.get(i);
            Map<String, String> map = new HashMap<String, String>();
            map.put("ssid", scanResult.SSID);
            map.put("bssid", scanResult.BSSID);
            map.put("security", scanResult.capabilities);
            map.put("signal_level", String.valueOf(calculateSignalLevel(scanResult.level, 5) + 1));
            map.put("frequency", String.valueOf(scanResult.frequency));
            map.put("level", String.valueOf(scanResult.level));
            wifiMapList.add(map);
        }
        return wifiMapList;
    }

    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public void enableWifi() {
        wifiManager.setWifiEnabled(true);

    }

    public void disableWifi() {
        wifiManager.setWifiEnabled(false);
    }

    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public int calculateSignalLevel(int rssi, int numLevels) {
        if (rssi <= -100) {
            return 0;
        } else if (rssi >= -55) {
            return numLevels - 1;
        } else {
            float inputRange = (-55 - -100);
            float outputRange = (numLevels - 1);
            if (inputRange != 0)
                return (int) ((float) (rssi - -100) * outputRange / inputRange);
        }
        return 0;
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
