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

import org.mortbay.ijetty.AndroidLog;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.HandlerCollection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.artcom.y60.BindingListener;
import com.artcom.y60.Constants;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IpAddressNotFoundException;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.Y60Service;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.GomProxyHelper;

public class DeviceControllerService extends Y60Service {

    public static final String  DEFAULT_NIONAME  = "com.artcom.y60.dc.nio";
    public static final String  DEFAULT_PORTNAME = "com.artcom.y60.dc.port";
    private static final String LOG_TAG          = "DeviceControllerService";

    private static Resources    sResources;

    Server                      mServer;
    private boolean             mUseNio;
    private SharedPreferences   mPreferences;

    private IBinder             mBinder          = new DeviceControllerBinder();
    GomProxyHelper              mGom             = null;

    public void onCreate() {

        super.onCreate();
        Logger.i(LOG_TAG, "onCreate called");

        mGom = new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {

            public void bound(GomProxyHelper helper) {

                Logger.i(LOG_TAG, "bound(): called");
                mGom = helper;

                try {
                    if (mServer == null) {
                        mServer = startServer(Constants.Network.DEFAULT_PORT);
                        Logger.v(LOG_TAG, "bound() to GomProxyHelper: Server will be started now");
                    }
                    updateRciUri(Constants.Network.DEFAULT_PORT);

                } catch (Exception ex) {

                    ErrorHandling.signalUnspecifiedError(LOG_TAG, ex, DeviceControllerService.this);
                }
            }

            public void unbound(GomProxyHelper helper) {
                Logger.i(LOG_TAG, "unbound(): called");
                mGom = null;
            }
        });

        Intent statusWatcherIntent = new Intent("y60.intent.SERVICE_STATUS_WATCHER");
        startService(statusWatcherIntent);
    }

    public void onStart(Intent intent, int startId) {
        Logger.i(LOG_TAG, "onStart called");

        Logger.i(LOG_TAG, "onStart(): DeviceControllerService started");
        super.onStart(intent, startId);

    }

    /** Update our rci_uri in the GOM */
    private void updateRciUri(int pPort) {

        DeviceConfiguration dc = DeviceConfiguration.load();
        String ipAddress;
        try {
            ipAddress = NetworkHelper.getDeviceIpAddress();

            // do not update uri if executed in the emulator; the host will need
            // to take care of this
            if (DeviceConfiguration.isRunningAsEmulator()) {
                Logger
                        .i(LOG_TAG,
                                "I'm running in the emulator. Not publishing Remote Control URI.");
                return;
            }

            String command_uri = "http://" + ipAddress + ":" + pPort + "/commands";
            Logger.v(LOG_TAG, "command_uri of local device controller is ", command_uri);

            GomNode device = mGom.getNode(dc.getDevicePath());
            device.getOrCreateAttribute("rci_uri").putValue(command_uri);

        } catch (IpAddressNotFoundException e) {
            ErrorHandling.signalNetworkError(LOG_TAG, e, this);
        }
    }

    public void onDestroy() {
        Logger.v(LOG_TAG, "onDestroy");

        if (mServer == null) {
            Logger.i(LOG_TAG, "onDestroy(): Jetty not running, Server is null");
            Toast.makeText(DeviceControllerService.this, R.string.jetty_not_running,
                    Toast.LENGTH_SHORT).show();

            super.onDestroy();
            return;
        }

        try {
            stopServer();

            if (mGom != null) {
                mGom.unbind();
            }
        } catch (Exception e) {
            ErrorHandling.signalServiceError(LOG_TAG, e, this);
            Toast.makeText(this, getText(R.string.jetty_not_stopped), Toast.LENGTH_SHORT).show();
        }

        super.onDestroy();
    }

    public void onLowMemory() {
        ErrorHandling.signalLowOnMemoryError(LOG_TAG, new Exception("Low on memory!"), this);
        super.onLowMemory();
    }

    Server startServer(int pPort) throws Exception {
        SocketConnector connector = new SocketConnector();
        connector.setPort(pPort);
        connector.setHost("0.0.0.0"); // listen on all interfaces

        Server server = new Server();

        server.setConnectors(new Connector[] { connector });

        // Bridge Jetty logging to Android logging
        System.setProperty("org.mortbay.log.class", "org.mortbay.log.AndroidLog");
        org.mortbay.log.Log.setLog(new AndroidLog());

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { new DeviceControllerHandler(this) });
        // server.setHandler(handlers);

        server.start();

        Toast.makeText(DeviceControllerService.this, R.string.jetty_started, Toast.LENGTH_SHORT)
                .show();

        return server;
    }

    private void stopServer() throws Exception {
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopping");
        mServer.stop();
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopped. done.");

        Toast.makeText(this, getText(R.string.jetty_stopped), Toast.LENGTH_SHORT).show();
        mServer = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d(LOG_TAG, "onBind called");

        return mBinder;
    }

    public class DeviceControllerBinder extends Binder {

        public DeviceControllerService getService() {
            return DeviceControllerService.this;
        }
    }
}
