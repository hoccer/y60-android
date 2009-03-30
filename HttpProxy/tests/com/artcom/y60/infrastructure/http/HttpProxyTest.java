package com.artcom.y60.infrastructure.http;
import java.net.URI;
import java.util.Arrays;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.util.Log;

import com.artcom.y60.infrastructure.BindingListener;
import com.artcom.y60.infrastructure.HTTPHelper;


public class HttpProxyTest extends ActivityUnitTestCase<HttpProxyActivity> {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "HttpProxyTest";
    
    
    
    // Instance Variables ------------------------------------------------

    private Intent mStartIntent;
    
    
    
    // Constructors ------------------------------------------------------

    public HttpProxyTest() {
        
        super(HttpProxyActivity.class);
    }


    
    // Public Instance Methods -------------------------------------------

    public void testGetInitiallyReturnsNull() throws Exception {
        
        initializeActivity();
        HttpProxyHelper helper = createHelper();
        byte[] bytes = helper.get(TestUriHelper.createUri());
        
        assertNull("uncached content should be null initially", bytes);
    }
    
    
    public void testResourceIsAsynchronouslyUpdated() throws Exception {
        
        initializeActivity();
        HttpProxyHelper helper = createHelper();
        
        Log.v(LOG_TAG, "enough waiting, let's get to work");
        
        TestListener listener = new TestListener();
        URI uri = TestUriHelper.createUri();
        helper.addResourceChangeListener(uri, listener);
        helper.get(uri);
        
        long start = System.currentTimeMillis();
        while (!listener.wasCalled() && System.currentTimeMillis()-start<2000) {
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException ix) {
                //
            }
        }
        
        Log.v(LOG_TAG, "now let's check results");
        
        assertTrue("update wasn't called", listener.wasCalled());
//        HttpProxyService.logCache();
        
        byte[] fromService = helper.fetchFromCache(uri);
        assertNotNull("content from cache was null", fromService);
        
        byte[] fromHttp    = HTTPHelper.getAsByteArray(Uri.parse(uri.toString()));
        assertTrue("content doesn't match", Arrays.equals(fromService, fromHttp));
    }
    
    

    // Protected Instance Methods ----------------------------------------

    protected void setUp() throws Exception {

        super.setUp();
        
        mStartIntent = new Intent(Intent.ACTION_MAIN);
    }

    
    protected void tearDown() throws Exception {

        super.tearDown();
    }

    
    
    // Private Instance Methods ------------------------------------------

    private HttpProxyHelper createHelper() {
        
        HttpProxyHelper helper = new HttpProxyHelper(getActivity(),
                new DummyListener());
        
        for (int i = 0; i < 200; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ix) {
                Log.v(LOG_TAG, "INTERRUPT!!1!");
            }
        }
        
        return helper;
    }
    
    
    private void initializeActivity() {
        
        startActivity(mStartIntent, null, null);
        assertNotNull(getActivity());
    }
    
    
    
    
    
    
    // Inner Classes -----------------------------------------------------

    class TestListener implements ResourceChangeListener {

        private boolean mCalled = false;
        public void onResourceChanged(URI resourceUri) {
            
            mCalled = true;
        }
        
        public boolean wasCalled() {
            
            return mCalled;
        }
    }
    
    
    class DummyListener implements BindingListener<HttpProxyHelper> {
        public void bound(HttpProxyHelper helper) {}
        public void unbound(HttpProxyHelper helper) {}
    }
}
