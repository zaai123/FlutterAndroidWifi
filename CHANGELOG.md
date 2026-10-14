## 0.0.1
* TODO: Describe initial release.

## 0.0.2
More Wifi functions added

## 0.0.3
API 19 bugs fixed

# 0.0.4
Get DHCP info

## 0.0.6
Wifi scan done with broadcast receiver

## 0.0.7
Connect to specific WIFI directly from plugin

## 0.0.9
Get saved networks list and forgets wifi network

## 0.1.0
Connect to pre-configured network

## 0.1.1
Connecting network with password improved

## 0.1.2
Connecting network with password improved

## 0.1.3
Gradle plugin updated

## 0.1.4
* Fix ANR crash caused by infinite scan loop in BroadcastReceiver.
* Offload all Wi-Fi Binder and system service calls to a background HandlerThread to prevent UI thread blocking.
* Add null safety and exception handling to Wi-Fi scan results, configured networks, and DHCP queries.
* Properly cleanup worker thread on engine detachment.