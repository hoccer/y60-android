package com.artcom.y60.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

public class InitReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "InitReceiver";

    @Override
    public void onReceive(Context pCtx, Intent pIntent) {

        Logger.v(LOG_TAG, "starting http proxy service");

        Intent startHttpProxyIntent = new Intent(Y60Action.SERVICE_HTTP_PROXY);
        pCtx.startService(startHttpProxyIntent);
    }
}
