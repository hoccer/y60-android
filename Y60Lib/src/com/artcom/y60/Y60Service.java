package com.artcom.y60;

import java.util.ArrayList;

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

    protected ArrayList<String> mStatusList       = null;

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
        if (mStatusList != null && !mStatusList.isEmpty()) {
            Intent reportStatusIntent = new Intent(Y60Action.REPORT_STATUS_BC);
            reportStatusIntent.putStringArrayListExtra(IntentExtraKeys.RETURN_DATA_LIST,
                    mStatusList);
            sendBroadcast(reportStatusIntent);
        }
    }

    protected void addToStatusList(String pStatusMessage) {
        if (mStatusList == null) {
            mStatusList = new ArrayList<String>();
            mStatusReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context pContext, Intent pIntent) {
                    broadcastStatus();
                }
            };
            registerReceiver(mStatusReceiver, new IntentFilter(Y60Action.REQUEST_STATUS_BC));
        }
        mStatusList.add(pStatusMessage);
    }
}
