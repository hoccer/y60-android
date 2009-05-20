package com.artcom.y60.dc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.mortbay.jetty.Connector;

import com.artcom.y60.Logger;

import android.content.Intent;
import android.test.AssertionFailedError;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.Suppress;

public class DeviceControllerServiceTest extends ServiceTestCase<DeviceControllerService> {

    private static final String TEST_PORT = "4042";
    public static final boolean TEST_NIO = true;

    public DeviceControllerServiceTest() {
        super(DeviceControllerService.class);
    }

    public void testAutomaticWebserverStartup() {

        assertNull(getService());
        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);
        assertNotNull(getService());

        blockUntilWebserverIsStarted();
        assertTrue("webserver does not run", getService().mServer.isRunning());

        assertEquals("webserver has unexpected number of connectors", 1,
                getService().mServer.getConnectors().length);

        Connector connector = getService().mServer.getConnectors()[0];
        assertEquals("webserver has unexpected number of open connections", 0,
                connector.getConnections());
        
        assertEquals("local port", 4042, connector.getLocalPort());

    }

    public void testShutdownService() throws IOException {
        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);

        shutdownService();
        assertNull(getService().mServer);
    }

    public void testGetIpAddress() throws IOException {
        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);

        String addressString = getService().getIpAddress();
        assertNotNull(addressString);
        assertFalse("is a local address", addressString.equals("172.0.0.1"));
        Logger.e("keks", addressString);
        InetAddress address = InetAddress.getByName(addressString);

        assertTrue("local adress " + address + " is not reachable", address.isReachable(100));
    }

    @Suppress
    public void testStartup() throws NumberFormatException, UnknownHostException, IOException {

        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        startService(startIntent);

        blockUntilWebserverIsStarted();
        Socket socket = new Socket("localhost", Integer.parseInt(TEST_PORT));
        socket.close();
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
