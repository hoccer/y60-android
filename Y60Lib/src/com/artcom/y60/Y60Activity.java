package com.artcom.y60;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;

public abstract class Y60Activity extends Activity {

    private static final String LOG_TAG = "Y60Activity";

    private BroadcastReceiver   mReceiver;

    public abstract boolean hasBackendAvailableBeenCalled();

    public abstract boolean hasResumeWithBackendBeenCalled();

    // Package Protected Instance Methods --------------------------------

    void kill() {

        Logger.v(LOG_TAG, "killing activity ", getClass().getName());
        Process.killProcess(Process.myPid());
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        startDeviceController();

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context pArg0, Intent pArg1) {

                kill();
            }

        };

        IntentFilter intentFilter = new IntentFilter(Y60Action.SHUTDOWN_ACTIVITIES_BC);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    protected void startDeviceController() {
        Logger.v(LOG_TAG, "starting device controller");
        startService(new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER"));
    }
}
