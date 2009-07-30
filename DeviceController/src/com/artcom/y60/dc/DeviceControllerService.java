package com.artcom.y60.dc;

import java.io.IOException;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.thread.QueuedThreadPool;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.Constants;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IpAddressNotFoundException;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.Y60Service;
import com.artcom.y60.gom.GomException;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.GomProxyHelper;
import com.artcom.y60.http.HttpException;

public class DeviceControllerService extends Y60Service {

    public static final String  DEFAULT_NIONAME  = "com.artcom.y60.dc.nio";
    public static final String  DEFAULT_PORTNAME = "com.artcom.y60.dc.port";
    private static final String LOG_TAG          = "DeviceControllerService";

    Server                      mServer;

    private IBinder             mBinder          = new DeviceControllerBinder();
    GomProxyHelper              mGom             = null;

    @Override
    public void onCreate() {

        super.onCreate();
        Logger.i(LOG_TAG, "onCreate called");

        new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {

            public void bound(GomProxyHelper helper) {

                Logger.i(LOG_TAG, "bound(): called");
                mGom = helper;

                try {
                    if (mServer == null) {
                        mServer = startServer(Constants.Network.DEFAULT_PORT);
                        Logger.v(LOG_TAG, "bound() to GomProxyHelper: Server will be started now");
                    }
                    try {
                        updateDeviceAddresses();
                    } catch (BindingException e) {
                        Logger.w(LOG_TAG,
                                "GomProxy was unbound while processing asynchronous thread");
                    }

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

    @Override
    public void onStart(Intent intent, int startId) {
        Logger.i(LOG_TAG, "onStart called");

        Logger.i(LOG_TAG, "onStart(): DeviceControllerService started");
        super.onStart(intent, startId);

    }

    private GomProxyHelper getGom() {
        if (mGom == null || !mGom.isBound()) {
            throw new BindingException("requested gom, but it is not bound");
        }
        return mGom;
    }

    /**
     * Update our ip and rci_uri in the GOM
     */
    private void updateDeviceAddresses() throws GomException, IOException, HttpException {

        DeviceConfiguration dc = DeviceConfiguration.load();
        String ipAddress;

        try {
            // do not update uri if executed in the emulator; the host will need
            // to take care of this
            if (!DeviceConfiguration.isRunningAsEmulator()) {
                ipAddress = NetworkHelper.getDeviceIpAddress();
                GomNode device = getGom().getNode(dc.getDevicePath());
                device.getOrCreateAttribute("ip_address").putValue(ipAddress);
            } else {
                Logger.i(LOG_TAG, "I'm running in the emulator. Not publishing my ip address.");
            }
        } catch (IpAddressNotFoundException e) {
            ErrorHandling.signalNetworkError(LOG_TAG, e, this);
        }

        try {
            ipAddress = NetworkHelper.getStagingIp().getHostAddress();

            String command_uri = "http://" + ipAddress + ":" + Constants.Network.DEFAULT_PORT
                    + "/commands";
            Logger.v(LOG_TAG, "command_uri of local device controller is ", command_uri);

            GomNode device = getGom().getNode(dc.getDevicePath());
            device.getOrCreateAttribute("rci_uri").putValue(command_uri);

        } catch (IpAddressNotFoundException e) {
            ErrorHandling.signalNetworkError(LOG_TAG, e, this);
        }
    }

    @Override
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

    @Override
    public void onLowMemory() {
        ActivityManager actMgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        MemoryInfo memInfo = new MemoryInfo();
        actMgr.getMemoryInfo(memInfo);
        ErrorHandling.signalWarning(LOG_TAG, "Memory low - " + memInfo.availMem + "!", this);
        Logger.logMemoryInfo(LOG_TAG, this);
        super.onLowMemory();
    }

    Server startServer(int pPort) throws Exception {
        SocketConnector connector = new SocketConnector();
        connector.setPort(pPort);
        connector.setHost("0.0.0.0"); // listen on all interfaces

        Server server = new Server();

        server.setConnectors(new Connector[] { connector });

        // Bridge Jetty logging to Android logging
        // System.setProperty("org.mortbay.log.class",
        // "org.mortbay.log.AndroidLog");
        // org.mortbay.log.Log.setLog(new AndroidLog());

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { new DeviceControllerHandler(this) });
        server.setHandler(handlers);

        server.start();
        QueuedThreadPool threadpool = (QueuedThreadPool) server.getThreadPool();
        threadpool.setMaxStopTimeMs(10);

        Toast.makeText(DeviceControllerService.this, R.string.jetty_started, Toast.LENGTH_SHORT)
                .show();

        return server;
    }

    private void stopServer() throws Exception {
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopping");
        mServer.stop();
        mServer.join();
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
