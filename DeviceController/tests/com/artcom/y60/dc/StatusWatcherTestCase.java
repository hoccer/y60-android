package com.artcom.y60.dc;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;
import com.artcom.y60.http.HttpHelper;

import android.content.Intent;
import android.test.AssertionFailedError;
import android.test.FlakyTest;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.Suppress;

@Suppress
public class StatusWatcherTestCase extends ServiceTestCase<StatusWatcher> {

    private static final String LOG_TAG = "StatusWatcherTestCase";
    private Intent              mIntent;
    private DeviceConfiguration mDeviceConfiguration;

    public StatusWatcherTestCase() {
        super(StatusWatcher.class);
        mDeviceConfiguration = DeviceConfiguration.load();
    }

    public StatusWatcherTestCase(Class<StatusWatcher> pServiceClass) {
        super(pServiceClass);
        mDeviceConfiguration = DeviceConfiguration.load();
    }

    // Verifies that the service correctly updates the device's
    // history log in the GOM
    @Suppress
    public void testTimestampUpdates() throws Exception {
        startService(mIntent);
        Runnable checkGom = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(4 * 1000); // Wait for service to settle, GOM
                    // to become available etc
                } catch (InterruptedException e) {
                    // ignore
                }
                try {
                    String uri = mDeviceConfiguration.getGomUrl() + "/"
                            + mDeviceConfiguration.getDevicePath() + ":history_log.txt";
                    String historyLog = HttpHelper.getAsString(uri);
                    String lastLine = historyLog.substring(historyLog.lastIndexOf("\n") + 1);
                    String timestamp = lastLine.substring(0, lastLine.indexOf(": "));
                    long now = System.currentTimeMillis();
                    long historyUpdated;
                    try {
                        historyUpdated = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(
                                timestamp).getTime();
                    } catch (ParseException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                    final long TIME_DELTA_TOLERANCE = 11 * 1000; // in
                    // milliseconds
                    long delta = Math.abs(historyUpdated - now);
                    assertTrue("Timestamp in GOM (" + timestamp
                            + ") is older than expected. Expected a difference of at most "
                            + (TIME_DELTA_TOLERANCE / 1000) + " seconds, found " + (delta / 1000)
                            + " seconds.", delta < TIME_DELTA_TOLERANCE);
                } catch (Exception ex) {

                    throw new RuntimeException(ex);
                }
            }
        };
        Thread thread = new Thread(checkGom);
        thread.start();

        while (thread.getState() != Thread.State.TERMINATED) {
            Thread.sleep(100);
        }
    }

    @FlakyTest
    public void testWatcherThreadRunning() throws InterruptedException {
        startService(mIntent);

        Thread thread = getService().getWatcherThread();

        assertTrue(thread.isAlive());

        sleepNonblocking(5 * 1000);

        assertTrue("Watcher thread died", thread.isAlive());
    }

    // Verifies that the "GOM unavailable" notification (symbolized by a
    // "West Wind"
    // icon) is correctly displayed/cleared.
    public void testGomUnavailableNotification() throws InterruptedException {
        startService(mIntent);

        // do not sleep beween updates -- tests must run fast
        getService().setSleepTime(0);

        // wait some time to let the service load the data
        long requestStartTime = System.currentTimeMillis();
        while (!getService().isBoundToGom()) {
            if (System.currentTimeMillis() > requestStartTime + 20 * 1000) {
                throw new AssertionFailedError(
                        "Expected the StatusWatcher to see the GOM right after starting up, but it doesn't.");
            }
            Thread thread = getService().getWatcherThread();
            assertTrue("Watcher thread died", thread.isAlive());
            Logger.v(LOG_TAG, "gom is still not available");

            Thread.sleep(250);
        }

        Logger.v(LOG_TAG, "unbinding from gom");
        getService().unbindFromGom();

        requestStartTime = System.currentTimeMillis();
        while (getService().isBoundToGom()) {
            if (System.currentTimeMillis() > requestStartTime + 10 * 1000) {
                throw new AssertionFailedError(
                        "Forced StatusWatcher to unbind from GOM, but StatusWatcher reports that it can still see the GOM");
            }
            Thread thread = getService().getWatcherThread();
            assertTrue("Watcher thread died", thread.isAlive());

            Thread.sleep(10);
        }

        Logger.v(LOG_TAG, "binding again");
        getService().bindToGom();

        requestStartTime = System.currentTimeMillis();
        while (!getService().isBoundToGom()) {
            if (System.currentTimeMillis() > requestStartTime + 15 * 1000) {
                throw new AssertionFailedError(
                        "Expected the StatusWatcher to see the GOM after telling it to re-bind, but it doesn't.");
            }
            Thread thread = getService().getWatcherThread();
            assertTrue("Watcher thread died", thread.isAlive());

            Thread.sleep(10);
        }
    }

    // Verifies that the service correctly updates the device's
    // "last updated" entry in the GOM
    public void testLastAliveUpdates() {
        // TODO
    }

    // Verifies that the service correctly updates the GOM if the
    // device's screen is turned on
    public void testScreenOnUpdate() {
        // TODO
    }

    // Verifies that the service correctly updates the GOM if the
    // device's screen is turned off
    public void testScreenOffUpdate() {
        // TODO
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mIntent = new Intent(Y60Action.SERVICE_STATUS_WATCHER);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void sleepNonblocking(int pSleepTime) {
        for (int i = 0; i < pSleepTime / 10; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ix) {
                // ignore
            }
        }
    }
}
