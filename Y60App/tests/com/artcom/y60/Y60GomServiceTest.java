package com.artcom.y60;

import android.content.Intent;
import android.test.ServiceTestCase;

public class Y60GomServiceTest extends ServiceTestCase<DemoY60GomService> {

    protected static final String LOG_TAG                     = "Y60GomServiceTest";
    boolean                       mHasCallbackBeenCalled      = false;
    boolean                       mHasCallbackBeenCalledAgain = false;

    public Y60GomServiceTest() {
        super(DemoY60GomService.class);
    }

    public void testBindingToGom() {
        startService(new Intent("com.artcom.y60.DemoY60GomService"));
        assertNotNull(getService());

        bindWithProxys(getService());
        getService().blockUntilBoundToGom(2000);

        assertTrue("service should have bounded to gom", getService().isBoundToGom());
    }

    public void testSettingACallbackForBindToGom() throws Exception {
        startService(new Intent("com.artcom.y60.DemoY60GomService"));
        bindWithProxys(getService());
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
        bindWithProxys(getService());
        getService().blockUntilBoundToGom(2000);

        getService().callOnBoundToGom(new Runnable() {
            @Override
            public void run() {
                mHasCallbackBeenCalled = true;
            }
        });

        TestHelper.blockUntilTrue("Callback should have been called", 1000,
                new TestHelper.Condition() {
                    public boolean isSatisfied() throws Exception {
                        return mHasCallbackBeenCalled;
                    }
                });
    }

    public void testSettingTwoCallbacksForBindToGom() throws Exception {
        startService(new Intent("com.artcom.y60.DemoY60GomService"));

        getService().callOnBoundToGom(new Runnable() {
            @Override
            public void run() {
                mHasCallbackBeenCalled = true;
            }
        });

        getService().callOnBoundToGom(new Runnable() {
            @Override
            public void run() {
                mHasCallbackBeenCalledAgain = true;
            }
        });

        bindWithProxys(getService());

        TestHelper.blockUntilTrue("runnable shoud have been called", 4000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return mHasCallbackBeenCalled;
                    }
                });

        TestHelper.blockUntilTrue("runnable shoud have been called again", 4000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return mHasCallbackBeenCalledAgain;
                    }
                });

    }

    private void bindWithProxys(DemoY60GomService service) {
        service.bindToGom();
        service.bindToHttpProxy();
    }

}
