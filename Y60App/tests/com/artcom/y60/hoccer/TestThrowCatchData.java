package com.artcom.y60.hoccer;

import com.artcom.y60.TestHelper;
import com.artcom.y60.data.StreamableString;

public class TestThrowCatchData extends HocEventTestCase {

    public void testLonelyThrow() throws Exception {

        ThrowEvent hocEvent = getPeer().throwIt(new StreamableString("my hocced data"));
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        hocEvent.addCallback(eventCallback);
        assertEventIsAlive("throw", hocEvent);

        TestHelper.assertMatches("event should have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", hocEvent.getResourceLocation());

        double lifetime = hocEvent.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        blockUntilLifetimeDecreases(hocEvent, lifetime);

        assertEventIsExpired("throw", hocEvent);
        assertEquals("lifetime should be down to zero", 0.0, hocEvent.getLifetime());
        blockUntilDataHasBeenUploaded(hocEvent);
        assertTrue("should have got error callback", eventCallback.hadError);
        assertPollingHasStopped(hocEvent);
    }

    public void testCollisionOfTwoThrowerOneCatcher() throws Exception {

        StreamableString content = new StreamableString("provoke collision");

        final ThrowEvent throwEventA = getPeer().throwIt(content);
        assertEventIsAlive("throwEventA", throwEventA);

        CatchEvent catchEvent = getPeer().catchIt();
        assertEventIsAlive("catchEvent", catchEvent);

        assertEventHasNumberOfPeers(throwEventA, 1);
        assertEventHasNumberOfPeers(catchEvent, 1);

        blockUntilEventIsLinked(throwEventA);
        blockUntilEventIsLinked(catchEvent);

        blockUntilDataHasBeenUploaded(throwEventA);
        // blockUntilDataHasBeenDownloaded(sweepIn, "my hocced text");
        assertEquals("mime type should be as expected", "text/plain", catchEvent.getData()
                .getContentType());
        assertEquals("filename should be as expected", "thedemofilename.txt", catchEvent.getData()
                .getFilename());
        assertPollingHasStopped(catchEvent);
        assertPollingHasStopped(throwEventA);
    }
}
