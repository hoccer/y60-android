package com.artcom.y60.http;

import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HttpProxyInitReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "HttpProxyInitReceiver";

    @Override
    public void onReceive(Context pCtx, Intent pIntent) {

        Logger.v(LOG_TAG, "starting http proxy service");

        Intent startHttpProxyIntent = new Intent(Y60Action.SERVICE_HTTP_PROXY);
        if (pIntent.hasExtra(IntentExtraKeys.IS_IN_INIT_CHAIN)) {
            startHttpProxyIntent.putExtra(IntentExtraKeys.IS_IN_INIT_CHAIN, pIntent
                    .getBooleanExtra(IntentExtraKeys.IS_IN_INIT_CHAIN, false));
        }
        pCtx.startService(startHttpProxyIntent);
    }
}
