package com.artcom.y60.tools;

import android.app.Activity;
import android.content.IntentFilter;

public class BroadcastLoggingActivity extends Activity {

    protected void onResume() {

        super.onResume();
        String action = "com.artcom.y60.http.RESOURCE_UPDATE";
        IntentFilter filter = new IntentFilter(action);
        registerReceiver(new LoggingBroadcastReceiver(), filter);
        setContentView(R.layout.main);
    }
}
