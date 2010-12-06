package com.artcom.y60.thread;

import com.artcom.y60.TestHelper;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.Suppress;

public class TestThreadedTask extends AndroidTestCase {

    ThreadedTask mTask = null;

    @Override
    public void tearDown() {
        mTask = null;
    }

    public void testConstruction() throws Exception {

        assertNull(mTask);
        mTask = new ThreadedTask() {
            @Override
            public void doInBackground() {
            }
        };
        assertNotNull(mTask);
        assertFalse("task should not have started", mTask.isAlive());
        assertEquals("task should be in Thread.State.NEW", Thread.State.NEW, mTask.getState());
    }

    public void testProgressWithSaneValues() throws Exception {
        mTask = new ThreadedTaskForTesting() {
            @Override
            public void doInBackground() {
                setProgress(1);
                sleep();
                setProgress(53);
                sleep();
                setProgress(99);
                sleep();
            }
        };

        assertEquals("task should start with 0% progress", 0, mTask.getProgress());
        mTask.start();

        assertProgress("progress should increase", 1);
        assertProgress("progress should increase", 53);
        assertProgress("progress should increase", 99);
        assertProgress("progress should automaticly be set to 100% if task is done", 100);
    }

    @Suppress
    public void testProgressWithBadValues() throws Exception {
        mTask = new ThreadedTaskForTesting() {
            @Override
            public void doInBackground() {
                setProgress(-1);
                sleep();
                setProgress(2011);
            }
        };
        assertEquals("task should start with 0% progress", 0, mTask.getProgress());
        mTask.start();
        Thread.sleep(100);
        assertEquals("task should stay at 0% progress", 0, mTask.getProgress());
        Thread.sleep(100);
        assertEquals("task should clamp at 100% progress", 100, mTask.getProgress());
    }

    public void testAskingForSuccess() throws Exception {
        mTask = new ThreadedTaskForTesting() {
            @Override
            public void doInBackground() {
            }
        };
        mTask.start();
        TestHelper.blockUntilTrue("Task should tell about it's success", 200,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return mTask.isTaskCompleted();
                    }
                });
    }

    private void assertProgress(String pMessage, int pExpectedPercent) throws Exception {
        TestHelper.blockUntilEquals(pMessage, 300, pExpectedPercent, new TestHelper.Measurement() {
            @Override
            public Object getActualValue() throws Exception {
                return mTask.getProgress();
            }
        });
    }

    private abstract class ThreadedTaskForTesting extends ThreadedTask {

        protected void sleep() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // this is fine for testing purposes
                throw new RuntimeException(e);
            }
        }
    }

}
