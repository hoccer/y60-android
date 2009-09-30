package com.artcom.y60.gom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

public class GomProxyInitReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "InitReceiver";

    @Override
    public void onReceive(Context pCtx, Intent pIntent) {

        Logger.v(LOG_TAG, "starting gom proxy service");
        pCtx.startService(new Intent(Y60Action.SERVICE_GOM_PROXY));
    }
}
