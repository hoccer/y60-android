package com.artcom.y60.hoccer;

import java.util.ArrayList;

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
                .getRemainingLifetime());
        TestHelper.assertGreater("sweep in should still have some time", 1, sweepIn
                .getRemainingLifetime());

        blockUntilLifetimeIsDownTo(sweepOut, 0);
        blockUntilLifetimeIsDownTo(sweepIn, 0);

        assertTrue("linking should be successful", sweepOut.isLinkEstablished()
                && sweepIn.isLinkEstablished());
    }

    public void testNotLinkingWhenLocationsDeffere() throws Exception {
        Peer sendingPeer = Peer.getPeer("Y60/Hoccer Unit Test on Android", getPeer()
                .getRemoteServer(), getContext());
        Peer receivingPeer = Peer.getPeer("Y60/Hoccer Unit Test on Android", getPeer()
                .getRemoteServer(), getContext());

        HocLocation sendingLocation = getUniqueGpsLocation();
        sendingPeer.setLocation(sendingLocation);
        HocLocation receivingLocation = getUniqueGpsLocation();
        receivingPeer.setLocation(receivingLocation);

        SweepOutEvent sweepOut = sendingPeer.sweepOut(new StreamableString("my hocced data"));
        SweepInEvent sweepIn = receivingPeer.sweepIn();
        blockUntilEventIsAlive("sweepOut", sweepOut);
        blockUntilEventIsAlive("sweepIn", sweepIn);

        blockUntilEventIsExpired("sweepOut", sweepOut);
        blockUntilEventIsExpired("sweepIn", sweepIn);

        assertDataHasNotBeenDownloaded(sweepIn);
    }

    public void testLinkingThroughBssids() throws Exception {
        Peer sendingPeer = Peer.getPeer("Y60/Hoccer Unit Test on Android", getPeer()
                .getRemoteServer(), getContext());
        Peer receivingPeer = Peer.getPeer("Y60/Hoccer Unit Test on Android", getPeer()
                .getRemoteServer(), getContext());

        ArrayList<AccessPointSighting> bssidsOfSender = new ArrayList<AccessPointSighting>();
        bssidsOfSender.add(new AccessPointSighting("00:22:3f:2e:33:e8", 3));
        bssidsOfSender.add(new AccessPointSighting("00:22:3f:10:a8:bf", -23));
        HocLocation sendingLocation = getUniqueGpsLocation(bssidsOfSender);
        sendingPeer.setLocation(sendingLocation);

        ArrayList<AccessPointSighting> bssidsOfReceiver = new ArrayList<AccessPointSighting>();
        bssidsOfReceiver.add(new AccessPointSighting("00:22:3f:2e:33:e8", 3));
        bssidsOfReceiver.add(new AccessPointSighting("0:24:fe:4a:7f:4f", 12));
        HocLocation receivingLocation = getUniqueGpsLocation(bssidsOfReceiver);
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
    }

    public void testLinkingViaAccuracyWhenLocationsDeffereSlightly() throws Exception {
        Peer sendingPeer = Peer.getPeer("Y60/Hoccer Unit Test on Android", getPeer()
                .getRemoteServer(), getContext());
        Peer receivingPeer = Peer.getPeer("Y60/Hoccer Unit Test on Android", getPeer()
                .getRemoteServer(), getContext());

        HocLocation sendingLocation = getUniqueGpsLocation();
        sendingPeer.setLocation(sendingLocation);
        HocLocation receivingLocation = getNearbyGpsLocation();
        receivingLocation.setLatitude(sendingLocation.getLatitude() + 0.01);
        receivingLocation.setLongitude(sendingLocation.getLongitude() + 0.01);
        receivingLocation.setAccuracy(1000);
        receivingPeer.setLocation(receivingLocation);

        SweepOutEvent sweepOut = sendingPeer.sweepOut(new StreamableString("my hocced data"));
        SweepInEvent sweepIn = receivingPeer.sweepIn();
        blockUntilEventIsAlive("sweepOut", sweepOut);
        blockUntilEventIsAlive("sweepIn", sweepIn);

        blockUntilEventIsLinked(sweepOut);
        blockUntilEventIsLinked(sweepIn);
    }

}
