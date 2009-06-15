package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.StatusLine;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.RpcStatus;
import com.artcom.y60.TestHelper;
import com.artcom.y60.Y60Action;
import com.artcom.y60.gom.GomTestObserver.Event;

public class GomNotificationHelperTest extends GomActivityUnitTestCase {

    // Constants ---------------------------------------------------------
    private static final String LOG_TAG        = "GomNotificationHelperTest";
    private static final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/gom_notification_helper_test";

    // Instance Variables ------------------------------------------------

    private GomObserver         mMockGomObserver;
    private JSONObject          mJson;

    // Public Instance Methods -------------------------------------------

    protected void tearDown() throws Exception {

        super.tearDown();
    }

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

    // The 5 different cases of callbacks during registerObserver()

    // 1. entry neither in proxy nor in gom
    // -> exception | entry deleted callback

    public void testAttrNotInProxyNotInGom() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_attr_not_in_proxy_not_in_gom";
        String attrPath = testPath + ":" + timestamp;

        final GomTestObserver gto = new GomTestObserver();

        GomNotificationHelper.registerObserverAndNotify(attrPath, gto, helper);

        TestHelper.blockUntilTrue("delete not called", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return gto.getDeleteCount() == 1;
            }

        });

        gto.assertDeleteCalled();

        // Unfortunately we need to make sure that the callback is not called
        // another time
        Thread.sleep(2500);
        assertEquals("Delete is called another time", 1, gto.getDeleteCount());
        gto.assertUpdateNotCalled();
        gto.assertCreateNotCalled();
    }

    // 2. entry in proxy but not in gom
    // -> exception |entry deleted callback, delete value from proxy

    public void testAttrInProxyNotInGom() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_attr_in_proxy_not_in_gom";
        String attrPath = testPath + ":" + timestamp;

        final GomTestObserver gto = new GomTestObserver();

        String value = "huhu";
        helper.saveAttribute(attrPath, value);

        GomNotificationHelper.registerObserverAndNotify(attrPath, gto, helper);

        TestHelper.blockUntilTrue("delete not called", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return gto.getDeleteCount() == 1;
            }

        });

        gto.assertDeleteCalled();

        // Unfortunately we need to make sure that the callback is not called
        // another time
        Thread.sleep(2500);
        assertEquals("Delete is called another time", 1, gto.getDeleteCount());
        gto.assertUpdateNotCalled();
        gto.assertCreateNotCalled();

        assertFalse("Value should now be deleted in proxy", helper.hasInCache(attrPath));

    }

    public void testNodeInProxyNotInGom() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_node_in_proxy_not_in_gom";
        String nodePath = testPath + "/" + timestamp;

        final GomTestObserver gto = new GomTestObserver();

        helper.saveNode(nodePath, new LinkedList<String>(), new LinkedList<String>());
        assertNotNull(helper.getNode(nodePath));

        GomNotificationHelper.registerObserverAndNotify(nodePath, gto, helper);

        TestHelper.blockUntilTrue("delete not called", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return gto.getDeleteCount() == 1;
            }

        });

        gto.assertDeleteCalled();

        // Unfortunately we need to make sure that the callback is not called
        // another time
        Thread.sleep(2500);
        assertEquals("Delete is called another time", 1, gto.getDeleteCount());
        gto.assertUpdateNotCalled();
        gto.assertCreateNotCalled();

        assertFalse("Node should now be deleted in proxy", helper.hasInCache(nodePath));
    }

    // 3. entry in gom but not in proxy
    // -> one entry updated callback, value is in proxy

    public void testAttrInGomNotInProxy() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String attrPath = TEST_BASE_PATH + "/test_attr_in_gom_not_in_proxy" + ":" + timestamp;
        Uri attrUrl = Uri.parse(Constants.Gom.URI + attrPath);
        String attrValue = "value_im_gom";

        GomHttpWrapper.updateOrCreateAttribute(attrUrl, attrValue);

        final GomTestObserver gto = new GomTestObserver();
        GomNotificationHelper.registerObserverAndNotify(attrPath, gto, helper);

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return gto.getUpdateCount() == 1;
            }

        });

        List<Event> events = gto.getEvents();
        assertTrue(events.get(0).data.toString().contains(attrValue));

        // Unfortunately we need to make sure that the callback is not called
        // another time
        Thread.sleep(2500);
        assertEquals("Update should not be called another time", 1, gto.getUpdateCount());
        gto.assertCreateNotCalled();
        gto.assertDeleteNotCalled();

        assertTrue("Value should now be in proxy", helper.hasInCache(attrPath));
    }

    // 4. entry is new in gom and old in proxy
    // -> two entry updated callbacks, old first, new second, new value is
    // in proxy

    public void testOldAttrInProxyNewInGom() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String attrPath = TEST_BASE_PATH + "/test_old_attr_in_proxy_new_in_gom" + ":" + timestamp;
        Uri attrUrl = Uri.parse(Constants.Gom.URI + attrPath);
        String oldAttrValue = "alt_im_proxy";
        String newAttrValue = "neu_im_gom";

        helper.saveAttribute(attrPath, oldAttrValue);
        assertTrue("Attribute should now be in proxy before register Observer", helper
                .hasInCache(attrPath));

        GomHttpWrapper.updateOrCreateAttribute(attrUrl, newAttrValue);

        final GomTestObserver gto = new GomTestObserver();
        GomNotificationHelper.registerObserverAndNotify(attrPath, gto, helper);

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return gto.getUpdateCount() == 2;
            }

        });

        List<Event> events = gto.getEvents();
        assertTrue("first callback should return with old value", events.get(0).data.toString()
                .contains(oldAttrValue));
        assertTrue("second callback should return with new value", events.get(1).data.toString()
                .contains(newAttrValue));

        Thread.sleep(2500);
        gto.assertCreateNotCalled();
        gto.assertDeleteNotCalled();

        assertTrue("Attribute should now be in proxy", helper.hasInCache(attrPath));
        assertEquals("Attribute value should be updated", newAttrValue, helper.getEntry(attrPath)
                .forceAttributeOrException().getValue());
    }

    // 5. same entry is in gom and proxy
    // -> one callback. done.

    public void testSameAttrInGomAndProxy() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String attrPath = TEST_BASE_PATH + "/test_same_attr_in_gom_and_proxy" + ":" + timestamp;
        Uri attrUrl = Uri.parse(Constants.Gom.URI + attrPath);
        String attrValue = "hans-peter";

        helper.saveAttribute(attrPath, attrValue);

        assertTrue("Attribute should now be in proxy before register Observer", helper
                .hasInCache(attrPath));

        assertEquals("attr value should be in proxy", attrValue, helper.getProxy()
                .getAttributeValue(attrPath, new RpcStatus()));
        Logger.v(LOG_TAG, "attr value from proxy, getatrrvalue()", helper.getProxy()
                .getAttributeValue(attrPath, new RpcStatus()));

        GomHttpWrapper.updateOrCreateAttribute(attrUrl, attrValue);
        assertTrue("Value should be in Gom", HttpHelper.getJson(attrUrl.toString()).toString()
                .contains(attrValue));

        final GomTestObserver gto = new GomTestObserver();
        GomNotificationHelper.registerObserverAndNotify(attrPath, gto, helper);

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return gto.getUpdateCount() == 1;
            }

        });

        List<Event> events = gto.getEvents();
        Logger.v(LOG_TAG, events.get(0).data);
        assertTrue("callback should return with value", events.get(0).data.toString().contains(
                attrValue));

        Thread.sleep(2500);
        gto.assertCreateNotCalled();
        gto.assertDeleteNotCalled();
        assertEquals("Update should be called only once", 1, gto.getUpdateCount());
    }

    // receiver tests for broadcast receiver
    public void testNotificationCreate() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_create";
        String attrPath = testPath + ":" + timestamp;

        GomTestObserver gomObserver = new GomTestObserver();

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserverAndNotify(attrPath, gomObserver, helper);

        Intent bcIntent = createBroadcastIntent(attrPath, "create", mJson);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateCalled();
        gomObserver.assertDeleteNotCalled();
        gomObserver.assertUpdateNotCalled();
        assertEquals("path doesn't match", attrPath, gomObserver.getPath());
        assertEquals("data doesn't match", mJson.toString(), gomObserver.getData().toString());
    }

    public void testNotificationDelete() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_delete";
        String attrPath = testPath + ":" + timestamp;

        GomTestObserver gomObserver = new GomTestObserver();

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserverAndNotify(attrPath, gomObserver, helper);

        Intent bcIntent = createBroadcastIntent(attrPath, "delete", mJson);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateNotCalled();
        gomObserver.assertUpdateNotCalled();
        gomObserver.assertDeleteCalled();
        assertEquals("path doesn't match", attrPath, gomObserver.getPath());
        assertEquals("data doesn't match", mJson.toString(), gomObserver.getData().toString());
    }

    public void testNotificationUpdate() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_update";
        String attrPath = testPath + ":" + timestamp;

        GomTestObserver gomObserver = new GomTestObserver();

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserverAndNotify(attrPath, gomObserver, helper);

        Intent bcIntent = createBroadcastIntent(attrPath, "update", mJson);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateNotCalled();
        gomObserver.assertUpdateCalled();
        gomObserver.assertDeleteNotCalled();
        assertEquals("path doesn't match", attrPath, gomObserver.getPath());
        assertEquals("data doesn't match", mJson.toString(), gomObserver.getData().toString());
    }

    // further tests
    public void testCreateRegularExpFromPath() throws Exception {

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

    public void testRegExpConstraintOnObserver() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = TEST_BASE_PATH + "/test_reg_exp_constraint_on_observer/" + timestamp;
        String observedPath = nodePath + "/A/B";

        // create the node we want to test
        Map<String, String> formData = new HashMap<String, String>();
        String visibleNodePath = observedPath + "/X";
        String invisibleNodePath = visibleNodePath + "/Y";
        StatusLine statusLine = HttpHelper.putUrlEncoded(Constants.Gom.URI + invisibleNodePath,
                formData).getStatusLine();
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        String visibleAttrPath = observedPath + ":attribute";
        Uri visibleAttrUrl = Uri.parse(Constants.Gom.URI + visibleAttrPath);
        GomHttpWrapper.updateOrCreateAttribute(visibleAttrUrl, "who cares?");

        String invisibleAttrPath = visibleNodePath + ":invalid_attribute";
        Uri invisibleAttrUrl = Uri.parse(Constants.Gom.URI + invisibleAttrPath);
        GomHttpWrapper.updateOrCreateAttribute(invisibleAttrUrl, "who else cares?");

        String content = HttpHelper.get(invisibleAttrUrl);
        assertNotNull(content);

        String content2 = HttpHelper.get(visibleAttrUrl);
        assertNotNull(content2);

        GomTestObserver observer = new GomTestObserver();
        BroadcastReceiver receiver = GomNotificationHelper.registerObserverAndNotify(observedPath,
                observer, helper);

        getActivity().registerReceiver(receiver, Constants.Gom.NOTIFICATION_FILTER);
        TestHelper.blockUntilResourceAvailable("Observer should be in GOM", Constants.Gom.URI
                + GomNotificationHelper.getObserverPathFor(observedPath));

        GomHttpWrapper.deleteAttribute(invisibleAttrUrl);
        Thread.sleep(2500);
        observer.assertCreateNotCalled();
        observer.assertDeleteNotCalled();
        // observer.assertUpdateNotCalled();
        observer.reset();

        GomHttpWrapper.deleteAttribute(visibleAttrUrl);
        Thread.sleep(2500);
        observer.assertCreateNotCalled();
        observer.assertDeleteCalled();
        // observer.assertUpdateCalled();
        observer.reset();

        GomHttpWrapper.deleteNode(Uri.parse(Constants.Gom.URI + invisibleNodePath));
        Thread.sleep(2500);
        observer.assertCreateNotCalled();
        observer.assertDeleteNotCalled();
        // observer.assertUpdateNotCalled();
        observer.reset();

        GomHttpWrapper.deleteNode(Uri.parse(Constants.Gom.URI + visibleNodePath));
        Thread.sleep(2500);
        observer.assertCreateNotCalled();
        observer.assertDeleteCalled();
        // observer.assertUpdateCalled();
        observer.reset();

        GomHttpWrapper.deleteNode(Uri.parse(Constants.Gom.URI + observedPath));
        Thread.sleep(2500);
        observer.assertCreateNotCalled();
        observer.assertDeleteCalled();
        // observer.assertUpdateCalled();
        observer.reset();

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

    public void testGetObserverId() {

        assertEquals("._device_bla_23", GomNotificationHelper.encodeObserverId("/device/bla/23"));
        assertEquals(".device_bla_23", GomNotificationHelper.encodeObserverId("device/bla/23"));
        assertEquals(".devicebla23", GomNotificationHelper.encodeObserverId("devicebla23"));

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

            HttpHelper.get(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }

        GomNotificationHelper.registerObserverAndNotify(attrPath, mMockGomObserver, helper);

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

            String result = HttpHelper.get(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }

        GomNotificationHelper.registerObserverAndNotify(nodePath, mMockGomObserver, helper);

        // check that the observer has arrived in gom
        TestHelper.blockUntilResourceAvailable("Observer should now be in GOM", observerUri);

    }

    public void testRegisterObserverMultipleTimes() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_register_observer_multiple_times";
        String nodePath = testPath + "/" + timestamp;

        final GomTestObserver observer = new GomTestObserver();

        // create node in gom
        GomHttpWrapper.createNode(Constants.Gom.URI + nodePath);
        assertNotNull("missing node in GOM", HttpHelper.get(Constants.Gom.URI + nodePath));

        BroadcastReceiver receiver = GomNotificationHelper.registerObserverAndNotify(nodePath,
                observer, helper);
        getActivity().registerReceiver(receiver, Constants.Gom.NOTIFICATION_FILTER);

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 1;
            }

        });

        assertEquals("update should be called once after getting node from gom and not from proxy",
                1, observer.getUpdateCount());

        // Thread.sleep(2500);

        // create attribute in gom
        String visibleAttrPath = nodePath + ":attribute";
        Uri visibleAttrUrl = Uri.parse(Constants.Gom.URI + visibleAttrPath);
        GomHttpWrapper.updateOrCreateAttribute(visibleAttrUrl, "who cares?");

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getCreateCount() == 1;
            }

        });

        Thread.sleep(2500);
        observer.assertDeleteNotCalled();
        assertEquals("create should be called only once", 1, observer.getCreateCount());
        assertEquals("update should be called only once", 1, observer.getUpdateCount());

        // register multiple times
        GomNotificationHelper.registerObserverAndNotify(nodePath, observer, helper);
        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 2;
            }

        });

        GomNotificationHelper.registerObserverAndNotify(nodePath, observer, helper);
        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 3;
            }

        });

        Thread.sleep(2500);
        GomHttpWrapper.updateOrCreateAttribute(visibleAttrUrl, "who cares ?");

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 4;
            }

        });

        Thread.sleep(2500);
        observer.assertDeleteNotCalled();
        assertEquals("create should be called only once", 1, observer.getCreateCount());
        assertEquals("update should be called only once", 4, observer.getUpdateCount());

    }

    // helper methods
    private Intent createBroadcastIntent(String pUri, String pOperation, JSONObject pData) {

        Intent gnpIntent = new Intent(Y60Action.GOM_NOTIFICATION_BC);

        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH, pUri);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION, pOperation);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING, pData.toString());

        return gnpIntent;

    }

}