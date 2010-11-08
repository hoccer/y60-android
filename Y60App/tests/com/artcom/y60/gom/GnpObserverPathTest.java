package com.artcom.y60.gom;

import org.json.JSONObject;

import com.artcom.y60.Constants;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.http.HttpHelper;

public class GnpObserverPathTest extends GomActivityUnitTestCase {
    
    protected final String LOG_TAG        = "GnpObserverPathTest";
    protected final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/" + LOG_TAG;
    
    private GomObserver    mMockGomObserver;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        mMockGomObserver = new GomObserver() {
            public void onEntryCreated(String pPath, JSONObject pData) {
            }
            
            public void onEntryDeleted(String pPath, JSONObject pData) {
            }
            
            public void onEntryUpdated(String pPath, JSONObject pData) {
            }
        };
    }
    
    // tests for creation of observer nodes in gom
    public void testObserverForAttributeAppearsInGom() throws Exception {
        
        initializeActivity();
        GomProxyHelper helper = createHelper();
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_observer_for_attribute_appears_in_gom";
        String attrPath = testPath + ":" + timestamp;
        final String observerUri = Constants.Gom.URI
                + GomNotificationHelper.getObserverPathFor(attrPath);
        
        try {
            
            HttpHelper.getAsString(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");
            
        } catch (Exception ex) {
            Logger.v(LOG_TAG, "-----------", ex);
            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }
        
        GomNotificationHelper.createObserverAndNotify(attrPath, mMockGomObserver, helper);
        
        // check that the observer has arrived in gom
        TestHelper.blockUntilResourceAvailable("Observer should now be in GOM", observerUri);
        
    }
    
    public void testObserverForNodeAppearsInGom() throws Exception {
        
        initializeActivity();
        GomProxyHelper helper = createHelper();
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_observer_for_node_appears_in_gom";
        String nodePath = testPath + "/" + timestamp;
        String observerUri = Constants.Gom.URI + GomNotificationHelper.getObserverPathFor(nodePath);
        
        try {
            //String result = HttpHelper.getAsString(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");
            
        } catch (Exception ex) {
            
            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }
        
        GomNotificationHelper.createObserverAndNotify(nodePath, mMockGomObserver, helper);
        
        // check that the observer has arrived in gom
        TestHelper.blockUntilResourceAvailable("Observer should now be in GOM", observerUri);
        
    }
    
}
