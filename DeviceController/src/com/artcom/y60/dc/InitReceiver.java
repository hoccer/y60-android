package com.artcom.y60.dc;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Y60Action;


public class InitReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context pCtx, Intent pIntent) {

          Intent startDcIntent = new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER);
          pCtx.startService(startDcIntent);
          Intent startSwIntent = new Intent(Y60Action.SERVICE_STATUS_WATCHER);
          pCtx.startService(startSwIntent);   
        
    }
}
