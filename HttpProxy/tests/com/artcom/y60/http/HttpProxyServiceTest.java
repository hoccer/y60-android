package com.artcom.y60.http;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.test.AssertionFailedError;
import android.test.ServiceTestCase;

import com.artcom.y60.IoHelper;
import com.artcom.y60.Logger;

/**
 * Direct service testing. No binding, no inter-vm-communication with aidl.
 */
public class HttpProxyServiceTest extends ServiceTestCase<HttpProxyService> {

    // Instance Variables ------------------------------------------------

    private static final String LOG_TAG = "HttpProxyServiceTest";
    private Intent              mIntent;

    // Constructors ------------------------------------------------------

    public HttpProxyServiceTest() {
        super(HttpProxyService.class);
    }

    // Protected Instance Methods ----------------------------------------

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mIntent = new Intent(getContext(), HttpProxyService.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // Public Instance Methods -------------------------------------------

    public void testMultipleGet() throws Exception {
        startService(mIntent);

        HttpProxyService service = getService();
        assertNotNull("service must not be null", service);
        Uri uri = TestUriHelper.createUri();
        service.requestResource(uri.toString());

        // wait some time to let the service load the data
        long requestStartTime = System.currentTimeMillis();
        Bundle cached = null;
        while (cached == null) {
            cached = service.fetchFromCache(uri.toString());
            if (System.currentTimeMillis() - requestStartTime > 8000) {
                throw new TimeoutException("Timeout while laoding:" + uri);
            }
            Thread.sleep(10);
        }

        assertNotNull("resource path from cache was null", cached
                .getByteArray(HttpProxyConstants.BYTE_ARRAY_TAG));

        byte[] fromHttp = HttpHelper.getAsByteArray(uri);
        byte[] cachedArray = IoHelper.convertResourceBundleToByteArray(cached);
        assertNotNull("conversion to array returned null", cachedArray);
        assertTrue("cached data is to small", cachedArray.length > 1000);
        assertTrue("content doesn't match", Arrays.equals(cachedArray, fromHttp));
    }

    public void testGettingBigData() throws Exception {
        Logger.v(LOG_TAG,
                 "start test.......................................................................");
        startService(mIntent);
        HttpProxyService service = getService();

        long requestStartTime = System.currentTimeMillis();
        String resourceUri = "http://www.artcom.de/images/stories/2_pro_bmwmuseum_kinetik/bmwmuseum_kinetik_d.pdf";
        service.requestResource(resourceUri);
        Bundle resourceDescription = null;
        while (resourceDescription == null) {
            resourceDescription = service.fetchFromCache(resourceUri);
            if (System.currentTimeMillis() > requestStartTime + 30 * 1000) {
                throw new AssertionFailedError("could not retrive data from uri " + resourceUri);
            }
            Thread.sleep(10);
        }

        assertTrue("cached content should contain uri", getService().getCachedContent()
                .containsKey(resourceUri));
        Bundle savedBundle = getService().getCachedContent().get(resourceUri);
        String resourcePathUuid = savedBundle.getString(HttpProxyConstants.LOCAL_RESOURCE_PATH_TAG);

        assertEquals(
                "resourcePath from direct response should match resource pathe saved in cached content",
                resourcePathUuid, resourceDescription
                        .get(HttpProxyConstants.LOCAL_RESOURCE_PATH_TAG));

        byte[] dataFromWeb = HttpHelper.getAsByteArray(resourceUri);
        byte[] dataFromCache = IoHelper.convertResourceBundleToByteArray(resourceDescription);
        Logger.v(LOG_TAG, "websize: ", dataFromWeb.length, " cachesize: ", dataFromCache.length);
        assertTrue("content doesn't match", Arrays.equals(dataFromCache, dataFromWeb));
    }
}