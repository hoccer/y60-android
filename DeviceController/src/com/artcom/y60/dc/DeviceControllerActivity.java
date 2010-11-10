//========================================================================
//$Id: DeviceControllerActivity.java 171 2008-10-21 08:20:53Z janb.webtide $
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package com.artcom.y60.dc;

import com.artcom.y60.Y60Action;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
//import android.widget.ListView;

public class DeviceControllerActivity extends Activity {
    public static final String  __PORT_DEFAULT = "4042";
    public static final boolean __NIO_DEFAULT  = true;

    @SuppressWarnings("unused")
	private static final String LOG_TAG = "DeviceController";

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.dc_console);

        // Watch for button clicks.
        final Button startButton = (Button) findViewById(R.id.start);
        startButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent startDcIntent = new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER);
                startDcIntent.putExtra(DeviceControllerService.DEFAULT_PORTNAME, __PORT_DEFAULT);
                startDcIntent.putExtra(DeviceControllerService.DEFAULT_NIONAME, __NIO_DEFAULT);
                startService(startDcIntent);

                Intent startSwIntent = new Intent(Y60Action.SERVICE_STATUS_WATCHER);
                startService(startSwIntent);
            }
        });

        Button stopButton = (Button) findViewById(R.id.stop);
        stopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent stopDcIntent = new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER);
                stopService(stopDcIntent);
                Intent stopSwIntent = new Intent(Y60Action.SERVICE_STATUS_WATCHER);
                stopService(stopSwIntent);
            }
        });

        // TODO: is this needed for some weird hack?
        //ListView list = (ListView) findViewById(R.id.list);

        // Automatically start the device controller. Useful during development,
        // possibly remove/refactor
        // in the production code.

        Intent startDcIntent = new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER);
        startDcIntent.putExtra(DeviceControllerService.DEFAULT_PORTNAME, __PORT_DEFAULT);
        startDcIntent.putExtra(DeviceControllerService.DEFAULT_NIONAME, __NIO_DEFAULT);
        startService(startDcIntent);

        Intent startSwIntent = new Intent(Y60Action.SERVICE_STATUS_WATCHER);
        startService(startSwIntent);
    }

    protected void onResume() {
        super.onResume();
    }

}
