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
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;

import org.mortbay.ijetty.AndroidLog;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.PreferencesActivity;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.GomProxyHelper;

public class DeviceControllerService extends Service {

    public static final String DEFAULT_NIONAME = "com.artcom.y60.dc.nio";
    public static final String DEFAULT_PORTNAME = "com.artcom.y60.dc.port";
    public static final int DEFAULT_PORT = 4042;
    private static final String LOG_TAG = "DeviceControllerService";

    private static Resources sResources;

    Server mServer;
    private boolean mUseNio;
    private SharedPreferences mPreferences;

    private IBinder mBinder = new DeviceControllerBinder();
    GomProxyHelper mGom = null;

    public void onCreate() {
        Logger.i(LOG_TAG, "onCreate called");
        sResources = getResources();

        // Get the gom proxy helper and run watcher thread if gom is available.
        mGom = new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {

            public void bound(GomProxyHelper helper) {

                mGom = helper;

                try {
                    mServer = startServer(DEFAULT_PORT);
                    updateRciUri(DEFAULT_PORT);

                } catch (Exception ex) {

                    ErrorHandling.signalUnspecifiedError(LOG_TAG, ex, DeviceControllerService.this);
                }
            }

            public void unbound(GomProxyHelper helper) {

                mGom = null;
            }
        });

        Intent statusWatcherIntent = new Intent("y60.intent.SERVICE_STATUS_WATCHER");
        startService(statusWatcherIntent);
    }

    public void onStart(Intent intent, int startId) {
        Logger.i(LOG_TAG, "onStart called");
        if (mServer != null) {
            Toast.makeText(DeviceControllerService.this, R.string.jetty_already_started,
                    Toast.LENGTH_SHORT).show();
            Logger.i(LOG_TAG, "already running");
            return;
        }

        try {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            String nioDefault = getText(R.string.pref_nio_value).toString();
            String nioKey = getText(R.string.pref_nio_key).toString();

            mUseNio = mPreferences.getBoolean(nioKey, Boolean.valueOf(nioDefault));

            Toast
                    .makeText(DeviceControllerService.this, R.string.jetty_started,
                            Toast.LENGTH_SHORT).show();

            // The PendingIntent to launch DeviceControllerActivity activity if
            // the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    DeviceControllerActivity.class), 0);

            CharSequence text = getText(R.string.manage_jetty);

            Notification notification = new Notification(R.drawable.smooth, text, System
                    .currentTimeMillis());

            notification.setLatestEventInfo(this, getText(R.string.app_name), text, contentIntent);

            Logger.i(LOG_TAG, "DeviceControllerService started");
            super.onStart(intent, startId);
        } catch (BindingException e) {
            ErrorHandling.signalServiceError(LOG_TAG, e, this);
        } catch (Exception e) {
            Logger.e(LOG_TAG, "Error starting DeviceControllerService: ", e);
            Toast.makeText(this, getText(R.string.jetty_not_started), Toast.LENGTH_SHORT).show();
        }
    }

    /** Update our rci_uri in the GOM */
    private void updateRciUri(int pPort) {

        DeviceConfiguration dc = DeviceConfiguration.load();
        String ipAddress = getIpAddress();

        // do not update uri if executed in the emulator; the host will need to
        // take care of this
        if (ipAddress.startsWith("10.0.2.")) {
            Logger.i(LOG_TAG, "I'm running in the emulator. Not publishing Remote Control URI.");
            return;
        }
 
        String command_uri = "http://" + ipAddress + ":" + pPort + "/commands";
        Logger.v(LOG_TAG, "command_uri of local device controller is ", command_uri);

        GomNode device = mGom.getNode(dc.getDevicePath());
        device.getOrCreateAttribute("rci_uri").putValue(command_uri);
    }

    public void onDestroy() {
        try {
            if (mServer != null) {
                stopServer();
                // Cancel the persistent notification.
                // Tell the user we stopped.
                Toast.makeText(this, getText(R.string.jetty_stopped), Toast.LENGTH_SHORT).show();
                Logger.i(LOG_TAG, "DeviceControllerService stopped");
                sResources = null;
            } else {
                Logger.i(LOG_TAG, "DeviceControllerService not running");
                Toast.makeText(DeviceControllerService.this, R.string.jetty_not_running,
                        Toast.LENGTH_SHORT).show();
            }
            if (mGom != null) {
                mGom.unbind();
            }
        } catch (Exception e) {
            ErrorHandling.signalServiceError(LOG_TAG, e, this);
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
        if (sResources != null)
            return sResources.openRawResource(id);
        else
            return null;
    }

    public String getGomLocation() {

        return mPreferences.getString(PreferencesActivity.KEY_GOM_LOCATION, "");
    }

    public String getSelfPath() {

        return mPreferences.getString(PreferencesActivity.KEY_DEVICES_PATH, "") + "/"
                + mPreferences.getString(PreferencesActivity.KEY_DEVICE_ID, "");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d(LOG_TAG, "onBind called");

        return mBinder;
    }

    Server startServer(int pPort) throws Exception {
        Connector connector;
        if (false) {
            SelectChannelConnector nioConnector = new SelectChannelConnector();
            nioConnector.setUseDirectBuffers(false);
            nioConnector.setPort(pPort);
            nioConnector.setHost("0.0.0.0"); // listen on all interfaces
            connector = nioConnector;
        } else {
            SocketConnector bioConnector = new SocketConnector();
            bioConnector.setPort(pPort);
            bioConnector.setHost("0.0.0.0"); // listen on all interfaces
            connector = bioConnector;
        }
        
        Server server = new Server();
        server.setConnectors(new Connector[] { connector });

        // Bridge Jetty logging to Android logging
        System.setProperty("org.mortbay.log.class", "org.mortbay.log.AndroidLog");
        org.mortbay.log.Log.setLog(new AndroidLog());

        HandlerCollection handlers = new HandlerCollection();
        if (server == null) {
            ErrorHandling.signalMissingMandatoryObjectError(LOG_TAG, new RuntimeException(
            "sombody removed the webserver object"));
        }

        handlers.setHandlers(new Handler[] { new DeviceControllerHandler(this) });
        server.setHandler(handlers);

        server.start();
        return server;
    }

    private void stopServer() throws Exception {
        Logger.i(LOG_TAG, "DeviceControllerService stopping");
        mServer.stop();
        mServer = null;
    }

    String getIpAddress() {

        HashSet<InetAddress> addresses = null;
        try {
            addresses = NetworkHelper.getLocalIpAddresses();
        } catch (SocketException e1) {
            ErrorHandling.signalServiceError(LOG_TAG, new RuntimeException(
                    "could not retive a valid ip address"), this);
        }

        Iterator<InetAddress> itr = addresses.iterator();
        while (itr.hasNext()) {
            InetAddress addr = itr.next();
            Logger.d(LOG_TAG, "address: ", addr.getHostAddress());
            String addrString = addr.getHostAddress();
            if (!addrString.equals("127.0.0.1")) {
                return addrString;
            }
        }

        ErrorHandling.signalServiceError(LOG_TAG, new RuntimeException(
                "could not retive a valid ip address"), this);
        return null;
    }

    public class DeviceControllerBinder extends Binder {

        public DeviceControllerService getService() {

            return DeviceControllerService.this;
        }
    }
}
