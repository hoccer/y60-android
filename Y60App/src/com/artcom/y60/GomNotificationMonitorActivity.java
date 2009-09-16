package com.artcom.y60;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

public class GomNotificationMonitorActivity extends Activity {

    // Instance Variables ------------------------------------------------

    private NotificationMonitoringReceiver mReceiver;

    // Protected Instance Methods ----------------------------------------
    
    @Override
    protected void onCreate(Bundle pSavedInstanceState) {

        super.onCreate(pSavedInstanceState);
        
        setContentView(R.layout.notification_monitor_layout);
        
        TextView textView = (TextView)findViewById(R.id.notification_log_view);
        mReceiver = new NotificationMonitoringReceiver(textView);
    }

    @Override
    protected void onResume() {

        super.onResume();
        
        registerReceiver(mReceiver, new IntentFilter(Y60Action.GOM_NOTIFICATION_BC));
    }

    @Override
    protected void onPause() {

        unregisterReceiver(mReceiver);
        super.onPause();
    }

    class NotificationMonitoringReceiver extends BroadcastReceiver {

        private TextView mTextView;
        
        public NotificationMonitoringReceiver(TextView pTextView) {
            
            mTextView = pTextView;
        }
        
        @Override
        public void onReceive(Context pContext, Intent pIntent) {
            
            mTextView.append("Received GOM notification:");
            mTextView.append("\n\tURI: "+pIntent.getStringExtra(IntentExtraKeys.NOTIFICATION_PATH));
            mTextView.append("\n\tOperation: "+pIntent.getStringExtra(IntentExtraKeys.NOTIFICATION_OPERATION));
            mTextView.append("\n\tData: "+pIntent.getStringExtra(IntentExtraKeys.NOTIFICATION_DATA_STRING));
            mTextView.append("\n\n");
        }
    }
    
}
