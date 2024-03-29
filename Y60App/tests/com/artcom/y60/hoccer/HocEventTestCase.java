package com.artcom.y60.hoccer;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.test.AndroidTestCase;

import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.http.AsyncHttpRequestWithBody;

public class HocEventTestCase extends AndroidTestCase {

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
            mLocation = new HocLocation(location, new ArrayList<ScanResult>());
        }
    }

    @Override
    public void setUp() {
        mPeer = Peer.createPeer("Y60/Hoccer Unit Test on Android", "http://beta.hoccer.com",
                getContext());
        mPeer.setLocation(getUniqueGpsLocation());
    }

    @Override
    public void tearDown() {
        mPeer = null;
    }

    protected Peer getPeer() {
        return mPeer;
    }

    protected HocLocation getUniqueGpsLocation() {
        return getUniqueGpsLocation(new ArrayList<AccessPointSighting>());
    }

    protected HocLocation getNearbyGpsLocation() {
        HocLocation location = new HocLocation(mLocation);
        location.setLatitude(location.getLatitude() + 0.00001);
        return location;
    }

    protected HocLocation getUniqueGpsLocation(ArrayList<AccessPointSighting> sightings) {
        mLocation.setLatitude(mLocation.getLatitude() + 0.1);
        mLocation.setLongitude(mLocation.getLongitude() + 0.1);

        Location location = new Location("TestLocationProvider");
        location.setAccuracy(mLocation.getAccuracy());
        location.setLatitude(mLocation.getLatitude());
        location.setLongitude(mLocation.getLongitude());
        HocLocation hocLocation = new HocLocation(location, sightings);

        return hocLocation;
    }

    protected void assertPollingHasStopped(final HocEvent hocEvent) throws Exception {
        TestHelper.blockUntilTrue("polling should not be running", 4000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return hocEvent.mStatusFetcher == null;
                    }

                });

        Thread.sleep(2000);
        assertNull("Async Http Request polling should still not be running",
                hocEvent.mStatusFetcher);
    }

    protected void blockUntilEventIsAlive(String pEventName, final HocEvent pEvent)
            throws Exception {
        TestHelper.blockUntilTrue(pEventName + " event should have been created", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        assertFalse("event should not collide with others", pEvent.hasCollision());
                        assertFalse("event should have no errors, but reported '"
                                + pEvent.getMessage() + "'", pEvent.hasError());
                        return pEvent.isOpenForLinking();
                    }
                });
    }

    protected void blockUntilEventHasCollision(String pEventName, final HocEvent pEvent)
            throws Exception {
        TestHelper.blockUntilTrue(pEventName + " event should have been created", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return pEvent.hasCollision();
                    }
                });
    }

    protected void blockUntilEventIsExpired(String pEventName, final HocEvent pEvent)
            throws Exception {

        TestHelper.blockUntilTrue(pEventName + " event shuld be expired by now", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return !pEvent.getState().equals("unborn") && !pEvent.isOpenForLinking();
                    }
                });
    }

    protected void blockUntilEventHasNumberOfPeers(final HocEvent pEvent, int expectedPeerCount)
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
                        return hocEvent.getRemainingLifetime() < lifetime
                                && hocEvent.getRemainingLifetime() > 0;
                    }
                });
    }

    protected void blockUntilLifetimeIsDownTo(final HocEvent hocEvent, final double targetedLifetime)
            throws Exception {
        TestHelper.blockUntilTrue("lifetime should be down to " + targetedLifetime + " but is "
                + hocEvent.getRemainingLifetime(), 8000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() throws Exception {
                return hocEvent.getRemainingLifetime() <= targetedLifetime;
            }
        });
    }

    protected void blockUntilEventIsSuccessful(final HocEventListenerForTesting eventCallback,
            long timeout) throws Exception {
        TestHelper.blockUntilTrue("event should be successful", timeout,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return eventCallback.wasSuccessful;
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

    protected void assertDataHasNotBeenUploaded(final ShareEvent shareEvent) throws Exception {
        assertFalse("data should not have been uploaded", shareEvent.hasDataBeenUploaded());
    }

    protected void assertDataHasNotBeenDownloaded(final ReceiveEvent receiveEvent) throws Exception {
        assertFalse("data should not have been downloaded", receiveEvent.hasDataBeenDownloaded());
    }

    protected void blockUntilDataHasBeenDownloaded(final ReceiveEvent receiveEvent,
            String pExpectedData) throws Exception {
        blockUntilDownloadIsDone(receiveEvent);

        assertEquals("incomming data should be as expected", pExpectedData, receiveEvent.getData()
                .toString());
    }

    protected void blockUntilDataHasBeenDownloaded(final ReceiveEvent receiveEvent,
            byte[] pExpectedData) throws Exception {
        blockUntilDownloadIsDone(receiveEvent);

        TestHelper.assertInputStreamEquals("incomming data should be as expected",
                new ByteArrayInputStream(pExpectedData), receiveEvent.getData().openInputStream());
    }

    private void blockUntilDownloadIsDone(final ReceiveEvent receiveEvent) throws Exception {
        TestHelper.blockUntilTrue("downloader request should have been created", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return receiveEvent.mDataDownloader != null;
                    }
                });

        TestHelper.blockUntilTrue("download should have finished", 10000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return receiveEvent.hasDataBeenDownloaded();
                    }
                });
    }
}
