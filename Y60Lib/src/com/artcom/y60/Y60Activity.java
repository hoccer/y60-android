package com.artcom.y60;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public abstract class Y60Activity extends Activity {

    private static final String LOG_TAG = "Y60Activity";

    private BroadcastReceiver   mReceiver;

    public abstract boolean hasBackendAvailableBeenCalled();

    public abstract boolean hasResumeWithBackendBeenCalled();

    // Package Protected Instance Methods --------------------------------

    void shutdown() {

        Logger.v(LOG_TAG, "shutting down activity ", getClass().getName());
        finish();
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context pArg0, Intent pArg1) {

                shutdown();
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

}
