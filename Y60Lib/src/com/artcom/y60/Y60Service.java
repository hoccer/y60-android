package com.artcom.y60;

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

        mShutdownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {
                kill();
            }
        };
        registerReceiver(mShutdownReceiver, new IntentFilter(Y60Action.SHUTDOWN_SERVICES_BC));

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mShutdownReceiver != null) {
            unregisterReceiver(mShutdownReceiver);
        }
        super.onDestroy();
    }

    protected void kill() {
        Logger.i(LOG_TAG, "stopping service: ", getClass().getName());
        stopSelf();
    }

}
