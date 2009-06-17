package com.artcom.y60;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class Y60Activity extends Activity {

    static String LOG_TAG = "Y60Activity";

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        startDeviceController();
    }

    public abstract boolean hasBackendAvailableBeenCalled();

    public abstract boolean hasResumeWithBackendBeenCalled();

    protected void startDeviceController() {
        Logger.v(LOG_TAG, "starting device controller");
        startService(new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER"));
    }

}
