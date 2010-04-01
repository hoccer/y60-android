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
                sleep();
                setProgress(53);
                sleep();
                setProgress(99);
                sleep();
                setProgress(100);
            }
            
            private void sleep() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // this is fine for testing purposes
                    throw new RuntimeException(e);
                }
            }
        };
        
        assertEquals("task should start with zero progress", 0, mTask.getProgress());
        mTask.start();
        
        TestHelper.blockUntilEquals("progress should increase", 300, 1,
                new TestHelper.Measurement() {
                    
                    @Override
                    public Object getActualValue() throws Exception {
                        return mTask.getProgress();
                    }
                });
        
        TestHelper.blockUntilEquals("progress should increase", 300, 53,
                new TestHelper.Measurement() {
                    
                    @Override
                    public Object getActualValue() throws Exception {
                        return mTask.getProgress();
                    }
                });
        
        TestHelper.blockUntilEquals("progress should increase", 300, 99,
                new TestHelper.Measurement() {
                    
                    @Override
                    public Object getActualValue() throws Exception {
                        return mTask.getProgress();
                    }
                });
        
        TestHelper.blockUntilEquals("progress should increase", 300, 100,
                new TestHelper.Measurement() {
                    
                    @Override
                    public Object getActualValue() throws Exception {
                        return mTask.getProgress();
                    }
                });
        
    }
}
