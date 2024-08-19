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

This plugin depends on [permission_handler](https://pub.dev/packages/permission_handler) package so
please import this package in yaml file

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

- Signal level
    - 5 is maximum
    - 0 is minimum

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
  getConnectionType() {
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

Enable Wifi

``` dart
 static Future<void> enableWifi() async {
    await _channel.invokeMethod(_ENABLE_WIFI_CALL);
  }
```

Disable Wifi

``` dart
static Future<void> disableWifi() async {
    await _channel.invokeMethod(_DISABLE_WIFI_CALL);
  }
```

Check if WI-FI is enable on your device

- Returns true if WIFI is enabled
- Return false is WIFI is disabled

``` dart
static Future<bool> isWifiEnabled() async {
    bool isWifiEnabled = await _channel.invokeMethod(_IS_WIFI_ENABLED_CALL);
    return isWifiEnabled;
  }
```

Get DHCP information

```dart
  getDhcpInfo() async {
    DhcpInfo dhcpInfo = await AndroidFlutterWifi.getDhcpInfo();
    String ipString = AndroidFlutterWifi.toIp(dhcpInfo.gateway!);
    print('Gateway: ${ipString}');
    print('Formed ip: ${formedIp}');
  }
```

Connect to a specific network with SSID & Password

````dart
void connectionTest() async {
    String ssid = '';
    String password = '';
    if (ssid.isEmpty) {
      throw ("SSID can't be empty");
    }
    if (password.isEmpty) {
      throw ("Password can't be empty");
    }
    debugPrint('Ssid: $ssid, Password: $password');
    ///Return boolean value 
    ///If true then connection is success
    ///If false then connection failed due to authentication
    var result = await AndroidFlutterWifi.connectToNetwork(ssid, password);

    debugPrint('---------Connection result-----------: ${result.toString()}');

  }
````

## IMPORTANT
- If your ip address is not well formed or it is reversed you can call this method to get well formed IP address
```dart
  String formedIp = AndroidFlutterWifi.getFormedIp(ipString);
```

## Get all saved wifi networks in your device
```
List<ConfiguredNetwork> list =
      await AndroidFlutterWifi.getConfiguredNetworks();
for (var element in list) {
  debugPrint('Network id: ${element.networkId}');
}
```

## Forget network by passing SSID
```
ActiveWifiNetwork activeWifiNetwork = await AndroidFlutterWifi.getActiveWifiInfo();
var result = await AndroidFlutterWifi.forgetWifiWithSSID(activeWifiNetwork.ssid!);
```

## Connect to pre-configured network
```
var result = await AndroidFlutterWifi.connectToNetworkWithSSID('Xartic_85AC9C_5G');
debugPrint('Connection result: ${result.toString()}');
```

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/), a specialized package that includes
platform-specific implementation code for Android and/or iOS.

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials, samples, guidance on
mobile development, and a full API reference.

