package com.example.tim.plutotjmille2cameralibrary.receiver;

/**
 * Created by Tim on 3/26/16.
 *
 *
 * BroadcastReceiver cannot be defined in the library project's manifest. The hosting project always needs to declare the components.
 *
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.tim.plutotjmille2cameralibrary.service.CameraService;

public class ServiceStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, CameraService.class);
        context.startService(i);
    }
}