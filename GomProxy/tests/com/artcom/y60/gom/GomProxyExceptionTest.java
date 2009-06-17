package com.artcom.y60.gom;

import android.content.Intent;
import android.net.Uri;
import android.test.ServiceTestCase;

import com.artcom.y60.Constants;

public class GomProxyExceptionTest extends ServiceTestCase<GomProxyService> {

    private Intent mIntent;

    protected void setUp() throws Exception {

        super.setUp();
        mIntent = new Intent(getContext(), GomProxyService.class);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    public GomProxyExceptionTest() {
        super(GomProxyService.class);
    }

    public void testGetWithNonexistingAttribute() throws Exception {

        startService(mIntent);

        try {

            String timestamp = String.valueOf(System.currentTimeMillis());
            String path = "/lalala/das/gibts/gar/nicht/" + timestamp;
            GomReference ref = new GomReference(Uri.parse(Constants.Gom.URI), path);

            String value = getService().getAttributeValue(ref.path());
            fail("Expected a 404 exception, but got attribute value " + value);
        } catch (RuntimeException rex) {

            if (!rex.toString().contains("404")) {

                throw new RuntimeException(rex);
            }
        }

    }

}