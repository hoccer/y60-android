package com.artcom.y60;

import java.io.FileWriter;
import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class Y60Service extends Service {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "Y60Service";

    private BroadcastReceiver   mShutdownReceiver;

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        monitorMyLifecycleOnSdcard();

        mShutdownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {
                Logger.v(LOG_TAG, "stop self");
                kill();
            }
        };
        registerReceiver(mShutdownReceiver, new IntentFilter(Y60Action.SHUTDOWN_SERVICES_BC));

        Logger.v(LOG_TAG, "------------ onCreate called for service ", getClass());

        super.onCreate();
    }

    private void monitorMyLifecycleOnSdcard() {
        FileWriter fw;
        try {
            fw = new FileWriter(Constants.Device.ALIVE_SERVICES_PATH + "/" + getClass().getName());
            fw.close();
            Logger.v(LOG_TAG, "Wrote: ", Constants.Device.ALIVE_SERVICES_PATH + "/"
                    + getClass().getName(), " on sdcard");
        } catch (IOException e) {
            ErrorHandling.signalIOError(LOG_TAG, e, this);
        }
    }

    @Override
    public void onDestroy() {

        if (mShutdownReceiver != null) {
            unregisterReceiver(mShutdownReceiver);
        }
        Logger.d(LOG_TAG, "------------ onDestroy called for service ", getClass());

        super.onDestroy();
    }

    protected void kill() {
        Logger.i(LOG_TAG, "stopping service: ", getClass().getName());
        stopSelf();
    }

}
