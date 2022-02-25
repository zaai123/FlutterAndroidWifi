# android_flutter_wifi

Android Wifi plugin for flutter apps

## Getting Started

This plugin allows Flutter apps to get available Wifi access point list where you can get almost all
the information about AP accessing to WifiNetwork object. This plugin works only for Android. Apple
does not allow devs to maniplute wifi.

Note :- This plugin requires the location permission to auto enable the wifi if android version is
above 9.0. Please enable your GPS 

Android : - Add below Permissions to your manifist.xml file -

``` xml
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-feature android:name="android.hardware.wifi" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

This plugin depends on [permission_handler](https://pub.dev/packages/permission_handler) package so please import this package in yaml file

``` yaml
    permission_handler: ^9.2.0
```

Initialize plugin to request location permissions inti() method return boolean 
- true if permission is granted 
- false is permission is denied

``` dart
     init() async {
        await AndroidFlutterWifi.init();
     }
```

Get Wifi access point list Object WifiNetwork contains almost every inforamtion avaiable on AP

``` dart
     getWifiList() async {
    List<WifiNetwork> wifiList = await AndroidFlutterWifi.getWifiScanResult();
    if(wifiList.isNotEmpty){
      WifiNetwork wifiNetwork = wifiList[0];
      print('Name: ${wifiNetwork.ssid}');
    }
  }
```

Check if connection is fast or slow
- Returns true if connection is fast
- Returns false if connection is slow
```dart
    isConnectionFast() {
      print(AndroidFlutterWifi.isConnectionFast());
    }
```

Check your connection type
- Return 2 types of connection WI-FI or Cellular

```dart
  getConnectionType(){
    print(AndroidFlutterWifi.getConnectionType());
  }
```
Get information about your wifi connection 
- ActiveWifiNetwork object contains every available information like 
  - IP address, 
  - Name
  - SSID
  - MAC Addess
  - etc
```` dart
    getActiveWifiNetwork() async {
        ActiveWifiNetwork activeWifiNetwork = await AndroidFlutterWifi.getActiveWifiInfo();
    }
````

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/), a specialized package that includes
platform-specific implementation code for Android and/or iOS.

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials, samples, guidance on
mobile development, and a full API reference.

