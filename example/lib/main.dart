import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:android_flutter_wifi/android_flutter_wifi.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();

    init();

  }



  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_platformVersion\n'),
        ),
      ),
    );
  }

  void init() async {
    await AndroidFlutterWifi.init();
  }

  void getWifiList() async {
    List<WifiNetwork> wifiList = await AndroidFlutterWifi.getWifiScanResult();
    if(wifiList.isNotEmpty){
      WifiNetwork wifiNetwork = wifiList[0];
      print('Name: ${wifiNetwork.ssid}');
    }
  }

  isConnectionFast(){
    print(AndroidFlutterWifi.isConnectionFast());
  }

  getConnectionType(){
    print(AndroidFlutterWifi.getConnectionType());
  }

  getActiveWifiNetwork() async {
    ActiveWifiNetwork activeWifiNetwork = await AndroidFlutterWifi.getActiveWifiInfo();
  }
}
