package com.artcom.y60.gom;

import android.net.Uri;
import android.test.AssertionFailedError;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.RpcStatus;

public class GomProxyTest extends GomActivityUnitTestCase {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "GomProxyTest";

    // Public Instance Methods -------------------------------------------

    public void testGetBaseUri() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        Uri uri = helper.getBaseUri();

        assertTrue("should contain http://", uri.toString().contains("http://"));
        assertTrue("should contain 'gom'", uri.toString().contains("gom"));
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

    public void testGetCachedAttribute() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String attrPath = "/test/android/y60/infrastructure_gom/gom_proxy_test:cached_attribute";

        RpcStatus rpcStatus = new RpcStatus();
        helper.getProxy().getCachedAttributeValue(attrPath, rpcStatus);

        assertTrue("cache should return an exception that attribute is not in cache", rpcStatus
                .getError() instanceof GomProxyException);

        helper.getProxy().saveAttribute(attrPath, "cache this value", rpcStatus);
        assertTrue("there should be no error", rpcStatus.isOk());

        String value = helper.getProxy().getCachedAttributeValue(attrPath, rpcStatus);
        assertTrue("cache should have no exception", rpcStatus.isOk());
        assertEquals("we should get our cached attribute value", "cache this value", value);
    }

    public void testErrorStatusForGetAttribute() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        long timestamp = System.currentTimeMillis();
        String path = "/lalalal:" + timestamp;

        try {
            GomAttribute attr = helper.getAttribute(path);
            fail("Expected 404 Exception, but got " + attr.getValue());
        } catch (GomEntryNotFoundException e) {
            if (!e.toString().contains("404")) {
                throw new RuntimeException(e);
            }
        }

    }

    // Protected Instance Methods ----------------------------------------

    @Override
    protected void setUp() throws Exception {

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();
    }

}
