package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import com.artcom.y60.BindingListener;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.Y60Service;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.GomProxyHelper;

public class StatusWatcher extends Y60Service {

    private static final String LOG_TAG = "StatusWatcher";

    public enum ScreenState {
        UNKNOWN, ON, OFF
    };

    private NotificationManager mNotificationManager;
    private GomProxyHelper      mGom;
    private boolean             mIsHeartbeatLoopRunning;
    private StatusCollector     mStatusCollector;
    private HeartbeatLoop       mHeartbeatLoop;
    private Thread              mHeartbeatThread;
    private DeviceConfiguration mDeviceConfiguration;
    private boolean             mIsGomAvailable = false;
    private long                mSleepTime      = 10 * 1000;
    private String              mHistoryLog;

    @Override
    public void onCreate() {

        super.onCreate();

        mDeviceConfiguration = DeviceConfiguration.load();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mHeartbeatLoop = new HeartbeatLoop();
        mHeartbeatThread = new Thread(null, mHeartbeatLoop, "watch net connection");
        mIsHeartbeatLoopRunning = true;
        mHeartbeatThread.start();

        mHistoryLog = "";

        bindToGom();

        /*
         * mStatusCollector = new StatusCollector(mGom, mDeviceConfiguration);
         * IntentFilter fltScreenOn = new IntentFilter(Intent.ACTION_SCREEN_ON);
         * IntentFilter fltScreenOff = new
         * IntentFilter(Intent.ACTION_SCREEN_OFF);
         * registerReceiver(mStatusCollector, fltScreenOn);
         * registerReceiver(mStatusCollector, fltScreenOff);
         */
    }

    String getHistoryLog() {
        return mHistoryLog;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        if (mStatusCollector != null) {
            unregisterReceiver(mStatusCollector);
        }
        mIsHeartbeatLoopRunning = false;
        unbindFromGom();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent pIntent) {
        Logger
                .w(LOG_TAG,
                        "This service is not currently intended to be bound to. Are you sure you know what you're doing?");
        return null;
    }

    class HeartbeatLoop implements Runnable {

        private static final int GOM_NOT_ACCESSIBLE_NOTIFICATION_ID = 42;
        private String           mHistoryLog;

        @Override
        public void run() {

            String timestamp = "";
            while (mIsHeartbeatLoopRunning) {
                try {

                    Thread.sleep(mSleepTime);

                    timestamp = (new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")).format(new Date());

                    if (mGom != null) {

                        mIsGomAvailable = true;

                        GomNode device = mGom.getNode(mDeviceConfiguration.getDevicePath());
                        // Logger.v(LOG_TAG, "putting timestamp");
                        // device.getOrCreateAttribute("last_alive_update").putValue(timestamp);
                        // Logger.v(LOG_TAG, "putted timestamp");

                        // append current ping statistic to the history_log in
                        // gom
                        if (!DeviceConfiguration.isRunningAsEmulator()) {
                            updatePingStatistics();
                        }

                        mNotificationManager.cancel(GOM_NOT_ACCESSIBLE_NOTIFICATION_ID);

                        continue;
                    }

                    Logger.w(LOG_TAG, "StatusWatcher isn't (yet?) bound to GomProxyService");

                } catch (NoSuchElementException e) {
                    ErrorHandling.signalMissingGomEntryError(LOG_TAG, e, StatusWatcher.this);
                } catch (RuntimeException e) {
                    ErrorHandling.signalUnspecifiedError(LOG_TAG, e, StatusWatcher.this);
                } catch (Exception e) {
                    ErrorHandling.signalServiceError(LOG_TAG, e, StatusWatcher.this);
                }

                showWestWindNotification(timestamp);
                mIsGomAvailable = false;
            }
        }

        private void showWestWindNotification(String pTimestamp) {

            Intent configureDC = new Intent("y60.intent.CONFIGURE_DEVICE_CONTROLLER");
            configureDC.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Notification notification = new Notification(R.drawable.network_down_status_icon,
                    "Y60's GOM not accessible.", System.currentTimeMillis());
            PendingIntent pint = PendingIntent.getActivity(StatusWatcher.this, 0, configureDC,
                    PendingIntent.FLAG_ONE_SHOT);

            notification.setLatestEventInfo(StatusWatcher.this, "GOM not accessible",
                    "network might be down", pint);
            mNotificationManager.notify(GOM_NOT_ACCESSIBLE_NOTIFICATION_ID, notification);

            appendToLog(pTimestamp + ": network failure");
        }

        private void updatePingStatistics() {
            Runtime runtime = Runtime.getRuntime();
            Process process = null;
            try {
                process = runtime.exec("ping -q -c 20 -i 0.1 "
                        + Uri.parse(mDeviceConfiguration.getGomUrl()).getHost());
            } catch (IOException e) {
                ErrorHandling.signalNetworkError(LOG_TAG, e, StatusWatcher.this);
            }
            InputStreamReader reader = new InputStreamReader(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line = "";
            List<String> pingStatistic = new ArrayList<String>();
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    Logger.v(LOG_TAG, line.toString());
                    pingStatistic.add(line.toString());
                }
            } catch (IOException e) {
                ErrorHandling.signalIOError(LOG_TAG, e, StatusWatcher.this);
            }

            if (pingStatistic.size() < 3) {
                appendToLog(pingStatistic.toString());
            }

            appendToLog(pingStatistic.get(pingStatistic.size() - 2));

        }

        private void appendToLog(String pLogInfo) {

            mHistoryLog += "\n" + pLogInfo;
        }

    };

    // The following methods were created to facilitate testing

    Thread getWatcherThread() {
        return mHeartbeatThread;
    }

    void setSleepTime(long pSleepTime) {
        mSleepTime = pSleepTime;
    }

    boolean isGomAvailable() {
        return mIsGomAvailable;
    }

    void bindToGom() {

        new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {

            public void bound(GomProxyHelper phelper) {
                Logger.v(LOG_TAG, "GomProxy bound");
                mGom = phelper;
            }

            public void unbound(GomProxyHelper helper) {
                Logger.v(LOG_TAG, "GomProxy unbound");
                mStatusCollector = null;
                mGom = null;
            }
        });
    }

    void unbindFromGom() {

        if (mGom != null) {
            mGom.unbind();
        }
    }
}
