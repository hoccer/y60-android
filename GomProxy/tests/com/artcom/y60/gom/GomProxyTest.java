package com.artcom.y60.gom;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityUnitTestCase;

import com.artcom.y60.Logger;
import com.artcom.y60.gom.GomAttribute;
import com.artcom.y60.gom.GomProxyActivity;
import com.artcom.y60.gom.GomProxyHelper;

public class GomProxyTest extends ActivityUnitTestCase<GomProxyActivity> {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "GomProxyTest";

    // Instance Variables ------------------------------------------------

    private Intent mStartIntent;

    // Constructors ------------------------------------------------------

    public GomProxyTest() {

        super(GomProxyActivity.class);
    }

    // Public Instance Methods -------------------------------------------

    public void testGetBaseUri() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        Uri uri = helper.getBaseUri();

        assertEquals("http://t-gom.service.t-gallery.act", uri.toString());
    }

    public void testGetAttribute() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        GomAttribute attr = helper
                .getAttribute("/test/android/y60/infrastructure_gom/gom_proxy_test:attribute");
        assertEquals("nassau", attr.getValue());
    }

    // Protected Instance Methods ----------------------------------------

    protected void setUp() throws Exception {

        super.setUp();

        mStartIntent = new Intent(Intent.ACTION_MAIN);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    // Private Instance Methods ------------------------------------------

    private GomProxyHelper createHelper() {

        GomProxyHelper helper = new GomProxyHelper(getActivity(), null);

        for (int i = 0; i < 200; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ix) {
                Logger.v(LOG_TAG, "INTERRUPT!!1!");
            }
        }

        return helper;
    }

    private void initializeActivity() {

        startActivity(mStartIntent, null, null);
        assertNotNull(getActivity());
    }
}
