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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.artcom.y60.Logger;

public class DeviceControllerActivity extends Activity 
{
    public static final String __PORT_DEFAULT = "4042";
    public static final boolean __NIO_DEFAULT = true;
    
    private IPList _ipList;
    private static final String LOG_TAG = "DeviceController";

    private class IPList 
    {
        private List _list = new ArrayList();

        public IPList()
        {
        }

        public int getCount ()
        {
            return _list.size();
        }

        public String getItem(int index)
        {
            return (String)_list.get(index);
        }

        public void refresh ()
        {
            _list.clear();

            try
            {
                Enumeration nis = NetworkInterface.getNetworkInterfaces();
                while (nis.hasMoreElements())
                {
                    NetworkInterface ni = (NetworkInterface)nis.nextElement();
                    Enumeration iis = ni.getInetAddresses();
                    while (iis.hasMoreElements())
                    {
                        _list.add(ni.getDisplayName()+": "+((InetAddress)iis.nextElement()).getHostAddress());
                    }
                }
            }
            catch (Exception e)
            {
                Logger.e(LOG_TAG, "Problem retrieving ip addresses", e);
            }
        }
    }

    private class NetworkListAdapter extends BaseAdapter 
    {
        private Context _context;
        private IPList _ipList;

        public NetworkListAdapter(Context context, IPList ipList) 
        {
            _context = context;
            _ipList = ipList;
            _ipList.refresh();
        }

        public int getCount() 
        {
            return _ipList.getCount();
        }

        public boolean areAllItemsSelectable() 
        {
            return false;
        }

        public boolean isSelectable(int position) 
        {
            return false;
        }

        public Object getItem(int position) 
        {
            return position;
        }

        public long getItemId(int position) 
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) 
        {
            TextView tv;
            if (convertView == null) 
            {
                tv = new TextView(_context);
            } 
            else 
            {
                tv = (TextView) convertView;
            }
            tv.setText(_ipList.getItem(position));
            return tv;
        }
    }


    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        setContentView(R.layout.dc_console);
        
        // Watch for button clicks.
        final Button startButton = (Button)findViewById(R.id.start);
        startButton.setOnClickListener(
                new OnClickListener()
                {
                    public void onClick(View v)
                    {  
                        Intent startDcIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
                        startDcIntent.putExtra(DeviceControllerService.DEFAULT_PORTNAME, __PORT_DEFAULT);
                        startDcIntent.putExtra(DeviceControllerService.DEFAULT_NIONAME, __NIO_DEFAULT);
                        startService(startDcIntent);
                        
                        Intent startSwIntent = new Intent("y60.intent.SERVICE_STATUS_WATCHER");
                        startService(startSwIntent);
                    }
                }
        );

        Button stopButton = (Button)findViewById(R.id.stop);
        stopButton.setOnClickListener(
                new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Intent stopDcIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
                        stopService(stopDcIntent);
                        Intent stopSwIntent = new Intent("y60.intent.SERVICE_STATUS_WATCHER");
                        stopService(stopSwIntent);
                    }
                }
        );

        ListView list = (ListView) findViewById(R.id.list);
        _ipList = new IPList();
        list.setAdapter(new NetworkListAdapter(this, _ipList));
        
        // Automatically start the device controller. Useful during development, possibly remove/refactor
        // in the production code.
        
        Intent startDcIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startDcIntent.putExtra(DeviceControllerService.DEFAULT_PORTNAME, __PORT_DEFAULT);
        startDcIntent.putExtra(DeviceControllerService.DEFAULT_NIONAME, __NIO_DEFAULT);
        startService(startDcIntent);
        
        Intent startSwIntent = new Intent("y60.intent.SERVICE_STATUS_WATCHER");
        startService(startSwIntent);
    }

    protected void onResume()
    {
        _ipList.refresh();
        super.onResume();
    }
}
