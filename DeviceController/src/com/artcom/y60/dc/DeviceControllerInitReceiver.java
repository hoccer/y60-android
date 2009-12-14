package com.artcom.y60.dc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceControllerInitReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "DeviceControllerInitReceiver";

    @Override
    public void onReceive(Context pCtx, Intent pIntent) {
        // if (pIntent.getAction().equals(Y60Action.SERVICE_DEVICE_CONTROLLER)) {
        //
        // Intent startDcIntent = new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER);
        // Logger.v(LOG_TAG, "Gom proxy service is ready -> starting device controller services");
        // pCtx.startService(startDcIntent);
        //
        // Intent startSwIntent = new Intent(Y60Action.SERVICE_STATUS_WATCHER);
        // pCtx.startService(startSwIntent);
        // }

    }
}
