package com.artcom.y60.hoccer;

import com.artcom.y60.TestHelper;
import com.artcom.y60.data.StreamableString;

public class TestDropPickData extends HocEventTestCase {
    
    public void testDrop() throws Exception {
        
        DropEvent hoc = getPeer().drop(new StreamableString("my hocced data"));
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
        
        TestHelper.assertMatches("event should have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", hoc.getResourceLocation());
        
        blockUntilEventIsExpired("sweepIn", hoc);
        assertEquals("lifetime should be down to zero", 0.0, hoc.getLifetime());
        assertTrue("should have got error callback", eventCallback.hadError);
    }
}
