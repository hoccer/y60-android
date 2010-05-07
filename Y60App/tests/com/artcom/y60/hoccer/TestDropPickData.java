package com.artcom.y60.hoccer;

import com.artcom.y60.TestHelper;
import com.artcom.y60.data.StreamableString;

public class TestDropPickData extends HocEventTestCase {

    public void testDrop() throws Exception {

        DropEvent hoc = getPeer().drop(new StreamableString("my hocced data"), 30);
        blockUntilEventIsAlive("drop", hoc);

        TestHelper.assertMatches("event should have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", hoc.getResourceLocation());

        double lifetime = hoc.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 18, lifetime);
        blockUntilLifetimeDecreases(hoc, lifetime);

        blockUntilDataHasBeenUploaded(hoc);
    }

    public void testEmptyPick() throws Exception {

        PickEvent hoc = getPeer().pick();
        HocEventListenerForTesting eventCallback = new HocEventListenerForTesting();
        hoc.addCallback(eventCallback);
        blockUntilEventIsExpired("pick", hoc);

        TestHelper.assertMatches("event should have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", hoc.getResourceLocation());

        assertEquals("lifetime should be down to zero", 0.0, hoc.getLifetime());
        assertTrue("should have got error", hoc.hasError());
        assertEquals("status message", "Nothing to pick up from this location", hoc.getMessage());
        assertTrue("should have got error callback", eventCallback.hadError);
    }

    public void testDropPickExampleFlow() throws Exception {

        DropEvent drop = getPeer().drop(new StreamableString("the dropped data"), 10);
        blockUntilEventIsAlive("drop", drop);
        blockUntilDataHasBeenUploaded(drop);
        TestHelper.assertGreater("lifetime should be fine", 8, drop.getLifetime());

        PickEvent pick = getPeer().pick();
        blockUntilEventIsExpired("pick", pick);
        blockUntilEventIsLinked(pick);

        TestHelper.assertGreater("lifetime should be fine", 2, drop.getLifetime());
        blockUntilEventIsExpired("drop", drop);

        // pick.getListOfPieces();
    }
}
