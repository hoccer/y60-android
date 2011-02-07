package com.artcom.y60.vncserver;

import java.io.IOException;
import java.io.OutputStream;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IoHelper;
import com.artcom.y60.IpAddressNotFoundException;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.Y60Action;
import com.artcom.y60.Y60Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class VncService extends Y60Service {

    private static final String LOG_TAG           = "VncService";
    private static final int    NOTIFICATION_ID   = 123;               // TODO receive from
                                                                        // constants?
    private static final String PORT              = "5900";
    private static final String SCALING           = "100";
    private static final String ROTATION          = "0";
    static final String         VNC_EXECUTABLE    = "androidvncserver";
    public static final long    WATCHDOG_INTERVAL = 5 * 1000;

    private Notification        notification      = null;
    private String              mIpAddress        = "unknown";
    private Intent              mNotificationPressedIntent;

    private Thread              vncWatchdog;

    @Override
    public void onCreate() {
        Logger.v(LOG_TAG, "onCreate() ", this);

        mNotificationPressedIntent = new Intent("tgallery.intent.SUPER_COW_POWER");

        notification = new Notification(R.drawable.icon_neutral, LOG_TAG,
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, LOG_TAG, "Service created - status unknown",
                PendingIntent.getActivity(this, 0, mNotificationPressedIntent, 0));
        startForeground(NOTIFICATION_ID, notification);

        vncWatchdog = new VncExecutableWatcher();
        vncWatchdog.start();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Logger.v(LOG_TAG, "onDestroy()");
        try {
            stopServer();
        } catch (Exception e) {
            ErrorHandling.signalError(LOG_TAG, e, this, ErrorHandling.Category.COMMAND_EXECUTION);
        }

        vncWatchdog.interrupt();
        vncWatchdog = null;

        cancelNotification();

        super.onDestroy();
    }

    private synchronized void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.v(LOG_TAG, "onBind: " + intent);
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.v(LOG_TAG, "onStartCommand  - id: " + startId); // , id: " + startId + " " + this, "
                                                               // " + Thread.currentThread());
        try {
            mIpAddress = NetworkHelper.getDeviceIpAddress();
        } catch (IpAddressNotFoundException e) {
            ErrorHandling.signalNetworkError(LOG_TAG, e, this);
        }

        try {
            startServer();
            Logger.v(LOG_TAG, "before sending vcn ready!");
            sendBroadcast(new Intent(Y60Action.VNC_SERVICE_READY));
            Logger.v(LOG_TAG, "bc send");
        } catch (Exception e) {
            ErrorHandling.signalError(LOG_TAG, e, this, ErrorHandling.Category.COMMAND_EXECUTION);
        }

        return START_NOT_STICKY;
    }

    private Notification getCurrentNotification() {
        int myIcon = R.drawable.icon_neutral;

        String myMsg = "Server is";
        if (isVncServerRunning()) {
            myMsg += " started (" + mIpAddress + ")";
            myIcon = R.drawable.icon_started;
        } else {
            myMsg += " stopped";
            myIcon = R.drawable.icon_stopped;
        }

        notification.icon = myIcon;
        notification.setLatestEventInfo(this, LOG_TAG, myMsg,
                PendingIntent.getActivity(this, 0, mNotificationPressedIntent, 0));
        return notification;
    }

    private boolean isVncServerRunning() {
        IoHelper.ProcessStates myStatus = IoHelper.isProcessRunning(VNC_EXECUTABLE);
        if (myStatus.equals(IoHelper.ProcessStates.UNKNOWN)) {
            ErrorHandling.signalIOError(LOG_TAG, new IOException("Unknown status of processs "
                    + VNC_EXECUTABLE), this);
            return false;
        } else if (myStatus.equals(IoHelper.ProcessStates.RUNNNING)) {
            return true;
        } else {
            return false;
        }
    }

    public void startServer() throws Exception {
        Logger.v(LOG_TAG, "startServer()");
        if (isVncServerRunning()) {
            Logger.v(LOG_TAG, "Server is already running... return");
            return;
        }

        String vncExecutablePath = getFilesDir().getAbsolutePath() + "/" + VNC_EXECUTABLE;
        String cmdParams = " -r " + ROTATION + " -s " + SCALING + " -P " + PORT;

        Process sh = Runtime.getRuntime().exec("su");
        OutputStream os = sh.getOutputStream();
        IoHelper.writeCommand(os, "chmod 777 " + getFilesDir().getAbsolutePath() + "/"
                + VNC_EXECUTABLE);
        os.close();

        Logger.v(LOG_TAG, "launching exec");
        IoHelper.launchExecutable(vncExecutablePath + cmdParams);
        Logger.v(LOG_TAG, "launched exec");
    }

    public void stopServer() throws Exception {
        Logger.v(LOG_TAG, "stopServer");
        if (!isVncServerRunning()) {
            Logger.v(LOG_TAG, "Server is already stopped...");
            return;
        }
        killVncExecutable();
    }

    static void killVncExecutable() throws Exception {
        Process sh;

        sh = Runtime.getRuntime().exec("su");
        OutputStream os = sh.getOutputStream();

        Logger.v(LOG_TAG, "killServer");

        if (IoHelper.hasBusybox()) {
            IoHelper.writeCommand(os, "busybox killall androidvncserver");
            IoHelper.writeCommand(os, "busybox killall -KILL androidvncserver");
        } else {
            IoHelper.writeCommand(os, "killall androidvncserver");
            IoHelper.writeCommand(os, "killall -KILL androidvncserver");
            if (IoHelper.findExecutableOnPath("killall") == null) {
                Logger.v(LOG_TAG,
                        "I couldn't find the killall executable, please install busybox or i can't stop server");
            }
        }

        IoHelper.writeCommand(os, "exit");
        os.flush();
        os.close();
    }

    public class VncExecutableWatcher extends Thread {

        boolean lastSeenStatus = false;

        public VncExecutableWatcher() {
            lastSeenStatus = isVncServerRunning();
        }

        @Override
        public void run() {
            while (!isInterrupted()) {

                try {
                    Thread.sleep(WATCHDOG_INTERVAL);
                } catch (InterruptedException e) {
                    Logger.e(LOG_TAG, "Thread sleep got interrupted in VncExecutableWatcher ", this);
                    cancelNotification();
                    break;
                }

                boolean vncCurrentRunningState = isVncServerRunning();
                if (lastSeenStatus != vncCurrentRunningState) {
                    Logger.v(LOG_TAG, "watchdog updates notification, ", this);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, getCurrentNotification());
                    lastSeenStatus = vncCurrentRunningState;
                }

            }
        }
    }

}
