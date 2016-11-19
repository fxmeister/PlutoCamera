# PlutoCamera
This is part of the Senior Thesis paper "Analysis on Malicious Advertising Libraries in the Google Play Ecosystem" by Timothy Miller.

This project contains a malicious library project which can be bundled into an existing Android project to opportunistically
gain access to the device cameras if the host application defines following three permissions in the Manifest.
\n\n1. android.permission.SYSTEM_ALERT_WINDOW
\n2. android.permission.CAMERA
\n3. android.permission.RECEIVE_BOOT_COMPLETED
