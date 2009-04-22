package com.artcom.y60.dc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.HTTPHelper;
import com.artcom.y60.gom.GomProxyService;

import android.content.ComponentName;
import android.content.Intent;
import android.test.ServiceTestCase;

public class StatusWatcherTestCase extends ServiceTestCase<StatusWatcher> {

    private Intent mIntent;
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
    public void testHistoryUpdates() throws ParseException, InterruptedException {
        startService(mIntent);
        Runnable checkGom = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10 * 1000); // Wait for service to settle, GOM
                                             // to become available etc
                } catch (InterruptedException e) {
                    // ignore
                }
                String uri = mDeviceConfiguration.getGomUrl() + "/"
                        + mDeviceConfiguration.getDevicePath() + ":history_log.txt";
                String historyLog = HTTPHelper.get(uri);
                String lastLine = historyLog.substring(historyLog.lastIndexOf("\n") + 1);
                String timestamp = lastLine.substring(0, lastLine.indexOf(": "));
                long now = System.currentTimeMillis();
                long historyUpdated;
                try {
                    historyUpdated = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(timestamp)
                            .getTime();
                } catch (ParseException e) {
                    throw new RuntimeException(e.getMessage());
                }
                final long TIME_DELTA_TOLERANCE = 11 * 1000; // in milliseconds
                long delta = Math.abs(historyUpdated - now);
                assertTrue("Timestamp in GOM (" + timestamp
                        + ") is older than expected. Expected a difference of at most "
                        + (TIME_DELTA_TOLERANCE / 1000) + " seconds, found " + (delta / 1000)
                        + " seconds.", delta < TIME_DELTA_TOLERANCE);
            }
        };
        Thread thread = new Thread(checkGom);
        thread.start();
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

    protected void setUp() throws Exception {
        super.setUp();
        mIntent = new Intent(getContext(), StatusWatcher.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
