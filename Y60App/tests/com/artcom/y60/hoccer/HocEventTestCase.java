package com.artcom.y60.hoccer;

import java.lang.reflect.Method;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import android.location.Location;
import android.net.wifi.ScanResult;

import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.http.AsyncHttpRequestWithBody;

public class HocEventTestCase extends TestCase {

    private static String      LOG_TAG   = "HocEventTestCase";
    private static HocLocation mLocation = null;
    private Peer               mPeer;

    public HocEventTestCase() {
        if (mLocation == null) {
            Logger
                    .v(LOG_TAG,
                            "created a default HocLocation -- all tests need to use getUniqueHocLocation()");
            Location location = new Location("TestLocationProvider");
            location.setAccuracy(100);
            location.setLatitude((Math.random() + 0.1) * 20);
            location.setLongitude((Math.random() + 0.1) * 20);
            mLocation = HocLocation.createFromLocation(location, new ArrayList<ScanResult>());
        }
    }

    @Override
    public void setUp() {
        mPeer = new Peer("Y60/Hoccer Unit Test on Android");
        mPeer.setLocation(getUniqueHocLocation());
    }

    @Override
    public void tearDown() {
        mPeer = null;
    }

    protected Peer getPeer() {
        return mPeer;
    }

    protected HocLocation getUniqueHocLocation() {
        mLocation.setLatitude(mLocation.getLatitude() + 0.1);
        mLocation.setLongitude(mLocation.getLongitude() + 0.1);
        return mLocation;
    }

    protected void assertPollingHasStopped(HocEvent hocEvent) throws Exception {
        assertNull("Async Http Request polling should not be running", hocEvent.mStatusFetcher);
        Thread.sleep(2000);
        assertNull("Async Http Request polling should still not be running",
                hocEvent.mStatusFetcher);
    }

    protected void assertEventIsAlive(String pEventName, final HocEvent pEvent) throws Exception {
        TestHelper.blockUntilTrue(pEventName + " event should have been created", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        assertFalse("event should not collide with others", pEvent.hasCollision());
                        return pEvent.isAlive();
                    }
                });
    }

    protected void assertEventIsExpired(String pEventName, final HocEvent pEvent) throws Exception {
        TestHelper.blockUntilFalse(pEventName + " event shuld be expired by now", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return pEvent.isAlive();
                    }
                });
    }

    protected void assertEventHasNumberOfPeers(final HocEvent pEvent, int expectedPeerCount)
            throws Exception {
        TestHelper.blockUntilEquals(" HocEvent shuld have " + expectedPeerCount + " peers by now",
                7000, expectedPeerCount, new TestHelper.Measurement() {

                    @Override
                    public Object getActualValue() throws Exception {
                        return pEvent.getLinkedPeerCount();
                    }
                });
    }

    protected void blockUntilEventIsLinked(final HocEvent pEvent) throws Exception {
        TestHelper.blockUntilTrue("link should be established", 10000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() throws Exception {
                return pEvent.isLinkEstablished();
            }
        });
    }

    protected void assertEventIsNotLinked(final HocEvent pEvent) throws Exception {
        assertFalse(pEvent + " should not be linked", pEvent.isLinkEstablished());
    }

    protected void blockUntilLifetimeDecreases(final HocEvent hocEvent, final double lifetime)
            throws Exception {
        TestHelper.blockUntilTrue("lifetime should be decreasing", 5000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return hocEvent.getLifetime() < lifetime && hocEvent.getLifetime() > 0;
                    }
                });
    }

    protected void blockUntilDataHasBeenUploaded(final ShareEvent shareEvent) throws Exception {
        TestHelper.blockUntilTrue("uploader request should have been created", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return shareEvent.mDataUploader != null;
                    }
                });

        TestHelper.blockUntilTrue("upload should have finished", 10000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() throws Exception {
                return shareEvent.hasDataBeenUploaded();
            }
        });

        Method m = AsyncHttpRequestWithBody.class.getDeclaredMethod("getRequest", null);
        m.setAccessible(true);
        HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) m.invoke(
                shareEvent.mDataUploader, null);
        String body = request.getEntity().toString();

        Logger.v(LOG_TAG, "body", body);
        assertEquals("uploaded data should have correct content-type", shareEvent.getData()
                .getContentType(), body.substring(body.indexOf("Content-Type: ") + 14, body
                .indexOf("\r\nContent-Transfer-Encoding")));
        assertEquals("uploaded data should have correct should have correct filename", shareEvent
                .getData().getFilename(), body.substring(body.indexOf("filename=\"") + 10, body
                .indexOf("\"\r\nContent-Type")));
    }
}
