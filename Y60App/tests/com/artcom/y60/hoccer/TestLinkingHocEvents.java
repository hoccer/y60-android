package com.artcom.y60.hoccer;

import com.artcom.y60.TestHelper;
import com.artcom.y60.data.StreamableString;

public class TestLinkingHocEvents extends HocEventTestCase {

    public void testLinkingSweepInAndOutEvents() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        blockUntilEventIsAlive("sweepOut", sweepOut);

        SweepInEvent sweepIn = getPeer().sweepIn();
        blockUntilEventIsAlive("sweepIn", sweepIn);

        blockUntilEventHasNumberOfPeers(sweepOut, 1);
        blockUntilEventHasNumberOfPeers(sweepIn, 1);

        blockUntilEventIsLinked(sweepOut);
        blockUntilEventIsLinked(sweepIn);
    }

    public void testLinkingWithDelayedSweepIn() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        blockUntilEventIsAlive("sweepOut", sweepOut);

        blockUntilLifetimeIsDownTo(sweepOut, 3);

        SweepInEvent sweepIn = getPeer().sweepIn();
        blockUntilEventIsAlive("sweepIn", sweepIn);
        TestHelper.assertSmaller("sweepIn should get lifetime from sweepOut", 4, sweepIn
                .getLifetime());
        TestHelper.assertGreater("sweep in should still have some time", 1, sweepIn.getLifetime());

        blockUntilLifetimeIsDownTo(sweepOut, 0);
        blockUntilLifetimeIsDownTo(sweepIn, 0);

        assertTrue("linking should be successful", sweepOut.isLinkEstablished()
                && sweepIn.isLinkEstablished());
    }

    public void testNotLinkingWhenLocationsDeffere() throws Exception {
        Peer sendingPeer = new Peer("Y60/Hoccer Unit Test on Android");
        Peer receivingPeer = new Peer("Y60/Hoccer Unit Test on Android");

        HocLocation sendingLocation = getUniqueGpsLocation();
        sendingPeer.setLocation(sendingLocation);
        HocLocation receivingLocation = getUniqueGpsLocation();
        receivingPeer.setLocation(receivingLocation);

        SweepOutEvent sweepOut = sendingPeer.sweepOut(new StreamableString("my hocced data"));
        SweepInEvent sweepIn = receivingPeer.sweepIn();

        blockUntilEventIsExpired("sweepOut", sweepOut);
        blockUntilEventIsExpired("sweepIn", sweepIn);

        assertDataHasNotBeenDownloaded(sweepIn);
    }

    public void testLinkingThroughBssids() throws Exception {
        Peer sendingPeer = new Peer("Y60/Hoccer Unit Test on Android");
        Peer receivingPeer = new Peer("Y60/Hoccer Unit Test on Android");

        HocLocation sendingLocation = getUniqueGpsLocation();
        sendingPeer.setLocation(sendingLocation);
        HocLocation receivingLocation = getUniqueGpsLocation();
        receivingPeer.setLocation(receivingLocation);

        assertNotSame(receivingLocation, sendingLocation);
        assertNotSame(receivingLocation.getLatitude(), sendingLocation.getLatitude());
        assertNotSame(receivingLocation.getLongitude(), sendingLocation.getLongitude());

        SweepOutEvent sweepOut = sendingPeer.sweepOut(new StreamableString("my hocced data"));
        SweepInEvent sweepIn = receivingPeer.sweepIn();

        blockUntilEventIsAlive("sweepOut", sweepOut);
        blockUntilEventIsAlive("sweepIn", sweepIn);

        blockUntilEventIsExpired("sweepOut", sweepOut);
        blockUntilEventIsLinked(sweepOut);
        blockUntilEventIsLinked(sweepIn);
        blockUntilDataHasBeenDownloaded(sweepIn, "my hocced data");
        assertTrue(false);
    }
}
