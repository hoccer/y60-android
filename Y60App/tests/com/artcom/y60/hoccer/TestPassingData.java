package com.artcom.y60.hoccer;

import com.artcom.y60.TestHelper;

public class TestPassingData extends HocEventTestCase {
    
    private HocEvent mEvent;
    
    public void testLonelySweepOutEvent() throws Exception {
        mEvent = getPeer().sweepOut();
        assertEventIsAlive("sweepOut", mEvent);
        
        TestHelper.assertMatches("event shuld have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", mEvent.getResourceLocation());
        
        double lifetime = mEvent.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        Thread.sleep(2000);
        assertTrue("lifetime should be decreasing", mEvent.getLifetime() < lifetime);
        
        assertEventIsExpired("sweepOut", mEvent);
        assertEquals("lifetime should be down to zero", 0.0, mEvent.getLifetime());
    }
    
    public void testLonelySweepInEvent() throws Exception {
        mEvent = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", mEvent);
        
        TestHelper.assertMatches("event shuld have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", mEvent.getResourceLocation());
        
        double lifetime = mEvent.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        Thread.sleep(2000);
        assertTrue("lifetime should be decreasing", mEvent.getLifetime() < lifetime);
        
        assertEventIsExpired("sweepIn", mEvent);
        assertEquals("lifetime should be down to zero", 0.0, mEvent.getLifetime());
    }
    
    public void testLinkingSweepInAndOutEvents() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut();
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);
        
        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);
    }
    
}
