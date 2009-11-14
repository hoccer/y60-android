package com.artcom.y60.gom;

import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GomProxyInitReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "GomProxyInitReceiver";

    @Override
    public void onReceive(Context pCtx, Intent pIntent) {

        Intent startGomProxyIntent = new Intent(Y60Action.SERVICE_GOM_PROXY);
        if (pIntent.hasExtra(IntentExtraKeys.IS_IN_INIT_CHAIN)) {
            Logger.v(LOG_TAG, "starting gom proxy service, adding extra: ", pIntent
                    .getBooleanExtra(IntentExtraKeys.IS_IN_INIT_CHAIN, false));
            startGomProxyIntent.putExtra(IntentExtraKeys.IS_IN_INIT_CHAIN, pIntent.getBooleanExtra(
                    IntentExtraKeys.IS_IN_INIT_CHAIN, false));
        }
        Logger.v(LOG_TAG, "starting gom proxy service");
        pCtx.startService(startGomProxyIntent);
    }
}
