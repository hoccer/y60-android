package com.artcom.y60.dc;

import android.content.Intent;
import android.os.IBinder;

import com.artcom.y60.Y60Service;

public class MockService extends DeviceControllerService {

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

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent pIntent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
