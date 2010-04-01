package com.artcom.y60.thread;

import android.test.AndroidTestCase;

import com.artcom.y60.TestHelper;

public class TestThreadedTask extends AndroidTestCase {
    
    ThreadedTask mTask = null;
    
    public void testConstruction() throws Exception {
        
        assertNull(mTask);
        mTask = new ThreadedTask() {
            @Override
            public void run() {
            }
        };
        assertNotNull(mTask);
        assertFalse("task should not have started", mTask.isAlive());
        assertEquals("task should be in Thread.State.NEW", Thread.State.NEW, mTask.getState());
    }
    
    public void testProgress() throws Exception {
        mTask = new ThreadedTask() {
            @Override
            public void run() {
                setProgress(1);
            }
        };
        assertEquals("task should start with zero progress", 0, mTask.getProgress());
        
        mTask.start();
        Thread.sleep(1000);
        TestHelper.assertGreater("progress should increase", 1, mTask.getProgress());
    }
}
