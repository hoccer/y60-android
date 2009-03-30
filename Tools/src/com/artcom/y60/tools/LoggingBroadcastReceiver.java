package com.artcom.y60.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LoggingBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context pContext, Intent pIntent) {
        
        Log.v(getClass().getName(), "received broadcast for action "+pIntent.getAction());
    }

}
