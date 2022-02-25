import 'dart:async';

import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

enum CONNECTION_TYPE { WIFI, MOBILE }

class AndroidFlutterWifi {
  static const MethodChannel _channel = MethodChannel('android_flutter_wifi');
  static const WIFI_INFO_CALL = "getWifiInfo";
  static const WIFI_LIST_CALL = "getWifiList";
  static const IS_CONNECTED_CALL = "isConnected";
  static const IS_CONNECTION_FAST = "isConnectionFast";
  static const CONNECTION_TYPE_CALL = "getConnectionType";

  ///init method call location permission which is necessary
  ///get any information about Wi-FI in android
  static Future<bool?> init() async {
    var isPermissionGranted = await Permission.location.isGranted;
    if (!isPermissionGranted) {
      PermissionStatus permissionStatus = await Permission.location.request();
      if (permissionStatus.isGranted) {
        return true;
      }
    } else {
      return false;
    }
  }

  /// getWifiScanResult return wifi list
  static Future<List<WifiNetwork>> getWifiScanResult() async {
    List<dynamic> wifiMapList = await _channel.invokeMethod(WIFI_LIST_CALL);
    List<WifiNetwork> wifiList =
        wifiMapList.map((e) => WifiNetwork.fromMap(e)).toList();
    return wifiList;
  }

  static Future<bool?> isConnectionFast() async {
    final bool isConnectionFast =
        await _channel.invokeMethod(IS_CONNECTION_FAST);
    return isConnectionFast;
  }

  static Future<bool> isConnected() async {
    final bool isConnected = await _channel.invokeMethod(IS_CONNECTED_CALL);
    return isConnected;
  }

  static Future<ActiveWifiNetwork> getActiveWifiInfo() async {
    Map<dynamic, dynamic> wifiObject =
        await _channel.invokeMethod(WIFI_INFO_CALL);

    ActiveWifiNetwork activeWifiNetwork = ActiveWifiNetwork.fromMap(wifiObject);
    return activeWifiNetwork;
  }

  static Future<CONNECTION_TYPE> getConnectionType() async {
    String connection = await _channel.invokeMethod(CONNECTION_TYPE_CALL);
    if (connection == 'wifi') {
      return CONNECTION_TYPE.WIFI;
    } else {
      return CONNECTION_TYPE.MOBILE;
    }
  }

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}

class WifiNetwork {
  String? ssid;
  String? bssid;
  String? frequency;
  String? level;
  String? security;
  String? signalLevel;

  WifiNetwork(
      {this.ssid,
      this.bssid,
      this.frequency,
      this.level,
      this.security,
      this.signalLevel});

  factory WifiNetwork.fromMap(Map<dynamic, dynamic> map) {
    return WifiNetwork(
        bssid: map['bssid'],
        frequency: map['frequency'],
        level: map['level'],
        security: map['security'],
        ssid: map['ssid'],
        signalLevel: map['signal_level']);
  }
}

class ActiveWifiNetwork {
  String? ip;
  String? bssid;
  String? frequency;
  String? linkSpeed;
  String? networkId;
  String? ssid;

  ActiveWifiNetwork(
      {this.ip,
      this.bssid,
      this.frequency,
      this.linkSpeed,
      this.networkId,
      this.ssid});

  factory ActiveWifiNetwork.fromMap(Map<dynamic, dynamic> map) {
    return ActiveWifiNetwork(
        ssid: map['ssid'],
        frequency: map['frequency'],
        ip: map['ip'],
        linkSpeed: map['link_speed'],
        bssid: map['mac'],
        networkId: map['network_id']);
  }
}
