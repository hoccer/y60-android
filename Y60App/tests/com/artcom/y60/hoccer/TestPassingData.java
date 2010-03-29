package com.artcom.y60.hoccer;

import android.test.AndroidTestCase;

import com.artcom.y60.TestHelper;

public class TestPassingData extends AndroidTestCase {
    
    HocEvent mEvent;
    
    public void testLonelySweepOutEvent() throws Exception {
        Peer peer = new Peer();
        mEvent = peer.sweepOut();
        
        TestHelper.blockUntilTrue("sweepOut event should have been created", 1000,
                new TestHelper.Condition() {
                    
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return mEvent.isAlive();
                    }
                });
        
        assertEquals("event shuld have a valid resource location", HocEvent.getRemoteServer()
                + "/events/", mEvent.getResourceLocation());
        
        double lifetime = mEvent.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        Thread.sleep(1000);
        assertTrue("lifetime should be decreasing", mEvent.getLifetime() < lifetime);
        
        TestHelper.blockUntilFalse("sweepOut event shuld be expired by now", 7000,
                new TestHelper.Condition() {
                    
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return mEvent.isAlive();
                    }
                });
        assertEquals("lifetime should be down to zero", 0, mEvent.getLifetime());
    }
}
