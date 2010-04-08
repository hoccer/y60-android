package com.artcom.y60.hoccer;

import android.test.AndroidTestCase;

import com.artcom.y60.TestHelper;

public class HocEventTestCase extends AndroidTestCase {
    
    private Peer mPeer;
    
    @Override
    public void setUp() {
        mPeer = new Peer("Y60/Hoccer Unit Test on Android");
    }
    
    @Override
    public void tearDown() {
        mPeer = null;
    }
    
    protected Peer getPeer() {
        return mPeer;
    }
    
    protected void assertEventIsAlive(String pEventName, final HocEvent pEvent) throws Exception {
        TestHelper.blockUntilTrue(pEventName + " event should have been created", 10000,
                new TestHelper.Condition() {
                    
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return pEvent.isAlive();
                    }
                });
    }
    
    protected void assertEventIsExpired(String pEventName, final HocEvent pEvent) throws Exception {
        TestHelper.blockUntilFalse(pEventName + " event shuld be expired by now", 7000,
                new TestHelper.Condition() {
                    
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return pEvent.isAlive();
                    }
                });
    }
    
}
