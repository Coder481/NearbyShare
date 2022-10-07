# NearbyShare
### Application to share data offline within 2 android devices

<!-- Download APK link here -->

## Implementation

I have used [NearBy Connections API](https://developers.google.com/nearby/connections/overview) to implement this project

Steps include:
* ### Add dependencies (build.gradle)
```java
// For NearBy connections API
implementation 'com.google.android.gms:play-services-nearby:18.3.0'
// For location
implementation 'com.google.android.gms:play-services-location:20.0.0'

// For Kotlin-Coroutines(optional)
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1"
implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.1'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.1'
```
* ### Add permissions in manifest and handle in activity too
```xml
<!-- Required for Nearby Connections -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- Only required for apps targeting Android 12 and higher -->
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<!-- Optional: only required for FILE payloads -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```
```kotlin
// Check permissions granted based on OS version
private fun genPermissionsAreGranted(): Boolean {
    val res = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        res && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
    else res
}

// Ask for permissions before performing any task
private fun askPermissions(){
    if(!genPermissionsAreGranted()) requestPermissionLauncher.launch(permissionsArray)
}
```
Permission launcher and permissions array
```kotlin
// Permissions array based on OS Version
private val permissionsArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)
} else {
    arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)
}
private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){}
```
* ### Handle transferring</br>
I have made two dialogs to handle transferring files, Advertising(Sender) and Discovering(Receiver)</br>
**Components involved in complete file transfer:**
  * ConnectionClient - Manages connection between devices
  * ConnectionLifecycleCallback - Callback for connection between devices
  * PayloadCallback - Callback for payload shared
  * Payload - The file/data that is going to be shared
**Methods involved in callbacks:**
  * ConnectionLifecycleCallback
      * onConnectionInitiated() - Found a connection, can accept in this method
      * onConnectionResult() - Share status of connection, whether established or got any errors
      * onDisconnected() - When connection demolished
  * PayloadCallback
      * onPayloadReceived() - Received payload(file) transferred by sender. Save this payload in a variable for future use(not in case of sender)
      * onPayloadTransferUpdate() - Share updates on payload transfer. When the status becomes SUCCESS, save the payload(saved in above method) in local storage

### Tip
* If you want to demolished connection just after successfully file transfer, then do it only Discoverer(receiver) side because doing it on Advertiser(Sender) side will disconnect connection before receiver receives complete file.
