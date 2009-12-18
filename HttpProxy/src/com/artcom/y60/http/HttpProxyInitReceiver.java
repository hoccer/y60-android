package com.artcom.y60.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HttpProxyInitReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "HttpProxyInitReceiver";

    @Override
    public void onReceive(Context pCtx, Intent pIntent) {

        // Logger.v(LOG_TAG, "starting http proxy service");
        //
        // Intent startHttpProxyIntent = new Intent(Y60Action.SERVICE_HTTP_PROXY);
        // if (pIntent.hasExtra(IntentExtraKeys.IS_IN_INIT_CHAIN)) {
        // startHttpProxyIntent.putExtra(IntentExtraKeys.IS_IN_INIT_CHAIN, true);
        // }
        // pCtx.startService(startHttpProxyIntent);
    }
}
