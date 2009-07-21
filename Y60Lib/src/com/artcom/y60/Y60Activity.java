package com.artcom.y60;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;

public abstract class Y60Activity extends Activity {

    private static final String LOG_TAG      = "Y60Activity";

    private BroadcastReceiver   mReceiver;

    private boolean             mIsDestroyed = false;

    public abstract boolean hasBackendAvailableBeenCalled();

    public abstract boolean hasResumeWithBackendBeenCalled();

    // Package Protected Instance Methods --------------------------------

    void kill() {

        Logger.i(LOG_TAG, "finishing activity ", Y60Activity.this.getClass().getName());
        finish();

        new Thread(new Runnable() {
            public void run() {

                long start = System.currentTimeMillis();
                while (!mIsDestroyed) {

                    if (System.currentTimeMillis() - start > 7000) {
                        Logger.w(LOG_TAG, "finishing activity ", Y60Activity.this.getClass()
                                .getName(), " took too long");
                        break;
                    }

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        Logger.w(LOG_TAG, e);
                    }
                }

                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                    Logger.w(LOG_TAG, e);
                }

                Logger
                        .i(LOG_TAG, "killing process ", Process.myPid(), " for activity ",
                                getClass());
                Process.killProcess(Process.myPid());
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        Logger.v(LOG_TAG, "onCreate called for activity ", getClass());

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

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();

        mIsDestroyed = true;
        Logger.d(LOG_TAG, "onDestroy called for activity ", getClass());
    }

    protected void startDeviceController() {
        Logger.v(LOG_TAG, "starting device controller");
        startService(new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER"));
    }
}
