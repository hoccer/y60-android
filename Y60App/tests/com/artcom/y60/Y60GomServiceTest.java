package com.artcom.y60;

import android.content.Intent;
import android.test.ServiceTestCase;

public class Y60GomServiceTest extends ServiceTestCase<DemoY60GomService> {

    boolean mHasCallbackBeenCalled = false;

    public Y60GomServiceTest() {
        super(DemoY60GomService.class);
    }

    public void testBindingToGom() {
        startService(new Intent("com.artcom.y60.DemoY60GomService"));
        assertNotNull(getService());

        getService().blockUntilBoundToGom();

        assertTrue("service should have bounded to gom", getService().isBoundToGom());
    }

    public void testSettingACallbackForBindToGom() throws Exception {
        startService(new Intent("com.artcom.y60.DemoY60GomService"));

        getService().callOnBoundToGom(new Runnable() {
            @Override
            public void run() {
                mHasCallbackBeenCalled = true;
            }
        });

        TestHelper.blockUntilTrue("runnable shoud have been called", 2000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return mHasCallbackBeenCalled;
                    }
                });

    }

    public void testSettingACallbackForBindToGomAfterBoundToGom() throws Exception {
        startService(new Intent("com.artcom.y60.DemoY60GomService"));

        getService().blockUntilBoundToGom();

        getService().callOnBoundToGom(new Runnable() {
            @Override
            public void run() {
                mHasCallbackBeenCalled = true;
            }
        });

        assertTrue("callback should have been called", mHasCallbackBeenCalled);

    }
}
