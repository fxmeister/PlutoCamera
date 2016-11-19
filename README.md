# PlutoCamera
This is part of the Senior Thesis paper "Analysis on Malicious Advertising Libraries in the Google Play Ecosystem" by Timothy Miller.

This project contains a malicious library project which can be bundled into an existing Android project to opportunistically
gain access to the device cameras if the host application defines following three permissions in the Manifest.
1. android.permission.SYSTEM_ALERT_WINDOW
2. android.permission.CAMERA
3. android.permission.RECEIVE_BOOT_COMPLETED
