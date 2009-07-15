package com.artcom.y60.dc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.mortbay.jetty.Connector;

import android.content.Intent;
import android.test.AssertionFailedError;
import android.test.ServiceTestCase;

import com.artcom.y60.Constants;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.TestHelper;

public class DeviceControllerServiceTest extends ServiceTestCase<DeviceControllerService> {

    private static final String LOG_TAG = "DeviceControllerServiceTest";

    public DeviceControllerServiceTest() {
        super(DeviceControllerService.class);
    }

    public void assertNoWebserverIsRunning() {
        try {
            HttpHelper.getStatusCode("http://localhost:4042/");
            fail();
        } catch (Exception e) {
            Logger.v(LOG_TAG, "Exception caught: No Webserver is available ");
        }
    }

    public void testAJettyRoundtrip() throws Exception {

        assertNoWebserverIsRunning();

        assertNull(getService());
        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);
        TestHelper.blockUntilWebServerIsRunning();
        assertNotNull(getService());

        int code = HttpHelper.getStatusCode("http://localhost:4042/");
        assertEquals(404, code);
        assertNotNull("Service should not be null", getService());
        assertNotNull("Jetty server should not be null", getService().mServer);

        shutdownService();
        assertNoWebserverIsRunning();
    }

    public void testAutomaticWebserverStartup() throws Exception {

        assertNoWebserverIsRunning();

        assertNull(getService());
        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);
        TestHelper.blockUntilWebServerIsRunning();
        assertNotNull(getService());

        blockUntilWebserverIsStarted();
        assertTrue("webserver does not run", getService().mServer.isRunning());

        assertEquals("webserver has unexpected number of connectors", 1, getService().mServer
                        .getConnectors().length);

        Connector connector = getService().mServer.getConnectors()[0];
        assertEquals("webserver has unexpected number of open connections", 0, connector
                        .getConnections());

        assertEquals("local port", 4042, connector.getLocalPort());

        int code = HttpHelper.getStatusCode("http://localhost:4042/");
        assertEquals(404, code);
        assertNotNull("Service should not be null", getService());
        assertNotNull("Jetty server should not be null", getService().mServer);
        assertTrue("Webserver died", getService().mServer.isRunning());

    }

    public void testGetIpAddress() throws Exception {

        assertNoWebserverIsRunning();

        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);

        TestHelper.blockUntilWebServerIsRunning();

        String addressString = NetworkHelper.getDeviceIpAddress();
        assertNotNull(addressString);
        assertFalse("is a local address", addressString.equals("127.0.0.1"));
        InetAddress address = InetAddress.getByName(addressString);

        assertTrue("local adress " + address + " is not reachable", address.isReachable(100));
    }

    public void testRciUriInGom() throws Exception {

        assertNoWebserverIsRunning();

        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);

        TestHelper.blockUntilWebServerIsRunning();

        String ipAddress;
        ipAddress = NetworkHelper.getDeviceIpAddress();

        DeviceConfiguration dc = DeviceConfiguration.load();
        String rciUri = HttpHelper.getAsString(Constants.Gom.URI + dc.getDevicePath() + ":rci_uri.txt");

        // if executed on emulator
        if (ipAddress.startsWith("10.0.2.")) {
            assertTrue("rci_uri should start with 'http://'", rciUri.startsWith("http://"));
            assertTrue("rci_uri should contain a gallery address ", rciUri.contains("192.168.9."));
            assertTrue("rci_uri should end with ':4042/commands'", rciUri
                            .endsWith(":4042/commands"));
        } else {
            assertEquals("http://" + ipAddress + ":4042/commands", rciUri);
        }
    }

    public void testShutdownService() throws Exception {

        assertNoWebserverIsRunning();

        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);

        TestHelper.blockUntilWebServerIsRunning();

        Thread.sleep(5000);

        int code = HttpHelper.getStatusCode("http://localhost:4042/");
        assertEquals(404, code);
        assertNotNull("Service should not be null", getService());
        assertNotNull("Jetty server should not be null", getService().mServer);
        assertTrue("Webserver died", getService().mServer.isRunning());

    }

    public void testStartup() throws NumberFormatException, UnknownHostException, IOException {
        assertNoWebserverIsRunning();

        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);

        blockUntilWebserverIsStarted();

    }

    public void testStatusCodeOfLocalHost() throws Exception {
        assertNoWebserverIsRunning();

        assertNull(getService());
        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);

        TestHelper.blockUntilEquals("webserver should present expected status code", 3000, 404,
                        new TestHelper.Measurement() {

                            @Override
                            public Object getActualValue() {

                                try {
                                    return HttpHelper.getStatusCode("http://localhost:4042/");
                                } catch (Exception e) {
                                    return e.getMessage();
                                }
                            }
                        });

        int code = HttpHelper.getStatusCode("http://localhost:4042/");
        assertEquals(404, code);
        assertNotNull("Service should not be null", getService());
        assertNotNull("Jetty server should not be null", getService().mServer);
        assertTrue("Webserver died", getService().mServer.isRunning());
    }

    void blockUntilWebserverIsStarted() {

        blockUntilWebserverObjectIsAvailable();

        long requestStartTime = System.currentTimeMillis();
        while (!getService().mServer.isStarted()) {

            if (System.currentTimeMillis() > requestStartTime + 5 * 1000) {
                throw new AssertionFailedError("Timeout while waiting for webserver to start.");
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new AssertionFailedError("interrupt exception");
            }
        }

        assertTrue("Webserver died", getService().mServer.isRunning());
    }

    void blockUntilWebserverObjectIsAvailable() {
        long requestStartTime = System.currentTimeMillis();
        while (getService().mServer == null) {

            if (System.currentTimeMillis() > requestStartTime + 5 * 1000) {
                throw new AssertionFailedError(
                                "Timeout while waiting for webserver object creation.");
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new AssertionFailedError("interrupt exception");
            }
        }

        assertNotNull("Webserver object is null", getService().mServer);
    }

}
