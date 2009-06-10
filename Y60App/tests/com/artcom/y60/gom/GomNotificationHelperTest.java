package com.artcom.y60.gom;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.test.suitebuilder.annotation.Suppress;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
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

    public void testPutObserverMultipleTimes() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_put_observer_multiple_times";
        String nodePath = testPath + "/" + timestamp;
        String observerUrl = Constants.Gom.URI + GomNotificationHelper.getObserverPathFor(nodePath);

        try {

            HttpHelper.get(observerUrl);
            fail("Expected a 404 on observer " + observerUrl + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }

        GomHttpWrapper.createNode(Constants.Gom.URI + nodePath);
        HttpResponse resp = GomNotificationHelper.putObserverToGom(nodePath);
        assertTrue(resp.getStatusLine().getStatusCode() < 300);

        // check that the observer has arrived in gom
        assertNotNull("missing observer in GOM", HttpHelper.get(observerUrl));
        assertNotNull("missing node in GOM", HttpHelper.get(Constants.Gom.URI + nodePath));

        // register multiple times
        resp = GomNotificationHelper.putObserverToGom(nodePath);
        assertTrue(resp.getStatusLine().getStatusCode() < 300);

        resp = GomNotificationHelper.putObserverToGom(nodePath);
        assertTrue(resp.getStatusLine().getStatusCode() < 300);

        JSONObject json = HttpHelper.getJson(observerUrl);
        JSONObject node = json.getJSONObject(Constants.Gom.Keywords.NODE);
        JSONArray entries = node.getJSONArray(Constants.Gom.Keywords.ENTRIES);
        assertEquals("There should be one observer node", 1, entries.length());
        JSONObject innerNode = entries.getJSONObject(0);
        assertEquals("Observer node should be named like the observer id", GomNotificationHelper
                .getObserverPathFor(nodePath)
                + "/" + GomNotificationHelper.getObserverId(), innerNode
                .getString(Constants.Gom.Keywords.NODE));

    }

    @Suppress
    public void testObserverForAttributeAppearsInGom() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_register_observer";
        String attrPath = testPath + ":" + timestamp;
        String observerUri = Constants.Gom.URI + GomNotificationHelper.getObserverPathFor(attrPath);

        try {

            HttpHelper.get(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }

        GomNotificationHelper.registerObserver(attrPath, mMockGomObserver);

        // check that the observer has arrived in gom
        assertNotNull("missing observer in GOM", HttpHelper.get(observerUri));
    }

    @Suppress
    public void testObserverForNodeAppearsInGom() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_register_observer";
        String nodePath = testPath + "/" + timestamp;
        String observerUri = Constants.Gom.URI + GomNotificationHelper.getObserverPathFor(nodePath);

        try {

            String result = HttpHelper.get(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }

        GomNotificationHelper.registerObserver(nodePath, mMockGomObserver);

        // check that the observer has arrived in gom
        assertNotNull("missing observer in GOM", HttpHelper.get(observerUri));
    }

    public void testNotificationCreate() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_create";
        String attrPath = testPath + ":" + timestamp;

        GomTestObserver gomObserver = new GomTestObserver();

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

        GomTestObserver gomObserver = new GomTestObserver();

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

        GomTestObserver gomObserver = new GomTestObserver();

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

        assertTrue("Regular expression should have matched the path", Pattern.matches(regExp,
                basePath));
        assertTrue("Regular expression should have matched the path", Pattern.matches(regExp,
                basePath + "/subNode"));
        assertTrue("Regular expression should have matched the path", Pattern.matches(regExp,
                basePath + ":attribute"));
        assertFalse("Regular expression shouldnt have matched the path", Pattern.matches(regExp,
                basePath + "/subNode/subSubNode"));
        assertFalse("Regular expression shouldnt have matched the path", Pattern.matches(regExp,
                basePath + "/subNode:attribute"));

        assertFalse("Regular expression shouldnt have matched the path", Pattern.matches(regExp,
                "/baseNode/otherNode"));

        regExp = GomNotificationHelper
                .createRegularExpression("/test/android/y60/infrastructure_gom/gom_notification_helper_test/test_reg_exp_contraint_on_observer/1243951216126/A/B");
        assertFalse(Pattern
                .matches(
                        regExp,
                        "/test/android/y60/infrastructure_gom/gom_notification_helper_test/test_reg_exp_contraint_on_observer/1243951216126/A/B/X:invalid_attribute"));
        assertTrue(Pattern
                .matches(
                        regExp,
                        "/test/android/y60/infrastructure_gom/gom_notification_helper_test/test_reg_exp_contraint_on_observer/1243951216126/A/B:attribute"));
    }

    // Private Instance Methods ------------------------------------------

    private Intent createBroadcastIntent(String pUri, String pOperation, JSONObject pData) {

        Intent gnpIntent = new Intent(Y60Action.GOM_NOTIFICATION_BC);

        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH, pUri);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION, pOperation);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING, pData.toString());

        return gnpIntent;

    }

    private HashMap<String, String> getObserverNodeData(String pPath) throws Exception {

        HashMap<String, String> formData = new HashMap<String, String>();
        InetAddress myIp = NetworkHelper.getStagingIp();
        String ip = myIp.getHostAddress();
        String callbackUrl = "http://" + ip + ":" + Constants.Network.DEFAULT_PORT
                + Constants.Network.GNP_TARGET;
        formData.put("callback_url", callbackUrl);
        formData.put("accept", "application/json");
        formData.put("uri_regexp", GomNotificationHelper.createRegularExpression(pPath));

        return formData;
    }
}