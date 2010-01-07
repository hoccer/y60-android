package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.gom.Y60GomService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public class StatusWatcher extends Y60GomService {

    private static final String LOG_TAG = "StatusWatcher";

    public enum ScreenState {
        UNKNOWN, ON, OFF
    };

    private NotificationManager mNotificationManager;
    private boolean             mIsHeartbeatLoopRunning;
    private HeartbeatLoop       mHeartbeatLoop;
    private Thread              mHeartbeatThread;
    private DeviceConfiguration mDeviceConfiguration;
    private long                mSleepTime = 10 * 1000;
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
        mIsHeartbeatLoopRunning = false;

        super.onDestroy();
    }

    @Override
    protected void kill() {
        // do not kill me upon shutdown services bc
    }

    @Override
    protected boolean monitorMyLifecycleOnSdcard() {
        return false;
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

            while (mIsHeartbeatLoopRunning) {

                try {
                    Thread.sleep(mSleepTime);
                } catch (InterruptedException e) {
                    ErrorHandling.signalServiceError(LOG_TAG, e, StatusWatcher.this);
                }

                if (!isBoundToGom()) {
                    showWestWindNotification("Can not bind to Y60's GOM.");
                    Logger.w(LOG_TAG, "StatusWatcher isn't (yet?) bound to GomProxy");
                    return;
                }

                getGom().getNode(mDeviceConfiguration.getDevicePath());

                // everything is fine... disable notification
                mNotificationManager.cancel(GOM_NOT_ACCESSIBLE_NOTIFICATION_ID);
            }
        }

        private void showWestWindNotification(String pMessage) {

            Intent configureDC = new Intent("y60.intent.CONFIGURE_DEVICE_CONTROLLER");
            configureDC.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Notification notification = new Notification(R.drawable.network_down_status_icon,
                    pMessage, System.currentTimeMillis());
            PendingIntent pint = PendingIntent.getActivity(StatusWatcher.this, 0, configureDC,
                    PendingIntent.FLAG_ONE_SHOT);

            notification.setLatestEventInfo(StatusWatcher.this, "GOM not accessible",
                    "network might be down", pint);
            mNotificationManager.notify(GOM_NOT_ACCESSIBLE_NOTIFICATION_ID, notification);

            // appendToLog(pTimestamp + ": network failure");
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

}
