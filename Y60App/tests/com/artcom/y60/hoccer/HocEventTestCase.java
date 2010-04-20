package com.artcom.y60.hoccer;

import java.util.ArrayList;

import junit.framework.TestCase;
import android.location.Location;
import android.net.wifi.ScanResult;

import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;

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

}
