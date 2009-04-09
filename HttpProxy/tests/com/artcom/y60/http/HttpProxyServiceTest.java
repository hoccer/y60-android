package com.artcom.y60.http;

import java.net.URI;
import java.util.Arrays;

import android.content.Intent;
import android.net.Uri;
import android.test.AssertionFailedError;
import android.test.ServiceTestCase;

import com.artcom.y60.HTTPHelper;
import com.artcom.y60.http.HttpProxyService;

/**
 * Direct service testing. No binding, no inter-vm-communication with aidl.
 */
public class HttpProxyServiceTest extends ServiceTestCase<HttpProxyService> {

    // Instance Variables ------------------------------------------------

    private Intent mIntent;

    // Constructors ------------------------------------------------------

    public HttpProxyServiceTest() {

        super(HttpProxyService.class);
    }

    // Public Instance Methods -------------------------------------------

    public void testDoubleGet() throws Exception {

        startService(mIntent);

        HttpProxyService service = getService();
        assertNotNull("service must not be null", service);

        URI uri = TestUriHelper.createUri();

        byte[] initial = service.get(uri.toString());
        assertNull("content should be null initially", initial);

        // wait some time to let the service load the data
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ix) {
                // kthxbye
            }
        }

        byte[] cached = service.get(uri.toString());
        assertNotNull("content from cache was null", cached);

        byte[] fromHttp = HTTPHelper.getAsByteArray(Uri.parse(uri.toString()));
        assertTrue("content doesn't match", Arrays.equals(cached, fromHttp));
    }

    public void testGettingBigData() {
        startService(mIntent);
        HttpProxyService service = getService();
        long requestStartTime = System.currentTimeMillis();
        String resourceUri = "http://www.artcom.de/images/stories/2_pro_bmwmuseum_kinetik/bmwmuseum_kinetik_d.pdf";
        byte[] data = null;
        while (data == null) {
            data = service.get(resourceUri);
            if (System.currentTimeMillis() > requestStartTime + 4000) {
                throw new AssertionFailedError("could not retrive data from uri " + resourceUri);
            }
        }
        assertEquals(data.length, 3153527);
    }

    // Protected Instance Methods ----------------------------------------

    protected void setUp() throws Exception {

        super.setUp();
        mIntent = new Intent(getContext(), HttpProxyService.class);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }
}
