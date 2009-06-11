package com.artcom.y60.gom;

import android.net.Uri;
import android.test.AssertionFailedError;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;

public class GomProxyTest extends GomActivityUnitTestCase {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "GomProxyTest";

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
        HttpHelper.putXML(Constants.Gom.URI
                + "/test/android/y60/infrastructure_gom/gom_proxy_test:attribute",
                "<attribute>nassau</attribute>");
        GomAttribute attr = helper
                .getAttribute("/test/android/y60/infrastructure_gom/gom_proxy_test:attribute");
        assertEquals("nassau", attr.getValue());
    }

    // Protected Instance Methods ----------------------------------------

    protected void setUp() throws Exception {

        super.setUp();
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

}
