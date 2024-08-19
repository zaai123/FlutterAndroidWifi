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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
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
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context mContext;
    WifiManager wifiManager;
    ConnectivityManager cm;
    BroadcastReceiver wifiReceiver;
    boolean success = false;
    HashMap connectingWifiHashMap = new HashMap();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "android_flutter_wifi");
        channel.setMethodCallHandler(this);
        mContext = flutterPluginBinding.getApplicationContext();
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
        cm = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiUtils.withContext(mContext).scanWifi(scanResults -> {
            //Log.e("Scan list length: ", String.valueOf(scanResults.size()));
        });
        intiBroadcast();
        mContext.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void intiBroadcast() {

        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    success = wifiManager.startScan();
                } else {
                    success = true;
                }
            }
        };
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
        } else if (call.method.equals(GET_DHCP_INFO)) {
            result.success(getDhcpInfo());
        } else if (call.method.equals(CONNECT_TO_NETWORK)) {
            String ssid = call.argument("ssid");
            String password = call.argument("password");
            Log.e("Arguments: ", "Ssid: " + ssid + ", Password: " + password);
            try {
                result.success(connectToNetwork(ssid, password));
            } catch (InterruptedException e) {
                if (e.getMessage() != null) {
                    Log.e("Error in connecting", e.getMessage());
                }
                e.printStackTrace();
            }
        } else if (call.method.equals(CHECK_CONNECTING_NETWORK_STATUS)) {
            String ssid = call.argument("ssid");
            result.success(checkConnectingStatus(ssid));
        } else if (call.method.equals(FORGET_WIFI)) {
            String ssid = call.argument("ssid");
            result.success(forgetWifi(ssid));
        } else if (call.method.equals(GET_CONFIGURED_NETWORKS)) {
            result.success(getConfiguredNetworks());
        } else if (call.method.equals(CHECK_IF_NETWORK_EXISTS)) {
            String ssid = call.argument("ssid");
            result.success(checkIfNetworkExists(ssid));
        } else if (call.method.equals(FORGET_WIFI_WITH_SSID)) {
            String ssid = call.argument("ssid");
            result.success(forgetWifiWithSSID(ssid));
        } else if (call.method.equals(CONNECT_SAVED_NETWORK)) {
            String ssid = call.argument("ssid");
            result.success(connectToSavedNetwork(ssid));
        } else if (call.method.equals(CHECK_SSID_CONNECTION)) {
            String ssid = call.argument("ssid");
            result.success(checkSsidConnection(ssid));
        } else {
            result.notImplemented();
        }
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        }
    }

    private boolean connectToSavedNetwork(String ssid) {
        Log.e("Enter here", "Enter here in connecting");
        int netId = -1;
        boolean result = false;
        for (WifiConfiguration tmp : wifiManager.getConfiguredNetworks())
            if (tmp.SSID.equalsIgnoreCase("\"" + ssid + "\"")) {
                Log.e("Enter here", "Enter here in connecting");
                netId = tmp.networkId;
                result = wifiManager.enableNetwork(netId, true);
                break;
            }
        return result;
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
            Map<String, String> map = new HashMap<>();
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

    private Map<String, String> getDhcpInfo() {
        if (wifiManager != null) {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            Map<String, String> map = new HashMap<>();
            map.put("ip_address", String.valueOf(dhcpInfo.ipAddress));
            map.put("gateway", String.valueOf(dhcpInfo.gateway));
            map.put("net_mask", String.valueOf(dhcpInfo.netmask));
            map.put("dns_1", String.valueOf(dhcpInfo.dns1));
            map.put("dns_2", String.valueOf(dhcpInfo.dns2));
            map.put("server_address", String.valueOf(dhcpInfo.serverAddress));
            map.put("lease_duration", String.valueOf(dhcpInfo.leaseDuration));
            map.put("gateway_ip", String.valueOf(longToIp(dhcpInfo.gateway)));
            return map;
        } else {
            return null;
        }
    }

    public List<Map<String, String>> getWifiScanResult() {
        List<ScanResult> wifiList = wifiManager.getScanResults();
        List<Map<String, String>> wifiMapList = new ArrayList<>();

        for (int i = 0; i < wifiList.size(); i++) {
            ScanResult scanResult = wifiList.get(i);
            Map<String, String> map = new HashMap<>();
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

    public String longToIp(long ip) {
        StringBuilder result = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {

            result.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                result.insert(0, '.');
            }

            ip = ip >> 8;
        }
        //Log.e("Ip address", result.reverse().toString());
        return result.reverse().toString();
    }

    public static InetAddress intToInetAddress(int hostAddress) throws UnknownHostException {


        byte[] bytes = BigInteger.valueOf(hostAddress).toByteArray();

        bytes = reverse(bytes);
// then
        InetAddress myaddr = InetAddress.getByAddress(bytes);
        return myaddr;
        //return  ipString;
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
                //Log.e("Connection result", "Successfully connected");
            }

            @Override
            public void failed(@NonNull ConnectionErrorCode errorCode) {
                connectingWifiHashMap.put("success", true);
                connectingWifiHashMap.put("error", "no_error");
                //Log.e("Connection error", errorCode.toString());
            }
        }).start();
        //wait(10000);
        boolean checkIfConnected = WifiUtils.withContext(mContext).isWifiConnected(ssid);
        //Log.e("Check if connected", String.valueOf(checkIfConnected));
        return checkIfConnected;

        //Log.e(TAG, "Connecting to SSID \"" + ssid + "\" with password \"" + password + "\" and with security \"" + security + "\" ...");

        // You need to create WifiConfiguration instance like this:

    }

    public boolean checkConnectingStatus(String ssid) {
        return WifiUtils.withContext(mContext).isWifiConnected(ssid);
    }


    public boolean forgetWifi(String ssid) {
        WifiUtils.withContext(mContext).remove(ssid, new RemoveSuccessListener() {
            @Override
            public void success() {
                Log.d("Network remove", "Network removed successfully");
            }

            @Override
            public void failed(@NonNull RemoveErrorCode errorCode) {
                Log.d("Network remove error", errorCode.toString());
            }
        });
        return true;
    }

    public boolean checkSsidConnection(String ssid) {
        boolean isConnected = WifiUtils.withContext(mContext).isWifiConnected(ssid);
        return isConnected;
    }

    public boolean forgetWifiWithSSID(String ssid) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Log.e("Enter here", "Enter here in version R");
            List<WifiNetworkSuggestion> suggestionList = wifiManager.getNetworkSuggestions();
            for (WifiNetworkSuggestion wifiNetworkSuggestion : suggestionList) {
                if (wifiNetworkSuggestion.getSsid().equalsIgnoreCase(ssid)) {
                    List<WifiNetworkSuggestion> removeAbleList = new ArrayList<>();
                    int removeResult = wifiManager.removeNetworkSuggestions(removeAbleList);
                    //Log.e("Remove result ", String.valueOf(removeResult));
                    return true;
                }
            }
        } else {
            //Log.e("Enter here", "Not entering in version R");
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration wifiConfiguration : list) {
                if (wifiConfiguration.SSID.equalsIgnoreCase(ssid)) {
                    /*Log.e("Wifi configuration network id: ",
                            String.valueOf(wifiConfiguration.networkId));*/
                    try {
                        result = wifiManager.removeNetwork(wifiConfiguration.networkId);
                        wifiManager.saveConfiguration();
                    } catch (Exception e) {
                        if (e.getMessage() != null) {
                            Log.e("Error in removing wifi", e.getMessage());
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    public boolean checkIfNetworkExists(String ssid) {
        boolean isExists = false;
        List<WifiConfiguration> savedList = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : savedList) {
            if (wifiConfiguration.SSID.equalsIgnoreCase(ssid)) {
                isExists = true;
                break;
            } else {
                isExists = false;
            }
        }
        return isExists;
    }

    public List<Map<String, String>> getConfiguredNetworks() {
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        List<Map<String, String>> configuredList = new ArrayList<>();
        for (WifiConfiguration i : list) {
            Log.e("Network id: ", String.valueOf(i.networkId));
            Log.e("ssid: ", String.valueOf(i.SSID));
            //wifiManager.saveConfiguration();
            HashMap map = new HashMap();
            map.put("id", String.valueOf(i.networkId));
            map.put("ssid", i.SSID);
            map.put("hidden_ssid", String.valueOf(i.hiddenSSID));
            map.put("status", String.valueOf(i.status));

            configuredList.add(map);
        }
        //Log.e("List length: ", String.valueOf(configuredList.size()));
        return configuredList;
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
