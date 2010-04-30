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

        assertEventIsExpired("sweepOut", hocEvent);
        assertEquals("lifetime should be down to zero", 0.0, hocEvent.getLifetime());
        blockUntilDataHasBeenUploaded(hocEvent);
        assertTrue("should have got error callback", eventCallback.hadError);
        assertPollingHasStopped(hocEvent);

    }

}
