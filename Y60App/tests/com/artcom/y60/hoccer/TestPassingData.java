package com.artcom.y60.hoccer;

import com.artcom.y60.TestHelper;
import com.artcom.y60.data.StreamableString;

public class TestPassingData extends HocEventTestCase {
    
    private HocEvent mEvent;
    
    public void testLonelySweepOutEvent() throws Exception {
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        assertEventIsAlive("sweepOut", sweepOut);
        
        TestHelper.assertMatches("event shuld have a valid resource location", HocEvent
                .getRemoteServer()
                + "/events/\\w*", sweepOut.getResourceLocation());
        
        double lifetime = sweepOut.getLifetime();
        TestHelper.assertGreater("lifetime should be fine", 5, lifetime);
        assertLifetimeDecreases(sweepOut, lifetime);
        
        assertEventIsExpired("sweepOut", sweepOut);
        assertEquals("lifetime should be down to zero", 0.0, sweepOut.getLifetime());
        assertDataHasBeenUploaded(sweepOut);
    }
    
    private void assertLifetimeDecreases(final SweepOutEvent sweepOut, final double lifetime)
            throws Exception {
        TestHelper.blockUntilTrue("lifetime should be decreasing", 5000,
                new TestHelper.Condition() {
                    
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return sweepOut.getLifetime() < lifetime && sweepOut.getLifetime() > 0;
                    }
                });
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
        SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);
        
        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);
    }
    
    public void testTransferingDataBetweenSweepInAndOutEvents() throws Exception {
        
        final SweepOutEvent sweepOut = getPeer().sweepOut(new StreamableString("my hocced data"));
        assertEventIsAlive("sweepOut", sweepOut);
        SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);
        
        assertEventHasNumberOfPeers(sweepOut, 1);
        assertEventHasNumberOfPeers(sweepIn, 1);
        
        assertDataHasBeenUploaded(sweepOut);
        assertDataHasBeenDownloaded(sweepIn);
    }
    
    private void assertDataHasBeenUploaded(final SweepOutEvent sweepOut) throws Exception {
        TestHelper.blockUntilTrue("uploader request should have been created", 10000,
                new TestHelper.Condition() {
                    
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return sweepOut.mDataUploader != null;
                    }
                });
        
        TestHelper.blockUntilTrue("upload should have finished", 10000, new TestHelper.Condition() {
            
            @Override
            public boolean isSatisfied() throws Exception {
                return sweepOut.hasDataBeenUploaded();
            }
        });
    }
    
    private void assertDataHasBeenDownloaded(final SweepInEvent sweepIn) throws Exception {
        TestHelper.blockUntilTrue("downloader request should have been created", 10000,
                new TestHelper.Condition() {
                    
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return sweepIn.mDataDownloader != null;
                    }
                });
        
        TestHelper.blockUntilTrue("download should have finished", 10000,
                new TestHelper.Condition() {
                    
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return sweepIn.hasDataBeenDownloaded();
                    }
                });
    }
}
