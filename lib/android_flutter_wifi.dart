import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

enum ConnectionType { wifi, mobile }

class AndroidFlutterWifi {
  static const MethodChannel _channel = MethodChannel('android_flutter_wifi');
  static const _wifiInfoCall = "getWifiInfo";
  static const _wifiListCall = "getWifiList";
  static const _isConnectedCall = "isConnected";
  static const _isConnectionFast = "isConnectionFast";
  static const _connectionTypeCall = "getConnectionType";
  static const _enableWifiCall = "enableWifi";
  static const _disableWifiCall = "disableWifi";
  static const _isWifiEnabledCall = "isWifiEnabled";
  static const _getDhcpInfo = "getDhcpInfo";
  static const _connectToNetwork = "connectToNetwork";
  static const _checkConnectingNetwork = "checkConnectingNetworkStatus";
  static const _getConfiguredNetworks = "getConfiguredNetworks";
  static const _forgetWifi = "forgetWifi";
  static const _checkIfNetworkExists = "checkIfNetworkExists";
  static const _forgetWifiWithSSID = "forgetWifiWithSsid";
  static const _connectSavedNetwork = "connectSavedNetwork";
  //static const _checkSsidConnection = "checkSsidConnection";

  ///init method call location permission which is necessary
  ///get any information about Wi-FI in android
  static Future<bool?> init() async {
    return true;
  }

  /// getWifiScanResult return wifi list
  static Future<List<WifiNetwork>> getWifiScanResult() async {
    List<dynamic> wifiMapList = await _channel.invokeMethod(_wifiListCall);
    List<WifiNetwork> wifiList =
        wifiMapList.map((e) => WifiNetwork.fromMap(e)).toList();
    return wifiList;
  }

  static Future<bool?> isConnectionFast() async {
    final bool isConnectionFast =
        await _channel.invokeMethod(_isConnectionFast);
    return isConnectionFast;
  }

  static Future<bool> isConnected() async {
    final bool isConnected = await _channel.invokeMethod(_isConnectedCall);
    return isConnected;
  }

  static Future<ActiveWifiNetwork> getActiveWifiInfo() async {
    Map<dynamic, dynamic> wifiObject =
        await _channel.invokeMethod(_wifiInfoCall);

    ActiveWifiNetwork activeWifiNetwork = ActiveWifiNetwork.fromMap(wifiObject);
    return activeWifiNetwork;
  }

  static Future<ConnectionType> getConnectionType() async {
    String connection = await _channel.invokeMethod(_connectionTypeCall);
    if (connection == 'wifi') {
      return ConnectionType.wifi;
    } else {
      return ConnectionType.mobile;
    }
  }

  static Future<void> enableWifi() async {
    await _channel.invokeMethod(_enableWifiCall);
  }

  static Future<void> disableWifi() async {
    await _channel.invokeMethod(_disableWifiCall);
  }

  static Future<bool> isWifiEnabled() async {
    bool isWifiEnabled = await _channel.invokeMethod(_isWifiEnabledCall);
    return isWifiEnabled;
  }

  static Future<DhcpInfo> getDhcpInfo() async {
    Map<dynamic, dynamic> map = await _channel.invokeMethod(_getDhcpInfo);
    return DhcpInfo.fromMap(map);
  }

  /// Convert integer ipv4 address to understandable string like "127.0.0.1"
  static String toIp(String ip) {
    try {
      int value = int.parse(ip);
      return [
        (value >> 24) & 0xff,
        (value >> 16) & 0xff,
        (value >> 8) & 0xff,
        value & 0xff
      ].join('.');
    } on FormatException {
      // This error is thrown if int. Parse doesn't find an integer in the string given
      throw Exception(
          "This integer can't be proccess by int.parse, please check your value: $ip");
    }
  }

  static String getFormedIp(String ipAddress) {
    if (ipAddress.isEmpty) {
      debugPrint('Error: IP addess is empty');
      return '';
    } else if (ipAddress.contains('.')) {
      List<String> ipAddressList = ipAddress.split('.');
      String formedIp = ipAddressList.reversed.join('.').toString();
      return formedIp;
    } else {
      debugPrint('Error: Please pass an IP adrress');
      return '';
    }
  }

  static Future<bool> connectToNetwork(String ssid, String password) async {
    var args = {'ssid': ssid, 'password': password};
    var isConnected = await _channel.invokeMethod(_connectToNetwork, args);
    debugPrint('Is connected: ${isConnected.toString()}');
    await Future.delayed(const Duration(seconds: 5));
    var params = {'ssid': ssid};
    isConnected = await _channel.invokeMethod(_checkConnectingNetwork, params);
    debugPrint(
        '----------------IsConnected in android flutter wifi--------------: ${isConnected.toString()}');
    return isConnected;
    //_checkConnectingNetwork();
  }

  static Future<bool> connectToNetworkWithSSID(String ssid) async{
    var params = {'ssid': ssid};
    var result = await _channel.invokeMethod(_connectSavedNetwork, params);
    return result;
  }

  /*static Future<bool> _checkSsidConnection(String ssid) async {
    var params = {'ssid': ssid};
    var isConnected =
        await _channel.invokeMethod(_checkConnectingNetwork, params);
    debugPrint(
        '----------------IsConnected in android flutter wifi--------------: ${isConnected.toString()}');
    return isConnected;
  }*/

  static Future<List<ConfiguredNetwork>> getConfiguredNetworks() async {
    List<dynamic> mapList = await _channel.invokeMethod(_getConfiguredNetworks);

    List<ConfiguredNetwork> savedNetworksList =
        mapList.map((e) => ConfiguredNetwork.fromMap(e)).toList();
    return savedNetworksList;
  }

  static Future<bool> forgetWifi(String ssid) async {
    var params = {'ssid': ssid};
    var result = await _channel.invokeMethod(_forgetWifi, params);
    await Future.delayed(const Duration(seconds: 2));
    result = await _channel.invokeMethod(_checkIfNetworkExists, params);
    return result;
  }

  static Future<bool> forgetWifiWithSSID(String ssid) async {
    var params = {'ssid': ssid};
    var result = await _channel.invokeMethod(_forgetWifiWithSSID, params);
    return result;
  }

/*static Future<Map<String, dynamic>> _checkConnectingNetwork() async {
    Map<String, dynamic> map = {};
    var result = await _channel.invokeMethod(_CHECK_CONNECTING_NETWORK);
    if (result == null) {
      _checkConnectingNetwork();
    } else {
      map['success'] = result['success'];
      map['error'] = result['result'];
    }
    debugPrint('Result: ${result.toString()}');

    return map;
  }*/

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

class DhcpInfo {
  String? ip;
  String? gateway;
  String? serverAddress;
  String? dns1;
  String? dns2;
  String? leaseDuration;
  String? netMask;
  String? gatewayIp;

  DhcpInfo(
      {this.ip,
      this.gateway,
      this.serverAddress,
      this.dns1,
      this.dns2,
      this.leaseDuration,
      this.netMask,
      this.gatewayIp});

  factory DhcpInfo.fromMap(Map<dynamic, dynamic> map) {
    return DhcpInfo(
        dns1: map['dns_1'].toString(),
        dns2: map['dns_2'].toString(),
        gateway: map['gateway'].toString(),
        leaseDuration: map['lease_duration'].toString(),
        netMask: map['net_mask'].toString(),
        ip: map['ip_address'].toString(),
        serverAddress: map['server_address'].toString(),
        gatewayIp: map['gateway_ip'].toString().split('').reversed.join(''));
  }
}

class ConfiguredNetwork {
  String? ssid;
  int? networkId;
  String? bssid;
  String? fqdn;
  String? preSharedKey;
  bool? isHiddenSsid;
  int? status;

  ConfiguredNetwork(
      {this.ssid,
      this.networkId,
      this.bssid,
      this.fqdn,
      this.preSharedKey,
      this.isHiddenSsid,
      this.status});

  factory ConfiguredNetwork.fromMap(Map<dynamic, dynamic> map) {
    return ConfiguredNetwork(
        ssid: map['ssid'],
        isHiddenSsid: map['hidden_ssid'].toString().toLowerCase() == 'true'
            ? true
            : false,
        networkId: int.parse(map['id']),
        status: int.parse(map['status']));
  }
}
