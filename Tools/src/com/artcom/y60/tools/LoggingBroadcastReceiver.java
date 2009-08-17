package com.artcom.y60.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Logger;

public class LoggingBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context pContext, Intent pIntent) {
        
        Logger.v(getClass().getName(), "received broadcast for action ", pIntent.getAction());
    }

}
