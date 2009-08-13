package com.artcom.y60.gom;

import com.artcom.y60.Logger;

public class GomProxyHelperTest extends GomActivityUnitTestCase {

    private static final String LOG_TAG = "GomProxyHelperTest";

    public void testGetAttributeWithException() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        try {
            helper.getAttribute("/this/attribute/does/not:exist");
            fail("expected a 404 exception in the service");

        } catch (GomEntryNotFoundException e) {

            Logger.v(LOG_TAG, "ok, caught exception ", e);

            assertTrue("expected a 404 exception in the service", e.getMessage().contains("404"));
        }
    }

    public void testDeleteEntryWithException() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        try {
            helper.deleteEntry(null);
            fail("expected a null pointer exception in the service");

        } catch (RuntimeException e) {

            Logger.v(LOG_TAG, "ok, caught exception ", e);

            assertTrue("expected a null pointer exception in the service",
                    e.getCause() instanceof NullPointerException);
        }
    }

    public void testGetEntryWithException() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();
        try {
            helper.getEntry("/this/attribute/does/not:exist");
            fail("expected a 404 exception in the service");

        } catch (GomEntryNotFoundException e) {

            Logger.v(LOG_TAG, "ok, caught exception ", e);

            assertTrue("expected a 404 exception in the service", e.getMessage().contains("404"));
        }
    }

    public void testGetNodeWithException() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        try {
            GomNode node = helper.getNode("/this/node/does/not/exist");
            node.entries(); // lazy loading accesses the GOM here, not before
            fail("expected a 404 exception in the service");

        } catch (GomEntryNotFoundException e) {
            Logger.v(LOG_TAG, "GomEntryNotFound!!! ", e);
            assertTrue("expected a 404 exception in the service", e.getMessage().contains("404"));
        }
    }
}
