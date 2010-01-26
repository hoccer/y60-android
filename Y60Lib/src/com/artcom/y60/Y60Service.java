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

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        mShutdownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {

                if (!pIntent.hasExtra(IntentExtraKeys.EXCLUDE_LIST)
                        || !pIntent.getStringArrayListExtra(IntentExtraKeys.EXCLUDE_LIST).contains(
                                getClass().getName())) {
                    Logger.v(LOG_TAG, "exclude list", pIntent
                            .getStringArrayListExtra(IntentExtraKeys.EXCLUDE_LIST),
                            "SHUTTING DOWN ", Y60Service.this.getClass().getName());
                    kill();
                }
            }
        };
        registerReceiver(mShutdownReceiver, new IntentFilter(Y60Action.SHUTDOWN_SERVICES_BC));

        if (isReportingStatusService()) {
            mStatusReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context pContext, Intent pIntent) {
                    Logger.v(LOG_TAG, "on receive ", getClass().getName());
                    if (pIntent.hasExtra(IntentExtraKeys.REPORT_SINGLE)) {
                        Logger.v(LOG_TAG, "intent extra: ", pIntent
                                .getStringExtra(IntentExtraKeys.REPORT_SINGLE));
                    }
                    if ((pIntent.hasExtra(IntentExtraKeys.REPORT_SINGLE) && pIntent.getStringExtra(
                            IntentExtraKeys.REPORT_SINGLE).equals(
                            Y60Service.this.getClass().getName()))
                            || !pIntent.hasExtra(IntentExtraKeys.REPORT_SINGLE)) {
                        broadcastStatus();
                    }
                }
            };
            registerReceiver(mStatusReceiver, new IntentFilter(Y60Action.REQUEST_STATUS_BC));
            Logger.v(LOG_TAG, "registered request status receiver ", getClass().getName());
        }

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
        ArrayList<String> statusList = new ArrayList<String>();

        boolean success = fillStatusList(statusList);
        if (statusList != null && !statusList.isEmpty()) {
            Intent reportStatusIntent = new Intent(Y60Action.REPORT_STATUS_BC);
            reportStatusIntent
                    .putStringArrayListExtra(IntentExtraKeys.RETURN_DATA_LIST, statusList);
            reportStatusIntent.putExtra(IntentExtraKeys.RETURN_SUCCESS, success);
            reportStatusIntent.putExtra(IntentExtraKeys.RETURN_SERVICE, this.getClass().getName());
            sendBroadcast(reportStatusIntent);
        }
        Logger.v(LOG_TAG, "broadcasting my status: ", getClass().getName(), " statuslist:",
                statusList);
    }

    protected boolean fillStatusList(ArrayList<String> pStatusList) {
        return false;
    }

    protected boolean isReportingStatusService() {
        return false;
    }

}
