package com.artcom.y60.http;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.Suppress;

import com.artcom.y60.BindingListener;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;

/**
 * Blackbox service testing through HttpProxyHelper (aidl and inter-vm-communication).
 */
public class HttpProxyTest extends ActivityUnitTestCase<HttpProxyTestActivity> {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "HttpProxyTest";

    // Instance Variables ------------------------------------------------

    private Intent             mStartIntent;

    // Constructors ------------------------------------------------------

    public HttpProxyTest() {

        super(HttpProxyTestActivity.class);
    }

    // Public Instance Methods -------------------------------------------

    public void testGetInitiallyReturnsNull() throws Exception {

        initializeActivity();
        HttpProxyHelper helper = createHelper();
        byte[] bytes = helper.get(TestUriHelper.createUri());

        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++)
                System.out.println(i + ": " + bytes[i]);
        }
        assertNull("uncached content should be null initially", bytes);
    }

    public void testResourceIsAsynchronouslyUpdated() throws Exception {

        initializeActivity();
        final HttpProxyHelper helper = createHelper();

        Logger.v(LOG_TAG, "enough waiting, let's get to work");

        final TestListener listener = new TestListener();
        final Uri uri = TestUriHelper.createUri();
        helper.addResourceChangeListener(uri, listener);
        helper.requestDownload(uri);

        TestHelper.blockUntilTrue("proxy should return the object", 4000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() {
                        return (helper.get(uri) != null);
                    }

                });

        TestHelper.blockUntilTrue("proxy call the listener", 1000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return listener.wasResourceAvailableCalled();
            }

        });

        Logger.v(LOG_TAG, "now let's check results");

        assertTrue("update should have been called", listener.wasResourceAvailableCalled());
        assertNotNull("get should return an object", helper.get(uri));
        // HttpProxyService.logCache();

        byte[] fromService = helper.fetchFromCache(uri);
        assertNotNull("content from cache was null", fromService);

        byte[] fromHttp = HttpHelper.getAsByteArray(Uri.parse(uri.toString()));
        assertTrue("content doesn't match", Arrays.equals(fromService, fromHttp));
    }

    public void testRemovingResourceFromCache() throws Exception {

        initializeActivity();
        HttpProxyHelper helper = createHelper();
        Uri uri = TestUriHelper.createUri();

        TestListener listener = new TestListener();
        helper.addResourceChangeListener(uri, listener);

        byte[] data = helper.get(uri);
        assertNull("uncached content should be null initially", data);

        long start = System.currentTimeMillis();
        while (!listener.wasResourceAvailableCalled()) {

            if (System.currentTimeMillis() - start > 2000) {
                throw new TimeoutException("took to long");
            }

            Thread.sleep(50);
        }

        assertTrue("update wasn't called", listener.wasResourceAvailableCalled());
        data = helper.fetchFromCache(uri);
        assertNotNull("content from cache was null", data);

        helper.removeFromCache(uri.toString());
        data = helper.get(uri);
        assertNull("content should be null after removing form cache", data);

    }

    public void testRequestingDownload() throws Exception {

        initializeActivity();
        final HttpProxyHelper helper = createHelper();
        final Uri uri = TestUriHelper.createUri();

        TestListener listener = new TestListener();
        helper.addResourceChangeListener(uri, listener);
        assertFalse(helper.isInCache(uri));

        helper.requestDownload(uri);
        assertFalse("the uri should not be in cache yet", helper.isInCache(uri));

        TestHelper.blockUntilTrue("cache should load uri into cache", 2000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return helper.isInCache(uri);
                    }
                });
    }

    public void testResourceNotAvailableInCache() throws Exception {
        initializeActivity();
        HttpProxyHelper helper = createHelper();
        TestListener listener = new TestListener();
        Uri uri = TestUriHelper.createUri();
        helper.addResourceChangeListener(uri, listener);

        byte[] data = helper.get(uri);
        assertNull(data);

        blockUntilResourceAvailableWasCalled(listener, 8000);

        assertTrue("callback not succsessful", listener.wasResourceAvailableCalled());
        data = helper.get(uri);
        assertNotNull(data);
    }

    public void testGettingNonexistentResource() throws Exception {
        initializeActivity();
        HttpProxyHelper helper = createHelper();
        final TestListener listener = new TestListener();
        Uri uri = Uri.parse("hxxp://x");
        helper.addResourceChangeListener(uri, listener);
        helper.requestDownload(uri);

        TestHelper.blockUntilTrue("not available callback should have been called", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() throws Exception {
                        return listener.wasResourceNotAvailableCalled();
                    }
                });
    }

    public void testResourceIsAvailableInCache() throws Exception {
        initializeActivity();
        HttpProxyHelper helper = createHelper();

        TestListener listener = new TestListener();
        Uri uri = TestUriHelper.createUri();
        helper.addResourceChangeListener(uri, listener);

        byte[] data = helper.get(uri);
        assertNull(data);

        blockUntilResourceAvailableWasCalled(listener, 4000);

        listener.reset();
        assertFalse(listener.wasResourceAvailableCalled());
        helper.addResourceChangeListener(uri, listener);

        // this is minimal asynchronous
        blockUntilResourceAvailableWasCalled(listener, 200);
        assertTrue("callback not succsessful", listener.wasResourceAvailableCalled());
        data = helper.get(uri);
        assertNotNull(data);
    }

    // the next four test can only be tested if the constructor lines that
    // create HashMap and LinkedList in Cache.java are commented out
    @Suppress
    public void testGetException() throws Exception {
        initializeActivity();
        HttpProxyHelper helper = createHelper();
        helper.get(Uri.parse("http://bla"));
    }

    @Suppress
    public void testRemoveException() throws Exception {
        initializeActivity();
        HttpProxyHelper helper = createHelper();
        helper.removeFromCache(Uri.parse("http://bla"));

    }

    @Suppress
    public void testFetchInCacheException() throws Exception {
        initializeActivity();
        HttpProxyHelper helper = createHelper();
        helper.fetchFromCache(Uri.parse("http://bla"));

    }

    @Suppress
    public void testIsInCacheException() throws Exception {
        initializeActivity();
        HttpProxyHelper helper = createHelper();
        helper.isInCache(Uri.parse("http://bla"));

    }

    // Protected Instance Methods ----------------------------------------

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        mStartIntent = new Intent(Intent.ACTION_MAIN);
    }

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();
    }

    // Private Instance Methods ------------------------------------------

    private void blockUntilResourceAvailableWasCalled(TestListener pListener, long pTimeout)
            throws TimeoutException, InterruptedException {
        long start = System.currentTimeMillis();
        while (!pListener.wasResourceAvailableCalled()) {
            if (System.currentTimeMillis() - start > pTimeout) {
                throw new TimeoutException("took to long");
            }
            Thread.sleep(50);
        }
    }

    private HttpProxyHelper createHelper() throws Exception {

        final DummyListener lsner = new DummyListener();
        HttpProxyHelper helper = new HttpProxyHelper(getActivity(), lsner);

        TestHelper.blockUntilTrue("HTTP helper not bound", 5000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return lsner.isBound();
            }

        });

        return helper;
    }

    private void initializeActivity() {

        startActivity(mStartIntent, null, null);
        assertNotNull(getActivity());
    }

    // Inner Classes -----------------------------------------------------

    class TestListener implements ResourceListener {

        private boolean mWasResourceAvailableCalled    = false;
        private boolean mWasResourceNotAvailableCalled = false;

        @Override
        public void onResourceAvailable(Uri pResourceUri) {
            mWasResourceAvailableCalled = true;
        }

        public boolean wasResourceAvailableCalled() {
            return mWasResourceAvailableCalled;
        }

        public boolean wasResourceNotAvailableCalled() {
            return mWasResourceNotAvailableCalled;
        }

        public void reset() {
            mWasResourceNotAvailableCalled = false;
            mWasResourceAvailableCalled = false;
        }

        @Override
        public void onResourceNotAvailable(Uri pResourceUri) {
            mWasResourceNotAvailableCalled = true;
        }
    }

    class DummyListener implements BindingListener<HttpProxyHelper> {
        private boolean mBound = false;

        public void bound(HttpProxyHelper helper) {
            mBound = true;
        }

        public void unbound(HttpProxyHelper helper) {
            mBound = false;
        }

        public boolean isBound() {
            return mBound;
        }
    }
}
