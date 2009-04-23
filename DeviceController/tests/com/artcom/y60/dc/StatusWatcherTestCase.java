package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.test.ServiceTestCase;

import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.HTTPHelper;
import com.artcom.y60.Logger;

public class StatusWatcherTestCase extends ServiceTestCase<StatusWatcher> {

    private static final String LOG_TAG = "StatusWatcherTestCase";
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

    // // Verifies that the service correctly updates the device's
    // // history log in the GOM
    // public void testHistoryUpdates() throws ParseException,
    // InterruptedException {
    // startService(mIntent);
    // Runnable checkGom = new Runnable() {
    // public void run() {
    // try {
    // Thread.sleep(10 * 1000); // Wait for service to settle, GOM
    // // to become available etc
    // } catch (InterruptedException e) {
    // // ignore
    // }
    // String uri = mDeviceConfiguration.getGomUrl() + "/"
    // + mDeviceConfiguration.getDevicePath() + ":history_log.txt";
    // String historyLog = HTTPHelper.get(uri);
    // String lastLine = historyLog.substring(historyLog.lastIndexOf("\n") + 1);
    // String timestamp = lastLine.substring(0, lastLine.indexOf(": "));
    // long now = System.currentTimeMillis();
    // long historyUpdated;
    // try {
    // historyUpdated = new
    // SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(timestamp)
    // .getTime();
    // } catch (ParseException e) {
    // throw new RuntimeException(e.getMessage());
    // }
    // final long TIME_DELTA_TOLERANCE = 11 * 1000; // in milliseconds
    // long delta = Math.abs(historyUpdated - now);
    // assertTrue("Timestamp in GOM (" + timestamp
    // + ") is older than expected. Expected a difference of at most "
    // + (TIME_DELTA_TOLERANCE / 1000) + " seconds, found " + (delta / 1000)
    // + " seconds.", delta < TIME_DELTA_TOLERANCE);
    // }
    // };
    // Thread thread = new Thread(checkGom);
    // thread.start();
    //        
    // while (thread.getState() != Thread.State.TERMINATED){
    // Thread.sleep(100);
    // }
    // }

    public void testWatcherThreadRunning() throws InterruptedException {
        startService(mIntent);

        Thread thread = getService().getWatcherThread();

        assertTrue(thread.isAlive());

        sleepNonblocking(5 * 1000);

        assertTrue(thread.isAlive());
    }

    // This is not strictly a test of StatusWatcher functionality. We just want
    // to know if the behaviour on
    // network disappearance is what we expect it to be.
    public void testCutNetworkConnection() throws IOException, InterruptedException {
        
        // TODO fix me! Kokosnuesse!
        
        startService(mIntent);

        Runtime runtime = Runtime.getRuntime();
        // String cmd = "ifconfig eth0 down && sleep 2 && dhcpcd eth0";
        String cmd = "/data/su -c \"/data/netupdown.sh\"";
        Process process = runtime.exec(cmd);

        assertEquals(1, process.waitFor());
        InputStreamReader reader = new InputStreamReader(process.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(reader);

        String lines = "";
        String currentLine = "";
        while ((currentLine = bufferedReader.readLine()) != null) {
            lines += currentLine;
        }
        Logger.v(LOG_TAG, lines);
        // assertEquals("keks", lines);
    }

    // Verifies that the "GOM unavailable" notification (symbolized by a
    // "West Wind"
    // icon) is correctly displayed/cleared.
    public void testWestWind() throws InterruptedException {
        startService(mIntent);
        sleepNonblocking(2 * 1000);

        Logger.v("westwind", "a");
        assertFalse(
                "Expected the StatusWatcher to see the GOM right after starting up, but it doesn't.",
                getService().isGomAvailable());
        Logger.v("westwind", "now unbinding gom");
        getService().unbindFromGom();
        sleepNonblocking(2 * 1000);
        Logger.v("westwind", "gom unbound (maybe)> ", getService().isGomAvailable());
        getService().bindToGom();
        sleepNonblocking(2 * 1000);
        // assertTrue(
        // "Forced StatusWatcher to unbind from GOM, but StatusWatcher reports that it can still see the GOM",
        // getService().isWestwindBlowing());
        // Logger.v("westwind", "b");
        // Logger.v("westwind", "now rebinding to gom");
        // getService().bindToGom();
        // waitForGom();
        // Logger.v("westwind", "rebound to gom (maybe)");
        // assertFalse(
        // "Expected the StatusWatcher to see the GOM after telling it to re-bind, but it doesn't.",
        // getService().isWestwindBlowing());
        // Logger.v("westwind", "c");

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
