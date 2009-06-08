package com.artcom.y60.gom;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.test.AssertionFailedError;

import com.artcom.y60.Constants;
import com.artcom.y60.HTTPHelper;

public class GomProxyTest extends ActivityUnitTestCase<GomProxyTestActivity> {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "GomProxyTest";

    // Instance Variables ------------------------------------------------

    private Intent mStartIntent;

    // Constructors ------------------------------------------------------

    public GomProxyTest() {

        super(GomProxyTestActivity.class);
    }

    // Public Instance Methods -------------------------------------------

    public void testGetBaseUri() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        Uri uri = helper.getBaseUri();

        assertEquals("http://t-gom.service.t-gallery.act", uri.toString());
    }

    public void testBindingAndUnbinding() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        helper.unbind();
        long requestStartTime = System.currentTimeMillis();
        while (helper.isBound()) {
            if (System.currentTimeMillis() > requestStartTime + 5 * 1000) {
                throw new AssertionFailedError("Could not unbound from service");
            }
            Thread.sleep(10);
        }

        helper.bind();
        requestStartTime = System.currentTimeMillis();
        while (!helper.isBound()) {
            if (System.currentTimeMillis() > requestStartTime + 5 * 1000) {
                throw new AssertionFailedError("Could not rebound to service");
            }
            Thread.sleep(10);
        }
    }

    public void testGetAttribute() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        HTTPHelper.putXML(Constants.Gom.URI+"/test/android/y60/infrastructure_gom/gom_proxy_test:attribute",
                "<attribute>nassau</attribute>");
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

    private GomProxyHelper createHelper() throws InterruptedException {

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

    private void initializeActivity() {

        startActivity(mStartIntent, null, null);
        assertNotNull(getActivity());
    }

}
