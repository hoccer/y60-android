package com.artcom.y60.http;

import java.net.URI;
import java.util.Arrays;
import java.util.logging.FileHandler;

import org.apache.http.client.methods.HttpHead;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

        Bundle initial = service.get(uri.toString());
        assertNull("content should be null initially", initial);

        // wait some time to let the service load the data
        long requestStartTime = System.currentTimeMillis();
        Bundle cached = null;
        while (cached == null) {
            cached = service.get(uri.toString());
            if (System.currentTimeMillis() > requestStartTime + 2000) {
                throw new AssertionFailedError("could not retrive data from uri " + uri);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ix) {
                // kthxbye
            }
        }

        assertNotNull("resource path from cache was null", cached.getByteArray(Cache.BYTE_ARRY_TAG));

        byte[] fromHttp = HTTPHelper.getAsByteArray(Uri.parse(uri.toString()));
        byte[] cachedArray = HttpProxyHelper.convertResourceBundleToByteArray(cached);
        assertNotNull("conversion to array returned null", cachedArray);
        assertTrue("cached data is to small", cachedArray.length > 1000);
        assertTrue("content doesn't match", Arrays.equals(cachedArray, fromHttp));
    }

    public void testGettingBigData() {
        startService(mIntent);
        HttpProxyService service = getService();
        long requestStartTime = System.currentTimeMillis();
        String resourceUri = "http://www.artcom.de/images/stories/2_pro_bmwmuseum_kinetik/bmwmuseum_kinetik_d.pdf";
        Bundle resourceDescription = null;
        while (resourceDescription == null) {
            resourceDescription = service.get(resourceUri);
            if (System.currentTimeMillis() > requestStartTime + 30 * 1000) {
                throw new AssertionFailedError("could not retrive data from uri " + resourceUri);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ix) {
                // kthxbye
            }
        }
        // assertEquals(resourceDescription.get(Cache.LOCAL_RESOURCE_PATH_TAG).hashCode(),
        // 3153527);
        assertEquals("/sdcard/HttpProxyCache/" + resourceUri.hashCode(), resourceDescription
                .get(Cache.LOCAL_RESOURCE_PATH_TAG));
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
