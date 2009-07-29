package com.artcom.y60;

import android.app.Service;

public abstract class Y60Service extends Service {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "Y60Service";

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        Logger.v(LOG_TAG, "onCreate called for service ", getClass());

        super.onCreate();
    }

    @Override
    public void onDestroy() {

        Logger.d(LOG_TAG, "onDestroy called for service ", getClass());

        super.onDestroy();
    }

}
