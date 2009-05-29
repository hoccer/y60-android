package com.artcom.y60.dc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MockService extends Service {

    private Intent mIntent;
    
    @Override
    public IBinder onBind(Intent pIntent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendBroadcast(Intent pIntent) {
        
        mIntent = pIntent;
    }
    
    public Intent getBroadcastIntent() {
        
        return mIntent;
    }

    
}
