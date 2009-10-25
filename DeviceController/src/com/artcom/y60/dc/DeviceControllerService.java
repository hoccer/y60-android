package com.artcom.y60.dc;

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

import com.artcom.y60.BindingException;
import com.artcom.y60.Constants;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IpAddressNotFoundException;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.Y60Action;
import com.artcom.y60.gom.GomException;
import com.artcom.y60.gom.GomHttpWrapper;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.Y60GomService;
import com.artcom.y60.http.HttpException;

public class DeviceControllerService extends Y60GomService {

    public static final String  DEFAULT_NIONAME  = "com.artcom.y60.dc.nio";
    public static final String  DEFAULT_PORTNAME = "com.artcom.y60.dc.port";
    private static final String LOG_TAG          = "DeviceControllerService";

    Server                      mServer;

    private final IBinder       mBinder          = new DeviceControllerBinder();

    @Override
    public void onCreate() {

        super.onCreate();
        Logger.i(LOG_TAG, "onCreate called");

        callOnBoundToGom(new Runnable() {

            public void run() {
                Logger.v(LOG_TAG, "call on bound to gom");
                try {
                    if (mServer == null) {
                        mServer = startServer(Constants.Network.DEFAULT_PORT);
                        Logger.v(LOG_TAG, "bound() to GomProxyHelper: Server will be started now");
                    }
                    try {
                        updateIpAdressAttributesForDevice();
                        updateVersionAttributeForDevice();
                    } catch (BindingException e) {
                        Logger.w(LOG_TAG,
                                "GomProxy was unbound while processing asynchronous thread");
                    }

                } catch (Exception ex) {

                    ErrorHandling.signalUnspecifiedError(LOG_TAG, ex, DeviceControllerService.this);
                }

                Logger.v(LOG_TAG,
                        "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ broadcast device controller ready");
                sendBroadcast(new Intent(Y60Action.DEVICE_CONTROLLER_READY));
            }
        });
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Logger.i(LOG_TAG, "onStart called");

        Logger.i(LOG_TAG, "onStart(): DeviceControllerService started");
        super.onStart(intent, startId);
    }

    private void updateIpAdressAttributesForDevice() throws GomException, IOException,
            HttpException {

        Logger.v(LOG_TAG, "updateGomAttributes for Device");
        DeviceConfiguration dc = DeviceConfiguration.load();
        String ipAddress;

        try {
            // do not update uri if executed in the emulator; the host will need
            // to take care of this
            if (!DeviceConfiguration.isRunningAsEmulator()) {
                ipAddress = NetworkHelper.getDeviceIpAddress();
                GomNode device = getGom().getNode(dc.getDevicePath());
                device.getOrCreateAttribute("ip_address").putValue(ipAddress);
                device.getOrCreateAttribute("rtp_address").putValue(ipAddress);
                device.getOrCreateAttribute("rtp_port").putValue("16384");
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
            if (!GomHttpWrapper.isAttributeExisting(Constants.Gom.URI + Constants.Gom.DEVICE_PATH
                    + ":enable_odp")) {
                GomHttpWrapper.updateOrCreateAttribute(Constants.Gom.URI
                        + Constants.Gom.DEVICE_PATH + ":enable_odp", "false");
            }

        } catch (IpAddressNotFoundException e) {
            ErrorHandling.signalNetworkError(LOG_TAG, e, this);
        }
    }

    private void updateVersionAttributeForDevice() throws GomException, HttpException, IOException {

        DeviceConfiguration dc = DeviceConfiguration.load();

        FileReader fr;
        try {
            fr = new FileReader("/sdcard/deployed_version.txt");
        } catch (FileNotFoundException e) {
            Logger.e(LOG_TAG, "could not find version string on sdcard: ", e);
            return;
        }
        char[] inputBuffer = new char[255];
        fr.read(inputBuffer);
        String version = new String(inputBuffer);

        GomNode device = getGom().getNode(dc.getDevicePath());
        device.getOrCreateAttribute("software_version").putValue(version);
    }

    @Override
    public void onDestroy() {
        Logger.v(LOG_TAG, "onDestroy");

        if (mServer == null) {
            Logger.i(LOG_TAG, "onDestroy(): Jetty not running, Server is null");
            // Toast.makeText(DeviceControllerService.this,
            // R.string.jetty_not_running,
            // Toast.LENGTH_SHORT).show();

            super.onDestroy();
            return;
        }

        try {
            stopServer();

        } catch (Exception e) {
            ErrorHandling.signalServiceError(LOG_TAG, e, this);
            // Toast.makeText(this, getText(R.string.jetty_not_stopped),
            // Toast.LENGTH_SHORT).show();
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

        return server;
    }

    private void stopServer() throws Exception {
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopping");
        mServer.stop();
        mServer.join();
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopped. done.");

        //Toast.makeText(this, getText(R.string.jetty_stopped), Toast.LENGTH_SHORT).show();
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
