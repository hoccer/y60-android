package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import android.app.Notification;
import android.app.PendingIntent;

import com.artcom.y60.Constants;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IpAddressNotFoundException;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.Y60Action;
import com.artcom.y60.Y60Service;
import com.artcom.y60.gom.GomHttpWrapper;
import com.artcom.y60.http.HttpException;

public class DeviceControllerService extends Y60Service {

    public static final String  DEFAULT_NIONAME  = "com.artcom.y60.dc.nio";
    public static final String  DEFAULT_PORTNAME = "com.artcom.y60.dc.port";
    private static final String LOG_TAG          = "DeviceControllerService";
    private final int           notificationId   = 58;

    Server                      mServer;

    private final IBinder       mBinder          = new DeviceControllerBinder();

    @Override
    public void onCreate() {
    	Logger.v(LOG_TAG, "onCreate START");
    	
        Notification notification = new Notification( R.drawable.statusbar_dc, 
            LOG_TAG, System.currentTimeMillis());
        notification.setLatestEventInfo(this, LOG_TAG, "", PendingIntent.getBroadcast(this, 0, new Intent(), 0) );
        startForeground(notificationId,notification);

        try {
            if (mServer == null) {
                mServer = startServer(Constants.Network.DEFAULT_PORT);
            }
        } catch (Exception ex) {
            ErrorHandling.signalUnspecifiedError(LOG_TAG, ex, this);
        }

        try {
        	Logger.v(LOG_TAG, "onCreate updating gom attributes for device... START");
            updateGomAttributesForDevice();
            Logger.v(LOG_TAG, "onCreate updating gom attributes for device... DONE");
        } catch (IOException e) {
            ErrorHandling.signalIOError(LOG_TAG, e, this);
        } catch (HttpException e) {
            ErrorHandling.signalHttpError(LOG_TAG, e, this);
        }
        Logger.v(LOG_TAG, "onCreate END");
        super.onCreate();
    }
    @Override
    public void onStart(Intent pIntent, int startId) {
        Logger.i(LOG_TAG, "onStartCommand called");

        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());

        sendBroadcast(new Intent(Y60Action.DEVICE_CONTROLLER_READY));
        Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ sent broadcast device controller ready");
    }


    @Override
    public int onStartCommand(Intent pIntent, int flags, int startId) {
        Logger.i(LOG_TAG, "onStartCommand called");

        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());

        sendBroadcast(new Intent(Y60Action.DEVICE_CONTROLLER_READY));
        Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ sent broadcast device controller ready");

        return START_STICKY;
    }

    @Override
    protected void kill() {
        // do not kill me upon shutdown services bc
    }

    private void updateGomAttributesForDevice() throws IOException, HttpException {

        Logger.v(LOG_TAG, "updateGomAttributes for Device");
        String ipAddress = "";
        String deviceUri = Constants.Gom.URI + Constants.Gom.DEVICE_PATH;

        try {
            // do not update uri if executed in the emulator; the host will need
            // to take care of this
            if (!DeviceConfiguration.isRunningViaArtcomDevelopmentVpn()) {
                ipAddress = NetworkHelper.getDeviceIpAddress();
                GomHttpWrapper.updateOrCreateAttribute(deviceUri + ":" + Constants.Network.IP_ADDRESS_ATTRIBUTE, ipAddress);
                GomHttpWrapper.updateOrCreateAttribute(deviceUri + ":rtp_address", ipAddress);
                GomHttpWrapper.updateOrCreateAttribute(deviceUri + ":rtp_port", "16384");
            } else {
                Logger.i(LOG_TAG, "I'm running in the emulator. Not publishing my ip address.");
            }

            ipAddress = NetworkHelper.getStagingIp().getHostAddress();
            String command_uri = "http://" + ipAddress + ":" + Constants.Network.DEFAULT_PORT
                    + "/commands";
            Logger.v(LOG_TAG, "command_uri of local device controller is ", command_uri);
            GomHttpWrapper.updateOrCreateAttribute(deviceUri + ":rci_uri", command_uri);

            createAttributeIfNotExistentWith(Constants.Gom.URI + Constants.Gom.ENABLE_ODP_ATTR,
                    "false");
            createAttributeIfNotExistentWith(Constants.Gom.URI + Constants.Gom.ENABLE_ODP_AGC_ATTR,
                    "false");
            createAttributeIfNotExistentWith(Constants.Gom.URI + Constants.Gom.DEBUG_MODE_ATTR,
                    "false");

        } catch (IpAddressNotFoundException e) {
            ErrorHandling.signalNetworkError(LOG_TAG, e, this);
        }
    }

    private void createAttributeIfNotExistentWith(String attributeUri, String value)
            throws HttpException, IOException {
        if (!GomHttpWrapper.isAttributeExisting(attributeUri)) {
            GomHttpWrapper.updateOrCreateAttribute(attributeUri, value);
        }
    }

    @Override
    public void onDestroy() {
        Logger.v(LOG_TAG, "onDestroy");
        if (mServer != null) {
            try {
                Logger.v(LOG_TAG, "stopping Jetty");
                stopServer();
            } catch (Exception e) {
                ErrorHandling.signalServiceError(LOG_TAG, e, this);
            }
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        ActivityManager actMgr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        MemoryInfo memInfo = new MemoryInfo();
        actMgr.getMemoryInfo(memInfo);
        ErrorHandling.signalWarningToLog(LOG_TAG, "Memory low - " + memInfo.availMem + "!", this);
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

        return server;
    }

    private void stopServer() throws Exception {
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopping");
        mServer.stop();
        mServer.join();
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopped. done.");
        mServer = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.d(LOG_TAG, "onBind called");
        sendBroadcast(new Intent(Y60Action.DEVICE_CONTROLLER_READY));
        return mBinder;
    }

    public class DeviceControllerBinder extends Binder {

        public DeviceControllerService getService() {
            return DeviceControllerService.this;
        }
    }
}
