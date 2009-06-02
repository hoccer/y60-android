package com.artcom.y60.gom;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.artcom.y60.Constants;
import com.artcom.y60.HTTPHelper;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

public class GomNotificationHelperTest extends TestCase {

    // Constants ---------------------------------------------------------
    private static final String LOG_TAG = "GomNotificationHelperTest";
    private static final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/gom_notification_helper_test";

    // Instance Variables ------------------------------------------------

    private GomObserver mMockGomObserver;
    private JSONObject mJson;

    // Constructors ------------------------------------------------------

    // Public Instance Methods -------------------------------------------

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
        mJson = new JSONObject("{\"hans\":\"wurst\"}");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetObserverPathForAttribute() throws Exception {

        String observerBase = Constants.Gom.OBSERVER_BASE_PATH;

        String attrPath = "/this/is/an:attribute";
        String observerPath = observerBase + "/this/is/an/attribute";

        String result = GomNotificationHelper.getObserverPathFor(attrPath);

        Logger.v(LOG_TAG, "Expected observer path: ", observerPath);
        Logger.v(LOG_TAG, "Actual observer path: ", result);

        assertEquals("unexpected observer path", observerPath, result);
    }

    public void testGetObserverPathForNode() throws Exception {

        String observerBase = Constants.Gom.OBSERVER_BASE_PATH;

        String nodePath = "/this/is/a/node";
        String observerPath = observerBase + nodePath;

        String result = GomNotificationHelper.getObserverPathFor(nodePath);

        Logger.v(LOG_TAG, "Expected observer path: ", observerPath);
        Logger.v(LOG_TAG, "Actual observer path: ", result);

        assertEquals("unexpected observer path", observerPath, result);
    }

    public void testObserverForAttributeAppearsInGom() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_register_observer";
        String attrPath = testPath + ":" + timestamp;
        String observerUri = Constants.Gom.URI + GomNotificationHelper.getObserverPathFor(attrPath);

        try {

            HTTPHelper.get(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }

        GomNotificationHelper.registerObserver(attrPath, mMockGomObserver);

        // check that the observer has arrived in gom
        assertNotNull("missing observer in GOM", HTTPHelper.get(observerUri));
    }

    public void testObserverForNodeAppearsInGom() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_register_observer";
        String nodePath = testPath + "/" + timestamp;
        String observerUri = Constants.Gom.URI + GomNotificationHelper.getObserverPathFor(nodePath);

        try {

            String result = HTTPHelper.get(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }

        GomNotificationHelper.registerObserver(nodePath, mMockGomObserver);

        // check that the observer has arrived in gom
        assertNotNull("missing observer in GOM", HTTPHelper.get(observerUri));
    }

    public void testNotificationCreate() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_create";
        String attrPath = testPath + ":" + timestamp;

        AssertiveGomObserver gomObserver = new AssertiveGomObserver();

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserver(attrPath, gomObserver);

        Intent bcIntent = createBroadcastIntent(attrPath, "create", mJson);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateCalled();
        gomObserver.assertDeleteNotCalled();
        gomObserver.assertUpdateNotCalled();
        assertEquals("path doesn't match", attrPath, gomObserver.getPath());
        assertEquals("data doesn't match", mJson.toString(), gomObserver.getData().toString());
    }

    public void testNotificationUpdate() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_update";
        String attrPath = testPath + ":" + timestamp;

        AssertiveGomObserver gomObserver = new AssertiveGomObserver();

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserver(attrPath, gomObserver);

        Intent bcIntent = createBroadcastIntent(attrPath, "update", mJson);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateNotCalled();
        gomObserver.assertUpdateCalled();
        gomObserver.assertDeleteNotCalled();
        assertEquals("path doesn't match", attrPath, gomObserver.getPath());
        assertEquals("data doesn't match", mJson.toString(), gomObserver.getData().toString());
    }

    public void testNotificationDelete() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_delete";
        String attrPath = testPath + ":" + timestamp;

        AssertiveGomObserver gomObserver = new AssertiveGomObserver();

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserver(attrPath, gomObserver);

        Intent bcIntent = createBroadcastIntent(attrPath, "delete", mJson);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateNotCalled();
        gomObserver.assertUpdateNotCalled();
        gomObserver.assertDeleteCalled();
        assertEquals("path doesn't match", attrPath, gomObserver.getPath());
        assertEquals("data doesn't match", mJson.toString(), gomObserver.getData().toString());
    }

    public void testCreateRegularExpFromPath() {
        
        String basePath = "/baseNode/node";
        
        String regExp = GomNotificationHelper.createRegularExpression(basePath);
        
        assertTrue("Regulare exp does not match path", Pattern.matches(regExp, basePath + "/subNode"));     
    }

    // Private Instance Methods ------------------------------------------

    private Intent createBroadcastIntent(String pUri, String pOperation, JSONObject pData) {

        Intent gnpIntent = new Intent(Y60Action.GOM_NOTIFICATION_BC);

        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH, pUri);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION, pOperation);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING, pData.toString());

        return gnpIntent;

    }

    // Inner Classes -----------------------------------------------------

    class AssertiveGomObserver implements GomObserver {

        private boolean mCreateCalled = false;
        private boolean mUpdateCalled = false;
        private boolean mDeleteCalled = false;
        private JSONObject mData;
        private String mPath;

        @Override
        public void onEntryCreated(String pPath, JSONObject pData) {

            mCreateCalled = true;
            mData = pData;
            mPath = pPath;
        }

        @Override
        public void onEntryDeleted(String pPath, JSONObject pData) {

            mDeleteCalled = true;
            mData = pData;
            mPath = pPath;
        }

        @Override
        public void onEntryUpdated(String pPath, JSONObject pData) {

            mUpdateCalled = true;
            mData = pData;
            mPath = pPath;
        }

        public void assertCreateCalled() {

            assertTrue("create not called", mCreateCalled);
        }

        public void assertDeleteCalled() {

            assertTrue("delete not called", mDeleteCalled);
        }

        public void assertUpdateCalled() {

            assertTrue("update not called", mUpdateCalled);
        }

        public void assertCreateNotCalled() {

            assertTrue("create called", !mCreateCalled);
        }

        public void assertDeleteNotCalled() {

            assertTrue("delete called", !mDeleteCalled);
        }

        public void assertUpdateNotCalled() {

            assertTrue("update called", !mUpdateCalled);
        }

        public String getPath() {

            return mPath;
        }

        public JSONObject getData() {

            return mData;
        }
    }
}

// public void testUpdatesForNode() {
//        
// String timestamp = String.valueOf(System.currentTimeMillis());
// String testPath = TEST_BASE_PATH+"/test_register_observer";
// String nodePath = testPath+"/"+timestamp;
// String nodeUri = Constants.Gom.URI+nodePath;
//        
// try {
//            
// String result = HTTPHelper.get(nodeUri);
// fail("Expected a 404 on test node "+nodePath+", which shouldn't exist");
//            
// } catch (Exception ex) {
//            
// boolean is404 = ex.toString().contains("404");
// assertTrue("expected a 404", is404);
// }
//        
// GomNotificationHelper.registerObserver(nodePath);
//
// Map<String, String> formData = new HashMap<String, String>();
//
// StatusLine statusLine = HTTPHelper.putUrlEncoded(nodeUri, formData);
// assertEquals("expected a 200 after creating the node", 200,
// statusLine.getStatusCode());
//        
// // check it's there now - if not, an exception is thrown
// assertNotNull("unexpected response from GOM", HTTPHelper.get(nodeUri));
//        
// // todo: register observer, check for updates
// }

// public void testUpdatesForAttribute() {
//        
// String timestamp = String.valueOf(System.currentTimeMillis());
// String testPath = TEST_BASE_PATH+"/test_register_observer";
// String attrPath = testPath+":"+timestamp;
// String attrUri = Constants.Gom.URI+attrPath;
//        
// try {
//            
// String result = HTTPHelper.get(attrUri);
// fail("Expected a 404 on test node "+attrPath+", which shouldn't exist");
//            
// } catch (Exception ex) {
//            
// boolean is404 = ex.toString().contains("404");
// assertTrue("expected a 404", is404);
// }
//        
// GomNotificationHelper.registerObserver(attrPath);
//
// String attrValue = "der wert ist ganz egal";
// Map<String, String> formData = new HashMap<String, String>();
// formData.put(GomKeywords.ATTRIBUTE, attrValue);
//
// StatusLine statusLine = HTTPHelper.putUrlEncoded(attrUri, formData);
// assertEquals("expected a 200 after creating the attribute", 200,
// statusLine.getStatusCode());
//        
// // check it's there now - if not, an exception is thrown
// assertNotNull("unexpected response from GOM", HTTPHelper.get(attrUri));
//      
// // todo: register observer, check for updates
// }