package com.artcom.y60;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;

public abstract class Y60Service extends Service {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "Y60Service";

    // Instance Variables ------------------------------------------------

    private BroadcastReceiver   mReceiver;

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        super.onCreate();

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context pArg0, Intent pArg1) {

                kill();
            }

        };

        IntentFilter intentFilter = new IntentFilter(Y60Action.SHUTDOWN_SERVICES_BC);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // Package Protected Instance Methods --------------------------------

    void kill() {

        Logger.v(LOG_TAG, "killing service ", getClass().getName());
        Process.killProcess(Process.myPid());
    }

}
