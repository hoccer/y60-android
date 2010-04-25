package com.artcom.y60.hoccer;

import junit.framework.Assert;
import android.test.suitebuilder.annotation.LargeTest;

import com.artcom.y60.data.GenericStreamableContent;
import com.artcom.y60.http.AsyncHttpPut;

public class TestIncompleteTransfers extends HocEventTestCase {
    
    @LargeTest
    public void testNotUploadingContentAfterSuccsessfulLink() throws Exception {
        
        GenericStreamableContent content = new GenericStreamableContent();
        content.setContentType("text/plain");
        content.openOutputStream().write("my hocced text".getBytes(), 0, "my hocced text".length());
        
        final SweepOutEvent sweepOut = getPeer().sweepOut(content);
        // this will break the upload
        sweepOut.mDataUploader = new AsyncHttpPut("nothing");
        
        assertEventIsAlive("sweepOut", sweepOut);
        final SweepInEvent sweepIn = getPeer().sweepIn();
        assertEventIsAlive("sweepIn", sweepIn);
        
        assertEventIsLinked(sweepOut);
        assertEventIsLinked(sweepIn);
        
        Thread.sleep(1000);
        if (sweepIn.mDataDownloader != null) {
            Assert.fail("download should have not been started but answered with status code "
                    + sweepIn.mDataDownloader.getStatusCode());
        }
    }
}
