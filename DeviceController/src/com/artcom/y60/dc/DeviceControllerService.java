//========================================================================
//$Id: DeviceControllerService.java 170 2008-10-21 05:37:59Z janb.webtide $
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

import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.PreferencesActivity;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.GomProxyHelper;

public class DeviceControllerService extends Service {

    private Server server;
    private boolean _useNIO;
    private int _port;

    private static Resources __resources;

    private SharedPreferences preferences;
    public static final String DEFAULT_NIONAME = "com.artcom.y60.dc.nio";
    public static final String DEFAULT_PORTNAME = "com.artcom.y60.dc.port";
    public static final int DEFAULT_PORT = 4042;

    private static final String LOG_TAG = "DeviceControllerService";

    private IBinder binder = new DeviceControllerBinder();
    GomProxyHelper mGom = null;
    private NotificationManager mNM;

    public void onCreate() {
        Logger.i(LOG_TAG, "onCreate called");
        __resources = getResources();

        // Get the gom proxy helper and run watcher thread if gom is available.
        mGom = new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {

            public void bound(GomProxyHelper helper) {
                
                mGom = helper;
                updateRciUri();
            }

            public void unbound(GomProxyHelper helper) {
                
                mGom = null;
            }
        });

    }

    public void onStart(Intent intent, int startId) {
        Logger.i(LOG_TAG, "onStart called");
        if (server != null) {
            Toast.makeText(DeviceControllerService.this, R.string.jetty_already_started,
                    Toast.LENGTH_SHORT).show();
            Logger.i(LOG_TAG, "already running");
            return;
        }

        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);

            String portDefault = getText(R.string.pref_port_value).toString();
            // Log.v(LOG_TAG, "Default port is " + portDefault);
            String nioDefault = getText(R.string.pref_nio_value).toString();

            String portKey = getText(R.string.pref_port_key).toString();
            String nioKey = getText(R.string.pref_nio_key).toString();

            _useNIO = preferences.getBoolean(nioKey, Boolean.valueOf(nioDefault));

            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(DEFAULT_PORTNAME)) {
                _port = Integer.parseInt(bundle.getString(DEFAULT_PORTNAME));
            } else if (preferences.contains(portKey)) {
                _port = Integer.parseInt(preferences.getString(portKey, portDefault));
            } else {
                _port = DEFAULT_PORT;
            }

            startServer();

            mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            Toast.makeText(DeviceControllerService.this, R.string.jetty_started,
                           Toast.LENGTH_SHORT).show();

            // The PendingIntent to launch DeviceControllerActivity activity if
            // the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    DeviceControllerActivity.class), 0);

            CharSequence text = getText(R.string.manage_jetty);

            Notification notification = new Notification(R.drawable.smooth, text, System
                    .currentTimeMillis());

            notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);

            mNM.notify(R.string.jetty_started, notification);
            Logger.i(LOG_TAG, "DeviceControllerService started");
            super.onStart(intent, startId);
        } catch (BindingException e) {
            ErrorHandling.signalServiceError(LOG_TAG, e, this);
        } catch (Exception e) {
            Logger.e(LOG_TAG, "Error starting DeviceControllerService: ", e);
            Toast.makeText(this, getText(R.string.jetty_not_started), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRciUri() {
        // Update our rci_uri in the GOM

        DeviceConfiguration dc = DeviceConfiguration.load();
        String ipAddress = getIpAddress();

        // do not update uri if executed in the emulator; the host will need to
        // take care of this
        if (ipAddress.startsWith("10.0.2.")) {
            Logger.i(LOG_TAG, "I'm running in the emulator. Not publishing Remote Control URI.");
            return;
        }
        String command_uri = "http://" + ipAddress + ":" + _port + "/commands";
        Logger.v(LOG_TAG, "command_uri of local device controller is ", command_uri);

        try {
            GomNode device = mGom.getNode(dc.getDevicePath());
            device.getOrCreateAttribute("rci_uri").putValue(command_uri);
        } catch (RuntimeException e) {
            // TODO this is rather ugly and will remain so until the refactoring of the
            // scattered RuntimeExceptions throughout is complete.
            
            // we write something to the logfile, but otherwise ignore this. it's most
            // likely a transient network error
            
            Logger.w(LOG_TAG, "Could not update rci_uri in GOM");
        }
    }

    public void onDestroy() {
        try {
            if (server != null) {
                stopServer();
                // Cancel the persistent notification.
                mNM.cancel(R.string.jetty_started);
                // Tell the user we stopped.
                Toast.makeText(this, getText(R.string.jetty_stopped), Toast.LENGTH_SHORT).show();
                Logger.i(LOG_TAG, "DeviceControllerService stopped");
                __resources = null;
            } else {
                Logger.i(LOG_TAG, "DeviceControllerService not running");
                Toast.makeText(DeviceControllerService.this, R.string.jetty_not_running,
                        Toast.LENGTH_SHORT).show();
            }
            if (mGom != null) {
                mGom.unbind();
            }
        } catch (Exception e) {
            Logger.e(LOG_TAG, "Error stopping DeviceControllerService. Exception: " + e);
            Toast.makeText(this, getText(R.string.jetty_not_stopped), Toast.LENGTH_SHORT).show();
        }
    }

    public void onLowMemory() {
        Logger.w(LOG_TAG, "Low on memory");
        super.onLowMemory();
    }

    /**
     * Hack to get around bug in ResourceBundles
     * 
     * @param id
     * @return
     */
    public static InputStream getStreamToRawResource(int id) {
        if (__resources != null)
            return __resources.openRawResource(id);
        else
            return null;
    }

    public String getGomLocation() {

        return preferences.getString(PreferencesActivity.KEY_GOM_LOCATION, "");
    }

    public String getSelfPath() {

        return preferences.getString(PreferencesActivity.KEY_DEVICES_PATH, "") + "/"
                + preferences.getString(PreferencesActivity.KEY_DEVICE_ID, "");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d(LOG_TAG, "onBind called");

        return binder;
    }

    private void startServer() throws Exception {
        server = new Server();
        Connector connector;
        if (_useNIO) {
            SelectChannelConnector nioConnector = new SelectChannelConnector();
            nioConnector.setUseDirectBuffers(false);
            nioConnector.setPort(_port);
            nioConnector.setHost("0.0.0.0"); // listen on all interfaces
            connector = nioConnector;
        } else {
            SocketConnector bioConnector = new SocketConnector();
            bioConnector.setPort(_port);
            bioConnector.setHost("0.0.0.0"); // listen on all interfaces
            connector = bioConnector;
        }
        server.setConnectors(new Connector[] { connector });

        // Bridge Jetty logging to Android logging
        System.setProperty("org.mortbay.log.class", "org.mortbay.log.AndroidLog");
        // org.mortbay.log.Log.setLog(new AndroidLog());

        HandlerCollection handlers = new HandlerCollection();

        handlers.setHandlers(new Handler[] { new DeviceControllerHandler(this) });
        server.setHandler(handlers);

        server.start();
    }

    private void stopServer() throws Exception {
        Logger.i(LOG_TAG, "DeviceControllerService stopping");
        server.stop();
        server.join();
        server = null;
    }

    private String getIpAddress() {

        String address = null;

        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                if (!ni.getName().equals("lo")) { // pick the first interface
                    // that is not the loopback
                    Enumeration<InetAddress> iis = ni.getInetAddresses();
                    if (!iis.hasMoreElements()) {
                        continue; // this interface does not have any ip
                        // addresses, try the next one
                    }
                    address = iis.nextElement().getHostAddress();
                    break;
                }
            }
        } catch (Exception e) {
            Logger.e(LOG_TAG, "Problem retrieving ip addresses", e);
        }

        return address;
    }

    public class DeviceControllerBinder extends Binder {

        public DeviceControllerService getService() {

            return DeviceControllerService.this;
        }
    }
}
