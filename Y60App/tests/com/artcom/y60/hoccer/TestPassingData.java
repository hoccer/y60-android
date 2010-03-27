package com.artcom.y60.hoccer;

import android.test.AndroidTestCase;

import com.artcom.y60.TestHelper;

public class TestPassingData extends AndroidTestCase {
    
    HocEvent mEvent;
    
    public void testLonelySweepOutEvent() throws Exception {
        Peer peer = new Peer();
        mEvent = peer.sweepOut();
        
        TestHelper.blockUntilTrue("sweepOut event created", 1000, new TestHelper.Condition() {
            
            @Override
            public boolean isSatisfied() throws Exception {
                return mEvent.isAlive();
            }
        });
        
        float lifetime = mEvent.getLifetime();
        assertTrue("lifetime is still fine", lifetime > 5 * 1000);
        Thread.sleep(1000);
        assertTrue("lifetime is decreasing", mEvent.getLifetime() < lifetime);
        
        TestHelper.blockUntilFalse("sweepOut event expired", 7000, new TestHelper.Condition() {
            
            @Override
            public boolean isSatisfied() throws Exception {
                return mEvent.isAlive();
            }
        });
        assertEquals("lifetime is zero", 0, mEvent.getLifetime());
    }
}
