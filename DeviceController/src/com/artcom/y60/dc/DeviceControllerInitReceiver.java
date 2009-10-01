package com.artcom.y60.dc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

public class DeviceControllerInitReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "DeviceControllerInitReceiver";

    @Override
    public void onReceive(Context pCtx, Intent pIntent) {
        if (pIntent.getAction().equals(Y60Action.SERVICE_GOM_PROXY_READY)) {
            Logger.v(LOG_TAG, "Gom proxy service is ready -> starting device controller services");
            Intent startDcIntent = new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER);
            pCtx.startService(startDcIntent);
            Intent startSwIntent = new Intent(Y60Action.SERVICE_STATUS_WATCHER);
            pCtx.startService(startSwIntent);
        }

    }
}
