package com.artcom.y60;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class Y60Service extends Service {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG           = "Y60Service";

    private BroadcastReceiver   mShutdownReceiver = null;
    private BroadcastReceiver   mStatusReceiver   = null;

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

        mStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {
                broadcastStatus();
            }
        };
        registerReceiver(mStatusReceiver, new IntentFilter(Y60Action.REQUEST_STATUS_BC));

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (mShutdownReceiver != null) {
            unregisterReceiver(mShutdownReceiver);
            mShutdownReceiver = null;
        }

        if (mStatusReceiver != null) {
            unregisterReceiver(mStatusReceiver);
            mStatusReceiver = null;
        }
        super.onDestroy();
    }

    protected void kill() {
        Logger.i(LOG_TAG, "stopping service: ", getClass().getName());
        stopSelf();
    }

    private void broadcastStatus() {
        Logger.i(LOG_TAG, "broadcasting my status: ", getClass().getName());

    }

}
