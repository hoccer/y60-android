package com.artcom.y60;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;

import com.artcom.y60.error.FallbackExceptionHandler;

public abstract class Y60Activity extends Activity {

    private static final String LOG_TAG                                  = "Y60Activity";

    private BroadcastReceiver   mShutdownReceiver;

    private int                 mResponsivnessCounterForTestPurposesOnly = 0;

    public abstract boolean hasBackendAvailableBeenCalled();

    public abstract boolean hasResumeWithBackendBeenCalled();

    // Package Protected Instance Methods --------------------------------

    protected void kill() {

        Logger.i(LOG_TAG, "finishing activity ", Y60Activity.this.getClass().getName());
        finish();

        // new Thread(new Runnable() {
        // public void run() {
        //
        // long start = System.currentTimeMillis();
        // while (!mIsDestroyed) {
        //
        // if (System.currentTimeMillis() - start > 7000) {
        // Logger.w(LOG_TAG, "finishing activity ", Y60Activity.this.getClass()
        // .getName(), " took too long");
        // break;
        // }
        //
        // try {
        // Thread.sleep(100);
        // } catch (Exception e) {
        // Logger.w(LOG_TAG, e);
        // }
        // }
        //
        // try {
        // Thread.sleep(250);
        // } catch (Exception e) {
        // Logger.w(LOG_TAG, e);
        // }
        //
        // Logger
        // .i(LOG_TAG, "killing process ", Process.myPid(), " for activity ",
        // getClass());
        // Process.killProcess(Process.myPid());
        // }
        // }).start();
    }

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        super.onCreate(pSavedInstanceState);
        startDeviceController();
        FallbackExceptionHandler.register(this);

        mShutdownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pArg0, Intent pArg1) {
                Logger.v(LOG_TAG, "received a kill bc for activity ", getClass());
                kill();
            }

        };

        IntentFilter intentFilter = new IntentFilter(Y60Action.SHUTDOWN_ACTIVITIES_BC);
        registerReceiver(mShutdownReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {

        if (mShutdownReceiver != null) {
            unregisterReceiver(mShutdownReceiver);
        }
        super.onDestroy();

        Logger.d(LOG_TAG, "onDestroy called for activity ", getClass());
    }

    protected void startDeviceController() {
        Logger.v(LOG_TAG, "starting device controller");
        startService(new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER"));
    }

    protected boolean isGomfreeTest() {
        return getIntent().getExtras() != null &&
               getIntent().getExtras().getBoolean("isGomfreeTest");
    }

    public int getResponsivnessCounterForTestPurposes() {
        return mResponsivnessCounterForTestPurposesOnly;
    }

    @Override
    public boolean onKeyDown(int pKeyCode, KeyEvent pEvent) {
        if (pKeyCode == KeyEvent.KEYCODE_T) {
            // used to test the responsivness of the gui
            mResponsivnessCounterForTestPurposesOnly++;
        }

        return super.onKeyDown(pKeyCode, pEvent);
    }

}
