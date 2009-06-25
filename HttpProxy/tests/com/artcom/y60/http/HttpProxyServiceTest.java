package com.artcom.y60.http;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.test.AssertionFailedError;
import android.test.ServiceTestCase;

import com.artcom.y60.HttpHelper;
import com.artcom.y60.ResourceBundleHelper;

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

    // Protected Instance Methods ----------------------------------------

    protected void setUp() throws Exception {

        super.setUp();
        mIntent = new Intent(getContext(), HttpProxyService.class);

        File dir = new File(Cache.CACHE_DIR);
        dir.delete();
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

    // Public Instance Methods -------------------------------------------

    public void testMultipleGet() throws Exception {

        startService(mIntent);

        HttpProxyService service = getService();
        assertNotNull("service must not be null", service);

        Uri uri = TestUriHelper.createUri();

        Bundle initial = service.get(uri.toString());
        assertNull("content should be null initially", initial);

        // wait some time to let the service load the data
        long requestStartTime = System.currentTimeMillis();
        Bundle cached = null;
        while (cached == null) {
            cached = service.get(uri.toString());
            if (System.currentTimeMillis() - requestStartTime > 4000) {
                throw new TimeoutException("Timeout while laoding:" + uri);
            }
            Thread.sleep(10);
        }

        assertNotNull("resource path from cache was null", cached
                .getByteArray(HttpProxyConstants.BYTE_ARRAY_TAG));

        byte[] fromHttp = HttpHelper.getAsByteArray(Uri.parse(uri.toString()));
        byte[] cachedArray = ResourceBundleHelper.convertResourceBundleToByteArray(cached);
        assertNotNull("conversion to array returned null", cachedArray);
        assertTrue("cached data is to small", cachedArray.length > 1000);
        assertTrue("content doesn't match", Arrays.equals(cachedArray, fromHttp));
    }

    public void testGettingBigData() throws InterruptedException {
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
            Thread.sleep(10);
        }

        assertEquals("/sdcard/HttpProxyCache/bmwmuseum_kinetik_d.pdf", resourceDescription
                .get(HttpProxyConstants.LOCAL_RESOURCE_PATH_TAG));
    }

}
