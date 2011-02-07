package com.artcom.y60.vncserver;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Y60Action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VncBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "VncBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Y60Action.KILL_VNC_SERVER)) {
            try {
                VncService.killVncExecutable();
            } catch (Exception e) {
                ErrorHandling.signalError(LOG_TAG, e, context,
                        ErrorHandling.Category.COMMAND_EXECUTION);
            }
        }
    }
}
