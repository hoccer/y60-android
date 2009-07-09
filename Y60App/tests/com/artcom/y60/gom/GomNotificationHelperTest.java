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
import android.test.suitebuilder.annotation.Suppress;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.UriHelper;
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

    @Override
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

        final GomTestObserver gto = new GomTestObserver(this);

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
    // -> 1. update callback, 2. exception |entry deleted callback, delete value
    // from proxy

    public void testAttrInProxyNotInGom() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_attr_in_proxy_not_in_gom";
        String attrPath = testPath + ":" + timestamp;

        final GomTestObserver gto = new GomTestObserver(this);

        String value = "huhu";
        helper.saveAttribute(attrPath, value);

        GomNotificationHelper.registerObserverAndNotify(attrPath, gto, helper);

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return gto.getUpdateCount() == 1;
            }

        });

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
        assertEquals("Update is called another time", 1, gto.getUpdateCount());
        gto.assertCreateNotCalled();

        assertFalse("Value should now be deleted in proxy", helper.hasInCache(attrPath));

    }

    public void testNodeInProxyNotInGom() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_node_in_proxy_not_in_gom";
        String nodePath = testPath + "/" + timestamp;

        final GomTestObserver gto = new GomTestObserver(this);

        helper.saveNode(nodePath, new LinkedList<String>(), new LinkedList<String>());
        assertNotNull(helper.getNode(nodePath));

        GomNotificationHelper.registerObserverAndNotify(nodePath, gto, helper);

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return gto.getUpdateCount() == 1;
            }

        });

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
        assertEquals("Update is called another time", 1, gto.getUpdateCount());
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

        final GomTestObserver gto = new GomTestObserver(this);
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

        final GomTestObserver gto = new GomTestObserver(this);
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

        assertEquals("attr value should be in proxy", attrValue, helper.getAttribute(attrPath)
                .getValue());
        Logger.v(LOG_TAG, "attr value from proxy, getatrrvalue()", helper.getAttribute(attrPath)
                .getValue());

        GomHttpWrapper.updateOrCreateAttribute(attrUrl, attrValue);
        assertTrue("Value should be in Gom", HttpHelper.getJson(attrUrl.toString()).toString()
                .contains(attrValue));

        final GomTestObserver gto = new GomTestObserver(this);
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

    // receiver tests for broadcast receiver, offline
    public void testNotificationCreate() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_create";
        String attrPath = testPath + ":" + timestamp;

        GomTestObserver gomObserver = new GomTestObserver(this);

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

        GomTestObserver gomObserver = new GomTestObserver(this);

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

        GomTestObserver gomObserver = new GomTestObserver(this);

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

        TestHelper.blockUntilWebServerIsRunning();

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

        String content = HttpHelper.getAsString(invisibleAttrUrl);
        assertNotNull(content);

        String content2 = HttpHelper.getAsString(visibleAttrUrl);
        assertNotNull(content2);

        GomTestObserver observer = new GomTestObserver(this);
        BroadcastReceiver receiver = GomNotificationHelper.registerObserverAndNotify(observedPath,
                observer, helper);

        getActivity().registerReceiver(receiver, Constants.Gom.GNP_INTENT_FILTER);
        TestHelper.blockUntilResourceAvailable("Observer should be in GOM", GomNotificationHelper
                .getObserverUriFor(observedPath));

        GomHttpWrapper.deleteAttribute(invisibleAttrUrl);
        Thread.sleep(4000);
        observer.assertCreateNotCalled();
        observer.assertDeleteNotCalled();
        // observer.assertUpdateNotCalled();
        observer.reset();

        GomHttpWrapper.deleteAttribute(visibleAttrUrl);
        Thread.sleep(4000);
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
        Thread.sleep(4000);
        observer.assertCreateNotCalled();
        observer.assertDeleteCalled();
        // observer.assertUpdateCalled();
        observer.reset();

        GomHttpWrapper.deleteNode(Uri.parse(Constants.Gom.URI + observedPath));
        Thread.sleep(4000);
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

            HttpHelper.getAsString(observerUri);
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

            String result = HttpHelper.getAsString(observerUri);
            fail("Expected a 404 on observer " + observerUri + ", which shouldn't exist");

        } catch (Exception ex) {

            boolean is404 = ex.toString().contains("404");
            assertTrue("expected a 404", is404);
        }

        GomNotificationHelper.registerObserverAndNotify(nodePath, mMockGomObserver, helper);

        // check that the observer has arrived in gom
        TestHelper.blockUntilResourceAvailable("Observer should now be in GOM", observerUri);

    }

    // initial state: value in gom, NOT in proxy
    public void testRegisterObserverMultipleTimes() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_register_observer_multiple_times";
        String nodePath = testPath + "/" + timestamp;

        final GomTestObserver observer = new GomTestObserver(this);

        // create node in gom
        GomHttpWrapper.createNode(Constants.Gom.URI + nodePath);
        assertNotNull("missing node in GOM", HttpHelper.getAsString(Constants.Gom.URI + nodePath));

        BroadcastReceiver receiver = GomNotificationHelper.registerObserverAndNotify(nodePath,
                observer, helper);
        getActivity().registerReceiver(receiver, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("update not called", 4000, new TestHelper.Condition() {
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

        TestHelper.blockUntilTrue("create not called", 4000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getCreateCount() == 1;
            }

        });

        Thread.sleep(4000);
        observer.assertDeleteNotCalled();
        assertEquals("create should be called only once", 1, observer.getCreateCount());
        assertEquals("update should be called only once", 1, observer.getUpdateCount());
        Thread.sleep(2500);

        // register multiple times
        // this causes two update calls, since the proxy is outdated
        GomNotificationHelper.registerObserverAndNotify(nodePath, observer, helper);
        Thread.sleep(2500);
        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 3;
            }

        });
        Thread.sleep(2500);
        assertEquals(3, observer.getUpdateCount());

        GomNotificationHelper.registerObserverAndNotify(nodePath, observer, helper);
        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 4;
            }

        });
        Thread.sleep(2500);
        assertEquals(4, observer.getUpdateCount());

        Thread.sleep(2500);
        GomHttpWrapper.updateOrCreateAttribute(visibleAttrUrl, "who else cares?");

        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 5;
            }

        });

        Thread.sleep(2500);
        observer.assertDeleteNotCalled();
        assertEquals("create should be called only once", 1, observer.getCreateCount());
        assertEquals("update should be called only once", 5, observer.getUpdateCount());

    }

    // ROUNDTRIPS

    // create attribute in gom, register gnp, get first onEntryUpdate, change
    // value, get second onEntryUpdate
    public void testSimpleGnpRoundtrip() throws Exception {

        initializeActivity();

        TestHelper.blockUntilWebServerIsRunning();

        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String attrPath = TEST_BASE_PATH + "/test_on_attribute_updated" + ":" + timestamp;

        HttpHelper.putXML(Constants.Gom.URI + attrPath, "<attribute>original value</attribute>");
        assertEquals("original value", HttpHelper
                .getAsString(Constants.Gom.URI + attrPath + ".txt"));

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver receiver = GomNotificationHelper.registerObserverAndNotify(attrPath, gto,
                helper);
        assertEquals("gnp update callback shuld not have been called", 0, gto.getUpdateCount());

        getActivity().registerReceiver(receiver, Constants.Gom.GNP_INTENT_FILTER);
        TestHelper.blockUntilTrue("gnp update callback shuld have been called once", 3000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }

                });

        assertEquals("cache should have the old value after callback was called once",
                "original value", helper.getCachedAttributeValue(attrPath));

        TestHelper.blockUntilResourceAvailable("observer should be registered in gom",
                GomNotificationHelper.getObserverUriFor(attrPath));

        HttpHelper.putXML(Constants.Gom.URI + attrPath, "<attribute>changed value</attribute>");
        assertEquals("cache should still have the old value", "original value", helper
                .getCachedAttributeValue(attrPath));

        assertEquals("the value should have changed in gom", "changed value", HttpHelper
                .getAsString(Constants.Gom.URI + attrPath + ".txt"));

        TestHelper.blockUntilTrue("GNP should notify our observer about the changed value", 2000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 2;
                    }

                });

        Logger.v(LOG_TAG, "data in cache: " + gto.getData());
        JSONObject jsonData = gto.getData();
        assertTrue("json has attribute", jsonData.has("attribute"));
        assertTrue("json has attribute.value", jsonData.getJSONObject("attribute").has("value"));
        assertEquals("value should have changed", "changed value", gto.getData().getJSONObject(
                "attribute").get("value"));

    }

    @Suppress
    public void testRegisterOnNodeGetOnAttributeUpdated() throws Exception {

        initializeActivity();

        TestHelper.blockUntilWebServerIsRunning();

        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = TEST_BASE_PATH + "/test_register_on_node_get_on_attribute_updated" + "/"
                + timestamp;
        String attrPath = nodePath + ":attribute";

        HttpHelper.putXML(Constants.Gom.URI + attrPath, "<attribute>original value</attribute>");
        assertEquals("original value", HttpHelper
                .getAsString(Constants.Gom.URI + attrPath + ".txt"));

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver receiver = GomNotificationHelper.registerObserverAndNotify(nodePath, gto,
                helper);
        assertEquals("gnp update callback should not have been called", 0, gto.getUpdateCount());
        assertEquals("gnp create callback should not have been called", 0, gto.getCreateCount());
        assertEquals("gnp delete callback should not have been called", 0, gto.getDeleteCount());

        getActivity().registerReceiver(receiver, Constants.Gom.GNP_INTENT_FILTER);
        TestHelper.blockUntilTrue("gnp update callback should have been called once", 3000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }

                });

        assertEquals("gnp update callback should have been called once", 1, gto.getUpdateCount());
        assertEquals("gnp create callback should not have been called", 0, gto.getCreateCount());
        assertEquals("gnp delete callback should not have been called", 0, gto.getDeleteCount());

        assertEquals("cache should have the old value after callback was called once",
                "original value", helper.getCachedAttributeValue(attrPath));

        TestHelper.blockUntilResourceAvailable("observer node should be registered in gom",
                GomNotificationHelper.getObserverUriFor(nodePath));

        HttpHelper.putXML(Constants.Gom.URI + attrPath, "<attribute>changed value</attribute>");
        assertEquals("cache should still have the old value", "original value", helper
                .getCachedAttributeValue(attrPath));

        assertEquals("the value should have changed in gom", "changed value", HttpHelper
                .getAsString(Constants.Gom.URI + attrPath + ".txt"));

        TestHelper.blockUntilTrue("GNP (update attribute notification) "
                + "should notify our observer about the changed value", 2000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 2;
                    }

                });

        Thread.sleep(3000);

        assertEquals("gnp update callback should have been called once", 2, gto.getUpdateCount());
        assertEquals("gnp create callback should not have been called", 0, gto.getCreateCount());
        assertEquals("gnp delete callback should not have been called", 0, gto.getDeleteCount());

        Logger.v(LOG_TAG, "data in cache: " + gto.getData());
        JSONObject jsonData = gto.getData();
        assertTrue("json has attribute", jsonData.has("attribute"));
        assertTrue("json has attribute.value", jsonData.getJSONObject("attribute").has("value"));
        assertEquals("value should have changed", "changed value", gto.getData().getJSONObject(
                "attribute").get("value"));

        assertEquals("the value should have changed in proxy", "changed value", helper
                .getAttribute(attrPath).getValue());

    }

    @Suppress
    public void testRegisterOnNodeGetOnNodeCreated() throws Exception {

        initializeActivity();

        TestHelper.blockUntilWebServerIsRunning();

        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = TEST_BASE_PATH + "/test_register_on_node_get_on_node_created" + "/"
                + timestamp;
        String subNodePath = nodePath + "/subNode";

        HttpHelper.putXML(Constants.Gom.URI + nodePath, "<>");
        TestHelper.blockUntilResourceAvailable("observer node should be registered in gom",
                nodePath);

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver receiver = GomNotificationHelper.registerObserverAndNotify(nodePath, gto,
                helper);
        assertEquals("gnp update callback should not have been called", 0, gto.getUpdateCount());
        assertEquals("gnp create callback should not have been called", 0, gto.getCreateCount());
        assertEquals("gnp delete callback should not have been called", 0, gto.getDeleteCount());

        getActivity().registerReceiver(receiver, Constants.Gom.GNP_INTENT_FILTER);
        TestHelper.blockUntilTrue("gnp update callback should have been called once", 3000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }

                });

        assertEquals("gnp update callback should have been called once", 1, gto.getUpdateCount());
        assertEquals("gnp create callback should not have been called", 0, gto.getCreateCount());
        assertEquals("gnp delete callback should not have been called", 0, gto.getDeleteCount());

        LinkedList<String> pSubNodeNames = new LinkedList<String>();
        helper.getCachedNodeData(nodePath, pSubNodeNames, new LinkedList<String>());

        assertEquals(
                "cache should have the node with zero subNodes after callback was called once", 0,
                pSubNodeNames.size());

        // TestHelper.blockUntilResourceAvailable("observer node should be registered in gom",
        // GomNotificationHelper.getObserverUriFor(nodePath));
        //
        // HttpHelper.putXML(Constants.Gom.URI + attrPath,
        // "<attribute>changed value</attribute>");
        // assertEquals("cache should still have the old value",
        // "original value", helper
        // .getCachedAttributeValue(attrPath));
        //
        // assertEquals("the value should have changed in gom", "changed value",
        // HttpHelper
        // .get(Constants.Gom.URI + attrPath + ".txt"));
        //
        // TestHelper.blockUntilTrue("GNP should notify our observer about the changed value",
        // 2000,
        // new TestHelper.Condition() {
        //
        // @Override
        // public boolean isSatisfied() {
        // return gto.getUpdateCount() == 2;
        // }
        //
        // });
        //
        // Thread.sleep(3000);
        //
        // assertEquals("gnp update callback should have been called once", 2,
        // gto.getUpdateCount());
        // assertEquals("gnp create callback should not have been called", 0,
        // gto.getCreateCount());
        // assertEquals("gnp delete callback should not have been called", 0,
        // gto.getDeleteCount());
        //
        // Logger.v(LOG_TAG, "data in cache: " + gto.getData());
        // JSONObject jsonData = gto.getData();
        // assertTrue("json has attribute", jsonData.has("attribute"));
        // assertTrue("json has attribute.value",
        // jsonData.getJSONObject("attribute").has("value"));
        // assertEquals("value should have changed", "changed value",
        // gto.getData().getJSONObject(
        // "attribute").get("value"));
        //
        // assertEquals("the value should have changed in proxy",
        // "changed value", helper
        // .getAttribute(attrPath).getValue());

    }

    @Suppress
    public void testDeleteAttributeOnNotification() throws Exception {

        initializeActivity();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = TEST_BASE_PATH + "/test_delete_attribute_on_notification/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        // create the node we want to test
        Map<String, String> formData = new HashMap<String, String>();
        HttpHelper.putUrlEncoded(nodeUrl, formData);

        // create attribute in the test node
        String attrName = "test_attribute";
        Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
        GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");

        String attrPath = nodePath + ":" + attrName;

        GomProxyHelper proxy = createHelper();
        BroadcastReceiver rec = GomNotificationHelper.registerObserver(nodePath, mMockGomObserver,
                proxy);
        boolean isInCache = proxy.hasInCache(attrPath);
        assertTrue("attribute is missing", isInCache);

        GomHttpWrapper.deleteAttribute(attrUri);

        // update may take a while
        Thread.sleep(3000);

        isInCache = proxy.hasInCache(attrPath);
        assertFalse("attribute '" + attrPath + "' shouldn't be there", isInCache);
    }

    // public void testDeleteNodeOnNotification() throws Exception {
    //
    // startService(mIntent);
    //
    // GomProxyService service = getService();
    // String timestamp = String.valueOf(System.currentTimeMillis());
    //
    // String nodePath = TEST_BASE_PATH + "/test_delete_node_on_notification/" +
    // timestamp;
    // String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);
    //
    // Logger.d(LOG_TAG, "node URL: ", nodeUrl);
    //
    // // create the node we want to test
    // Map<String, String> formData = new HashMap<String, String>();
    // StatusLine statusLine = HttpHelper.putUrlEncoded(nodeUrl,
    // formData).getStatusLine();
    // int statusCode = statusLine.getStatusCode();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // // make sure it's in the cache
    // service.getNodeData(nodePath, new LinkedList<String>(), new
    // LinkedList<String>());
    //
    // boolean isInCache = service.hasNodeInCache(nodePath);
    // assertTrue("node is missing", isInCache);
    //
    // GomHttpWrapper.deleteNode(Uri.parse(nodeUrl));
    //
    // // update may take a while
    // Thread.sleep(3000);
    //
    // isInCache = service.hasNodeInCache(nodePath);
    // assertFalse("node is in cache", isInCache);
    // }
    //
    // public void testRecursiveDeleteNodeOnNotification() throws Exception {
    //
    // startService(mIntent);
    //
    // GomProxyService service = getService();
    // String timestamp = String.valueOf(System.currentTimeMillis());
    //
    // String nodePath = TEST_BASE_PATH +
    // "/test_recursive_delete_node_on_notification/"
    // + timestamp;
    // String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);
    //
    // Logger.d(LOG_TAG, "node URL: ", nodeUrl);
    //
    // Map<String, String> formData = new HashMap<String, String>();
    // StatusLine statusLine = HttpHelper.putUrlEncoded(nodeUrl,
    // formData).getStatusLine();
    // int statusCode = statusLine.getStatusCode();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // // create sub node
    // String subNodeName = "subNode";
    // String subNodeUrl = UriHelper.join(nodeUrl, subNodeName);
    // statusLine = HttpHelper.putUrlEncoded(subNodeUrl,
    // formData).getStatusLine();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // String subNodePath = nodePath + "/" + subNodeName;
    // // make sure it's in the cache
    // service.getNodeData(nodePath, new LinkedList<String>(), new
    // LinkedList<String>());
    // service.getNodeData(subNodePath, new LinkedList<String>(), new
    // LinkedList<String>());
    //
    // boolean isInCache = service.hasNodeInCache(subNodePath);
    // assertTrue("sub node is not in cache", isInCache);
    //
    // // create attribute in the test node
    // String attrName = "test_attribute";
    // String attrPath = nodePath + ":" + attrName;
    // Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
    // GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");
    // service.getAttributeValue(attrPath);
    //
    // isInCache = service.hasAttributeInCache(attrPath);
    // assertTrue("attribute is not in cache", isInCache);
    //
    // GomHttpWrapper.deleteNode(Uri.parse(nodeUrl));
    //
    // // update may take a while
    // Thread.sleep(3000);
    //
    // isInCache = service.hasNodeInCache(subNodePath);
    // assertFalse("sub node shouldn't be in cache", isInCache);
    //
    // isInCache = service.hasAttributeInCache(attrPath);
    // assertFalse("attribute shouldn't be in cache", isInCache);
    //
    // }
    //
    // public void testRefreshNodeOnAttributeCreation() throws Exception {
    //
    // startService(mIntent);
    //
    // GomProxyService service = getService();
    // String timestamp = String.valueOf(System.currentTimeMillis());
    //
    // String nodePath = TEST_BASE_PATH +
    // "/test_refresh_node_on_attribute_creation/" + timestamp;
    // String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);
    //
    // Logger.d(LOG_TAG, "node URL: ", nodeUrl);
    //
    // // create the node we want to test
    // Map<String, String> formData = new HashMap<String, String>();
    // StatusLine statusLine = HttpHelper.putUrlEncoded(nodeUrl,
    // formData).getStatusLine();
    // int statusCode = statusLine.getStatusCode();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // // make sure it's in the cache
    // service.getNodeData(nodePath, new LinkedList<String>(), new
    // LinkedList<String>());
    //
    // // create attribute in the test node
    // String attrName = "test_attribute";
    // Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
    // GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");
    //
    // // update may take a while
    // Thread.sleep(3000);
    //
    // LinkedList<String> attrNames = new LinkedList<String>();
    // service.getNodeData(nodePath, new LinkedList<String>(), attrNames);
    //
    // assertTrue("attribute is missing", attrNames.contains(attrName));
    // }
    //
    // public void testRefreshNodeOnAttributeDeletion() throws Exception {
    //
    // startService(mIntent);
    //
    // GomProxyService service = getService();
    // String timestamp = String.valueOf(System.currentTimeMillis());
    //
    // String nodePath = TEST_BASE_PATH +
    // "/test_refresh_node_on_attribute_creation/" + timestamp;
    // String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);
    //
    // Logger.d(LOG_TAG, "node URL: ", nodeUrl);
    //
    // // create the node we want to test
    // Map<String, String> formData = new HashMap<String, String>();
    // StatusLine statusLine = HttpHelper.putUrlEncoded(nodeUrl,
    // formData).getStatusLine();
    // int statusCode = statusLine.getStatusCode();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // // create attribute in the test node
    // String attrName = "test_attribute";
    // Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
    // GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");
    //
    // // make sure the node's in the cache
    // LinkedList<String> attrNames = new LinkedList<String>();
    // service.getNodeData(nodePath, new LinkedList<String>(), attrNames);
    // assertTrue("attribute is missing", attrNames.contains(attrName));
    //
    // GomHttpWrapper.deleteAttribute(attrUri);
    //
    // // update may take a while
    // Thread.sleep(3000);
    //
    // attrNames = new LinkedList<String>();
    // service.getNodeData(nodePath, new LinkedList<String>(), attrNames);
    // assertFalse("attribute shouldn't be there",
    // attrNames.contains(attrName));
    // }
    //
    // public void testRefreshNodeOnSubNodeCreation() throws Exception {
    //
    // startService(mIntent);
    //
    // GomProxyService service = getService();
    // String timestamp = String.valueOf(System.currentTimeMillis());
    //
    // String nodePath = TEST_BASE_PATH +
    // "/test_refresh_node_on_attribute_creation/" + timestamp;
    // String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);
    //
    // Logger.d(LOG_TAG, "node URL: ", nodeUrl);
    //
    // // create the node we want to test
    // Map<String, String> formData = new HashMap<String, String>();
    // StatusLine statusLine = HttpHelper.putUrlEncoded(nodeUrl,
    // formData).getStatusLine();
    // int statusCode = statusLine.getStatusCode();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // // make sure it's in the cache
    // service.getNodeData(nodePath, new LinkedList<String>(), new
    // LinkedList<String>());
    //
    // // create sub node
    // String subNodeName = "subNode";
    // String subNodeUrl = UriHelper.join(nodeUrl, subNodeName);
    // statusLine = HttpHelper.putUrlEncoded(subNodeUrl,
    // formData).getStatusLine();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // // update may take a while
    // Thread.sleep(3000);
    //
    // LinkedList<String> subNodeNames = new LinkedList<String>();
    // service.getNodeData(nodePath, subNodeNames, new LinkedList<String>());
    //
    // assertTrue("sub node is missing", subNodeNames.contains(subNodeName));
    // }
    //
    // public void testRefreshNodeOnSubNodeDeletion() throws Exception {
    //
    // startService(mIntent);
    //
    // GomProxyService service = getService();
    // String timestamp = String.valueOf(System.currentTimeMillis());
    //
    // String nodePath = TEST_BASE_PATH +
    // "/test_refresh_node_on_attribute_creation/" + timestamp;
    // String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);
    //
    // Logger.d(LOG_TAG, "node URL: ", nodeUrl);
    //
    // // create the node we want to test
    // Map<String, String> formData = new HashMap<String, String>();
    // StatusLine statusLine = HttpHelper.putUrlEncoded(nodeUrl,
    // formData).getStatusLine();
    // int statusCode = statusLine.getStatusCode();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // // create sub node
    // String subNodeName = "subNode";
    // String subNodeUrl = UriHelper.join(nodeUrl, subNodeName);
    // statusLine = HttpHelper.putUrlEncoded(subNodeUrl,
    // formData).getStatusLine();
    // assertTrue("something went wrong with the PUT to the GOM - status code is: "
    // + statusCode,
    // statusCode < 300);
    //
    // // make sure it's in the cache
    // service.getNodeData(nodePath, new LinkedList<String>(), new
    // LinkedList<String>());
    //
    // LinkedList<String> subNodeNames = new LinkedList<String>();
    // service.getNodeData(nodePath, subNodeNames, new LinkedList<String>());
    // assertTrue("sub node is missing", subNodeNames.contains(subNodeName));
    //
    // // update may take a while
    // Thread.sleep(3000);
    //
    // GomHttpWrapper.deleteNode(Uri.parse(subNodeUrl));
    //
    // // update may take a while
    // Thread.sleep(3000);
    //
    // subNodeNames = new LinkedList<String>();
    // service.getNodeData(nodePath, subNodeNames, new LinkedList<String>());
    // assertFalse("sub node name shouldn't be there",
    // subNodeNames.contains(subNodeName));
    // }

    // helper methods
    private Intent createBroadcastIntent(String pUri, String pOperation, JSONObject pData) {

        Intent gnpIntent = new Intent(Y60Action.GOM_NOTIFICATION_BC);

        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH, pUri);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION, pOperation);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING, pData.toString());

        return gnpIntent;

    }

}