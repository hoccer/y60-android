package com.artcom.y60.gom;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.AssertionFailedError;

public class GomActivityUnitTestCase extends ActivityUnitTestCase<GomProxyTestActivity> {

    private Intent mStartIntent;

    public GomActivityUnitTestCase() {
        super(GomProxyTestActivity.class);

        mStartIntent = new Intent(Intent.ACTION_MAIN);
    }

    protected GomProxyHelper createHelper() throws InterruptedException {

        GomProxyHelper helper = new GomProxyHelper(getActivity(), null);

        long requestStartTime = System.currentTimeMillis();
        while (!helper.isBound()) {
            if (System.currentTimeMillis() > requestStartTime + 2 * 1000) {
                throw new AssertionFailedError("Could not bind to gom service");
            }
            Thread.sleep(10);
        }

        return helper;
    }

    protected void initializeActivity() {

        startActivity(mStartIntent, null, null);
        assertNotNull(getActivity());
    }
}
