package com.artcom.y60.gom;

import android.content.Intent;
import android.net.Uri;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.Suppress;

import com.artcom.y60.Constants;

@Suppress
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

        boolean gotExpectedException = false;
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String path = "/lalala/das/gibts/gar/nicht/" + timestamp;
            GomReference ref = new GomReference(Uri.parse(Constants.Gom.URI), path);

            String value = getService().getAttributeValue(ref.path());
            fail("Expected a 404 exception, but got attribute value " + value);
        } catch (GomEntryNotFoundException e) {
            gotExpectedException = true;
        }

        assertTrue("we should have got an GomEntryNotFound exeption", gotExpectedException);

    }

}
