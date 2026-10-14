package com.example.android_flutter_wifi;

import static android.content.Context.WIFI_SERVICE;
import static com.example.android_flutter_wifi.constants.Constants.CHECK_CONNECTING_NETWORK_STATUS;
import static com.example.android_flutter_wifi.constants.Constants.CHECK_IF_NETWORK_EXISTS;
import static com.example.android_flutter_wifi.constants.Constants.CHECK_SSID_CONNECTION;
import static com.example.android_flutter_wifi.constants.Constants.CONNECT_SAVED_NETWORK;
import static com.example.android_flutter_wifi.constants.Constants.CONNECT_TO_NETWORK;
import static com.example.android_flutter_wifi.constants.Constants.DISABLE_WIFI_CALL;
import static com.example.android_flutter_wifi.constants.Constants.ENABLE_WIFI_CALL;
import static com.example.android_flutter_wifi.constants.Constants.FORGET_WIFI;
import static com.example.android_flutter_wifi.constants.Constants.FORGET_WIFI_WITH_SSID;
import static com.example.android_flutter_wifi.constants.Constants.GET_CONFIGURED_NETWORKS;
import static com.example.android_flutter_wifi.constants.Constants.GET_DHCP_INFO;
import static com.example.android_flutter_wifi.constants.Constants.IS_WIFI_ENABLED_CALL;
import static com.example.android_flutter_wifi.constants.Constants.WIFI_LIST_CALL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.example.android_flutter_wifi.constants.Constants;
import com.example.android_flutter_wifi.wifiUtils.WifiUtils;
import com.example.android_flutter_wifi.wifiUtils.wifiConnect.ConnectionErrorCode;
import com.example.android_flutter_wifi.wifiUtils.wifiConnect.ConnectionSuccessListener;
import com.example.android_flutter_wifi.wifiUtils.wifiRemove.RemoveErrorCode;
import com.example.android_flutter_wifi.wifiUtils.wifiRemove.RemoveSuccessListener;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * AndroidFlutterWifiPlugin
 */
public class AndroidFlutterWifiPlugin implements FlutterPlugin, MethodCallHandler {
    private static final String TAG = "AndroidFlutterWifi";

    private MethodChannel channel;
    private Context mContext;
    private WifiManager wifiManager;
    private ConnectivityManager cm;
    private HandlerThread workerThread;
    private Handler workerHandler;
    private Handler mainHandler;
    private final HashMap<String, Object> connectingWifiHashMap = new HashMap<>();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "android_flutter_wifi");
        channel.setMethodCallHandler(this);
        mContext = flutterPluginBinding.getApplicationContext();
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
        cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Dedicated background thread with Looper for all binder calls & Wi-Fi operations
        workerThread = new HandlerThread("AndroidFlutterWifiWorker");
        workerThread.start();
        workerHandler = new Handler(workerThread.getLooper());
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (workerHandler == null) {
            result.error("UNAVAILABLE", "Worker thread is not available", null);
            return;
        }

        // Offload all Binder IPC calls to the background thread to prevent UI thread ANRs
        workerHandler.post(() -> {
            try {
                handleMethodCall(call, result);
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception in method call " + call.method, e);
                mainHandler.post(() -> result.error("EXCEPTION", e.getMessage(), null));
            }
        });
    }

    private void handleMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals(Constants.WIFI_INFO_CALL)) {
            Map<String, String> info = getWifiInfo();
            mainHandler.post(() -> result.success(info));
        } else if (call.method.equals(WIFI_LIST_CALL)) {
            List<Map<String, String>> list = getWifiScanResult();
            mainHandler.post(() -> result.success(list));
        } else if (call.method.equals(Constants.IS_CONNECTED_CALL)) {
            boolean connected = isConnected();
            mainHandler.post(() -> result.success(connected));
        } else if (call.method.equals(Constants.IS_CONNECTION_FAST)) {
            boolean fast = isConnectionFast();
            mainHandler.post(() -> result.success(fast));
        } else if (call.method.equals(Constants.CONNECTION_TYPE_CALL)) {
            String type = getConnectionType();
            mainHandler.post(() -> result.success(type));
        } else if (call.method.equals(ENABLE_WIFI_CALL)) {
            enableWifi();
            mainHandler.post(() -> result.success(null));
        } else if (call.method.equals(DISABLE_WIFI_CALL)) {
            disableWifi();
            mainHandler.post(() -> result.success(null));
        } else if (call.method.equals(IS_WIFI_ENABLED_CALL)) {
            boolean enabled = isWifiEnabled();
            mainHandler.post(() -> result.success(enabled));
        } else if (call.method.equals(GET_DHCP_INFO)) {
            Map<String, String> dhcp = getDhcpInfo();
            mainHandler.post(() -> result.success(dhcp));
        } else if (call.method.equals(CONNECT_TO_NETWORK)) {
            String ssid = call.argument("ssid");
            String password = call.argument("password");
            Log.d(TAG, "Connect arguments: Ssid: " + ssid);
            try {
                boolean connected = connectToNetwork(ssid, password);
                mainHandler.post(() -> result.success(connected));
            } catch (InterruptedException e) {
                Log.e(TAG, "Error in connecting: " + e.getMessage(), e);
                mainHandler.post(() -> result.error("CONNECT_ERROR", e.getMessage(), null));
            }
        } else if (call.method.equals(CHECK_CONNECTING_NETWORK_STATUS)) {
            String ssid = call.argument("ssid");
            boolean status = checkConnectingStatus(ssid);
            mainHandler.post(() -> result.success(status));
        } else if (call.method.equals(FORGET_WIFI)) {
            String ssid = call.argument("ssid");
            boolean res = forgetWifi(ssid);
            mainHandler.post(() -> result.success(res));
        } else if (call.method.equals(GET_CONFIGURED_NETWORKS)) {
            List<Map<String, String>> configured = getConfiguredNetworks();
            mainHandler.post(() -> result.success(configured));
        } else if (call.method.equals(CHECK_IF_NETWORK_EXISTS)) {
            String ssid = call.argument("ssid");
            boolean exists = checkIfNetworkExists(ssid);
            mainHandler.post(() -> result.success(exists));
        } else if (call.method.equals(FORGET_WIFI_WITH_SSID)) {
            String ssid = call.argument("ssid");
            boolean res = forgetWifiWithSSID(ssid);
            mainHandler.post(() -> result.success(res));
        } else if (call.method.equals(CONNECT_SAVED_NETWORK)) {
            String ssid = call.argument("ssid");
            boolean res = connectToSavedNetwork(ssid);
            mainHandler.post(() -> result.success(res));
        } else if (call.method.equals(CHECK_SSID_CONNECTION)) {
            String ssid = call.argument("ssid");
            boolean res = checkSsidConnection(ssid);
            mainHandler.post(() -> result.success(res));
        } else if (call.method.equals("getPlatformVersion")) {
            mainHandler.post(() -> result.success("Android " + android.os.Build.VERSION.RELEASE));
        } else {
            mainHandler.post(result::notImplemented);
        }
    }

    private boolean connectToSavedNetwork(String ssid) {
        if (wifiManager == null || ssid == null) return false;
        try {
            List<WifiConfiguration> configured = wifiManager.getConfiguredNetworks();
            if (configured != null) {
                for (WifiConfiguration tmp : configured) {
                    if (tmp != null && tmp.SSID != null && tmp.SSID.equalsIgnoreCase("\"" + ssid + "\"")) {
                        return wifiManager.enableNetwork(tmp.networkId, true);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in connectToSavedNetwork", e);
        }
        return false;
    }

    private boolean isConnectionFast() {
        if (cm == null) return false;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network == null) return false;
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                if (capabilities == null) return false;
                int downSpeed = capabilities.getLinkDownstreamBandwidthKbps();
                return downSpeed > 550;
            } else {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo == null) return false;
                return isConnectionFast(networkInfo.getType(), networkInfo.getSubtype());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in isConnectionFast", e);
            return false;
        }
    }

    private String getConnectionType() {
        if (cm == null) return "Not found";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network == null) return "Not found";
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                if (capabilities == null) return "Not found";
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return "wifi";
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return "mobile";
                } else {
                    return "Not found";
                }
            } else {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo == null) return "Not found";
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "wifi";
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return "mobile";
                } else {
                    return "Not found";
                }
            }
        } catch (Exception e) {
            return "Not Found";
        }
    }

    public boolean isConnected() {
        if (cm == null) return false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = cm.getActiveNetwork();
                if (network == null) return false;
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else {
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, String> getWifiInfo() {
        Map<String, String> map = new HashMap<>();
        if (wifiManager != null) {
            try {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    map.put("ssid", wifiInfo.getSSID() != null ? wifiInfo.getSSID() : "");
                    map.put("mac", wifiInfo.getBSSID() != null ? wifiInfo.getBSSID() : "");
                    map.put("ip", String.valueOf(wifiInfo.getIpAddress()));
                    map.put("link_speed", String.valueOf(wifiInfo.getLinkSpeed()));
                    map.put("network_id", String.valueOf(wifiInfo.getNetworkId()));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in getWifiInfo", e);
            }
        }
        return map;
    }

    private Map<String, String> getDhcpInfo() {
        Map<String, String> map = new HashMap<>();
        if (wifiManager != null) {
            try {
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                if (dhcpInfo != null) {
                    map.put("ip_address", String.valueOf(dhcpInfo.ipAddress));
                    map.put("gateway", String.valueOf(dhcpInfo.gateway));
                    map.put("net_mask", String.valueOf(dhcpInfo.netmask));
                    map.put("dns_1", String.valueOf(dhcpInfo.dns1));
                    map.put("dns_2", String.valueOf(dhcpInfo.dns2));
                    map.put("server_address", String.valueOf(dhcpInfo.serverAddress));
                    map.put("lease_duration", String.valueOf(dhcpInfo.leaseDuration));
                    map.put("gateway_ip", String.valueOf(longToIp(dhcpInfo.gateway)));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in getDhcpInfo", e);
            }
        }
        return map;
    }

    public List<Map<String, String>> getWifiScanResult() {
        List<Map<String, String>> wifiMapList = new ArrayList<>();
        if (wifiManager == null) {
            return wifiMapList;
        }

        try {
            List<ScanResult> wifiList = wifiManager.getScanResults();
            if (wifiList != null) {
                for (int i = 0; i < wifiList.size(); i++) {
                    ScanResult scanResult = wifiList.get(i);
                    if (scanResult == null) continue;
                    Map<String, String> map = new HashMap<>();
                    map.put("ssid", scanResult.SSID != null ? scanResult.SSID : "");
                    map.put("bssid", scanResult.BSSID != null ? scanResult.BSSID : "");
                    map.put("security", scanResult.capabilities != null ? scanResult.capabilities : "");
                    map.put("signal_level", String.valueOf(calculateSignalLevel(scanResult.level, 5) + 1));
                    map.put("frequency", String.valueOf(scanResult.frequency));
                    map.put("level", String.valueOf(scanResult.level));
                    wifiMapList.add(map);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getWifiScanResult", e);
        }
        return wifiMapList;
    }

    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return true;
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public void enableWifi() {
        if (wifiManager != null) {
            try {
                wifiManager.setWifiEnabled(true);
            } catch (Exception e) {
                Log.e(TAG, "Error enabling wifi", e);
            }
        }
    }

    public void disableWifi() {
        if (wifiManager != null) {
            try {
                wifiManager.setWifiEnabled(false);
            } catch (Exception e) {
                Log.e(TAG, "Error disabling wifi", e);
            }
        }
    }

    public boolean isWifiEnabled() {
        if (wifiManager != null) {
            try {
                return wifiManager.isWifiEnabled();
            } catch (Exception e) {
                Log.e(TAG, "Error checking if wifi is enabled", e);
            }
        }
        return false;
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

    public String longToIp(long ip) {
        StringBuilder result = new StringBuilder(15);
        for (int i = 0; i < 4; i++) {
            result.insert(0, Long.toString(ip & 0xff));
            if (i < 3) {
                result.insert(0, '.');
            }
            ip = ip >> 8;
        }
        return result.reverse().toString();
    }

    public static InetAddress intToInetAddress(int hostAddress) throws UnknownHostException {
        byte[] bytes = BigInteger.valueOf(hostAddress).toByteArray();
        bytes = reverse(bytes);
        return InetAddress.getByAddress(bytes);
    }

    public static byte[] reverse(byte[] array) {
        if (array == null) {
            return null;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
        return array;
    }

    public boolean connectToNetwork(String ssid, String password) throws InterruptedException {
        WifiUtils.withContext(mContext).connectWith(ssid, password).onConnectionResult(new ConnectionSuccessListener() {
            @Override
            public void success() {
                connectingWifiHashMap.put("success", true);
                connectingWifiHashMap.put("error", "no_error");
            }

            @Override
            public void failed(@NonNull ConnectionErrorCode errorCode) {
                connectingWifiHashMap.put("success", true);
                connectingWifiHashMap.put("error", "no_error");
            }
        }).start();

        return WifiUtils.withContext(mContext).isWifiConnected(ssid);
    }

    public boolean checkConnectingStatus(String ssid) {
        return WifiUtils.withContext(mContext).isWifiConnected(ssid);
    }

    public boolean forgetWifi(String ssid) {
        WifiUtils.withContext(mContext).remove(ssid, new RemoveSuccessListener() {
            @Override
            public void success() {
                Log.d(TAG, "Network removed successfully");
            }

            @Override
            public void failed(@NonNull RemoveErrorCode errorCode) {
                Log.d(TAG, "Network remove error: " + errorCode);
            }
        });
        return true;
    }

    public boolean checkSsidConnection(String ssid) {
        return WifiUtils.withContext(mContext).isWifiConnected(ssid);
    }

    public boolean forgetWifiWithSSID(String ssid) {
        if (wifiManager == null || ssid == null) return false;
        boolean result = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                List<WifiNetworkSuggestion> suggestionList = wifiManager.getNetworkSuggestions();
                if (suggestionList != null) {
                    for (WifiNetworkSuggestion wifiNetworkSuggestion : suggestionList) {
                        if (wifiNetworkSuggestion != null && wifiNetworkSuggestion.getSsid() != null && wifiNetworkSuggestion.getSsid().equalsIgnoreCase(ssid)) {
                            List<WifiNetworkSuggestion> removeAbleList = new ArrayList<>();
                            removeAbleList.add(wifiNetworkSuggestion);
                            wifiManager.removeNetworkSuggestions(removeAbleList);
                            return true;
                        }
                    }
                }
            } else {
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                if (list != null) {
                    for (WifiConfiguration wifiConfiguration : list) {
                        if (wifiConfiguration != null && wifiConfiguration.SSID != null && wifiConfiguration.SSID.equalsIgnoreCase(ssid)) {
                            try {
                                result = wifiManager.removeNetwork(wifiConfiguration.networkId);
                                wifiManager.saveConfiguration();
                            } catch (Exception e) {
                                Log.e(TAG, "Error in removing wifi: " + e.getMessage(), e);
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in forgetWifiWithSSID", e);
        }
        return result;
    }

    public boolean checkIfNetworkExists(String ssid) {
        if (wifiManager == null || ssid == null) return false;
        try {
            List<WifiConfiguration> savedList = wifiManager.getConfiguredNetworks();
            if (savedList != null) {
                for (WifiConfiguration wifiConfiguration : savedList) {
                    if (wifiConfiguration != null && wifiConfiguration.SSID != null && wifiConfiguration.SSID.equalsIgnoreCase(ssid)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in checkIfNetworkExists", e);
        }
        return false;
    }

    public List<Map<String, String>> getConfiguredNetworks() {
        List<Map<String, String>> configuredList = new ArrayList<>();
        if (wifiManager == null) return configuredList;
        try {
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            if (list != null) {
                for (WifiConfiguration i : list) {
                    if (i == null) continue;
                    HashMap<String, String> map = new HashMap<>();
                    map.put("id", String.valueOf(i.networkId));
                    map.put("ssid", i.SSID != null ? i.SSID : "");
                    map.put("hidden_ssid", String.valueOf(i.hiddenSSID));
                    map.put("status", String.valueOf(i.status));
                    configuredList.add(map);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getConfiguredNetworks", e);
        }
        return configuredList;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        if (channel != null) {
            channel.setMethodCallHandler(null);
            channel = null;
        }
        if (workerThread != null) {
            workerThread.quitSafely();
            workerThread = null;
            workerHandler = null;
        }
    }
}
