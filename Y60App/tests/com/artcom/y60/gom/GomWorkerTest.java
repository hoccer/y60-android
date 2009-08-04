package com.artcom.y60.gom;

import com.artcom.y60.BindingException;

public class GomWorkerTest extends GomActivityUnitTestCase {

    public static final String LOG_TAG = "GomWorkerTest";

    private boolean            mHasCodeBeenExecuted;

    @Override
    protected void setUp() throws Exception {
        mHasCodeBeenExecuted = false;
        super.setUp();
    }

    public void testExecution() throws Exception {

        initializeActivity();
        GomProxyHelper gom = createHelper();

        GomWorker worker = new GomWorker(gom) {
            @Override
            public void execute() {
                assertNotNull("gom should not be null", getGom());
                assertTrue("gom should be bound", getGom().isBound());
                mHasCodeBeenExecuted = true;
            }
        };
        assertTrue("worker should have been executed", worker.hasFinished());
        assertTrue("code should have been executed", mHasCodeBeenExecuted);
    }

    public void testProvidingNoGom() throws Exception {

        initializeActivity();
        try {
            new GomWorker(null) {
                @Override
                public void execute() {
                    mHasCodeBeenExecuted = true;
                }
            };
            fail("we should get a null pointer exeption");
        } catch (NullPointerException e) {
            // everything is fine
        }

        assertFalse("code should not have been executed", mHasCodeBeenExecuted);
    }

    public void testProvidingAnUnboundedGom() throws Exception {

        initializeActivity();
        GomProxyHelper gom = createHelper();
        gom.unbind();
        try {
            new GomWorker(gom) {
                @Override
                public void execute() {
                    mHasCodeBeenExecuted = true;
                }
            };
            fail("we should get a binding exeption");
        } catch (BindingException e) {
            // everything is fine
        }

        assertFalse("code should not have been executed", mHasCodeBeenExecuted);
    }

    public void testUnexpectedUnbindingWhileWorking() throws Exception {

        initializeActivity();
        GomProxyHelper gom = createHelper();
        GomWorker worker = new GomWorker(gom) {
            @Override
            public void execute() {
                getGom().unbind();
                mHasCodeBeenExecuted = true;
                try {
                    getGom().getNode("/");
                } catch (GomEntryTypeMismatchException e) {
                    fail();
                }
                fail("BindingException should have been thrown and caught silently");
            }
        };

        assertTrue("worker should have been executed", worker.hasFinished());
        assertTrue("unbinding should have worked", mHasCodeBeenExecuted);
    }
}
