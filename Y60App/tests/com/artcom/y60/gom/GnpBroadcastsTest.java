package com.artcom.y60.gom;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.artcom.y60.Constants;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.Y60Action;

public class GnpBroadcastsTest extends GomActivityUnitTestCase {

    protected final String LOG_TAG        = "GnpRoundtripTest";
    protected final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/" + LOG_TAG;

    private JSONObject     mJson;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mJson = new JSONObject("{\"attribute\": { \"name\":\"hans\" , \"value\":\"keks\"}}");
    }

    public void testNotificationCreate() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_notification_create";
        String attrPath = testPath + ":" + timestamp;

        GomTestObserver gomObserver = new GomTestObserver(this);

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserverAndNotify(attrPath, gomObserver, helper);

        JSONObject json = new JSONObject(TestHelper.createJsonFromAttr(testPath, timestamp, "keks"));

        Intent bcIntent = createBroadcastIntent(attrPath, "create", json);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateCalled();
        gomObserver.assertDeleteNotCalled();
        gomObserver.assertUpdateNotCalled();

        assertEquals("path doesn't match", attrPath, gomObserver.getPath());

        JSONObject attrData = gomObserver.getData().getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
        String observedParentPath = attrData.getString(Constants.Gom.Keywords.NODE);
        String observedAttrName = attrData.getString(Constants.Gom.Keywords.NAME);
        String observedAttrValue = attrData.getString(Constants.Gom.Keywords.VALUE);
        assertEquals("Parent path should be equal", testPath, observedParentPath);
        assertEquals("Attr name should be equal", timestamp, observedAttrName);
        assertEquals("Attr value should be equal", "keks", observedAttrValue);

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

        JSONObject json = new JSONObject(TestHelper.createJsonFromAttr(testPath, timestamp, "keks"));
        Intent bcIntent = createBroadcastIntent(attrPath, "delete", json);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateNotCalled();
        gomObserver.assertUpdateNotCalled();
        gomObserver.assertDeleteCalled();
        assertEquals("path doesn't match", attrPath, gomObserver.getPath());

        Logger.v(LOG_TAG, gomObserver.getPath());

        JSONObject attrData = gomObserver.getData().getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
        String observedParentPath = attrData.getString(Constants.Gom.Keywords.NODE);
        String observedAttrName = attrData.getString(Constants.Gom.Keywords.NAME);
        String observedAttrValue = attrData.getString(Constants.Gom.Keywords.VALUE);
        assertEquals("Parent path should be equal", testPath, observedParentPath);
        assertEquals("Attr name should be equal", timestamp, observedAttrName);
        assertEquals("Attr value should be equal", "keks", observedAttrValue);

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

        JSONObject json = new JSONObject(TestHelper.createJsonFromAttr(testPath, timestamp, "keks"));
        Intent bcIntent = createBroadcastIntent(attrPath, "update", json);
        br.onReceive(null, bcIntent);

        gomObserver.assertCreateNotCalled();
        gomObserver.assertUpdateCalled();
        gomObserver.assertDeleteNotCalled();

        assertEquals("path doesn't match", attrPath, gomObserver.getPath());

        JSONObject attrData = gomObserver.getData().getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
        String observedParentPath = attrData.getString(Constants.Gom.Keywords.NODE);
        String observedAttrName = attrData.getString(Constants.Gom.Keywords.NAME);
        String observedAttrValue = attrData.getString(Constants.Gom.Keywords.VALUE);
        assertEquals("Parent path should be equal", testPath, observedParentPath);
        assertEquals("Attr name should be equal", timestamp, observedAttrName);
        assertEquals("Attr value should be equal", "keks", observedAttrValue);
    }

    public void testCreateNotificationRefreshesGomProxy() throws Exception {

        initializeActivity();
        GomProxyHelper gomProxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String attrPath = "pathToAttribute:" + timestamp;

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserverAndNotify(attrPath, new GomTestObserver(this),
                gomProxy);
        // you will probably get a delete notification

        JSONObject createdAttribute = new JSONObject(TestHelper.createJsonFromAttr(
                "pathToAttribute", timestamp, "keks"));

        Intent bcIntent = createBroadcastIntent(attrPath, "create", createdAttribute);
        br.onReceive(null, bcIntent);

        assertTrue("attribute should be in cache", gomProxy.hasInCache(attrPath));
        assertEquals("gom proxy should provide the newly created attribute from cache", "keks",
                gomProxy.getCachedAttributeValue(attrPath));
    }

    public void testUpdateNotificationRefreshesGomProxy() throws Exception {

        initializeActivity();
        GomProxyHelper gomProxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String attrPath = "pathToAttribute:" + timestamp;
        gomProxy.saveAttribute(attrPath, "old value");
        assertEquals("gom proxy should provide the original attribute from cache", "old value",
                gomProxy.getCachedAttributeValue(attrPath));

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserverAndNotify(attrPath, new GomTestObserver(this),
                gomProxy);
        // you will probably get a delete notification

        JSONObject createdAttribute = new JSONObject(TestHelper.createJsonFromAttr(
                "pathToAttribute", timestamp, "keks"));

        Intent bcIntent = createBroadcastIntent(attrPath, "update", createdAttribute);
        br.onReceive(null, bcIntent);

        assertEquals("gom proxy should provide the newly created attribute from cache", "keks",
                gomProxy.getCachedAttributeValue(attrPath));
    }

    public void testDeleteNotificationRefreshesGomProxy() throws Exception {

        initializeActivity();
        GomProxyHelper gomProxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String attrPath = "pathToAttribute:" + timestamp;
        gomProxy.saveAttribute(attrPath, "keks");
        assertEquals("gom proxy should provide the original attribute from cache", "keks", gomProxy
                .getCachedAttributeValue(attrPath));

        BroadcastReceiver br;
        br = GomNotificationHelper.registerObserverAndNotify(attrPath, new GomTestObserver(this),
                gomProxy);
        // you will probably get a delete notification

        JSONObject createdAttribute =  new JSONObject(TestHelper.createJsonFromAttr("pathToAttribute", timestamp,
                "keks"));

        Intent bcIntent = createBroadcastIntent(attrPath, "delete", createdAttribute);
        br.onReceive(null, bcIntent);

        assertFalse("attribute should be deleted from cache", gomProxy.hasInCache(attrPath));

    }

    private Intent createBroadcastIntent(String pUri, String pOperation, JSONObject pData)
            throws Exception {

        Intent gnpIntent = new Intent(Y60Action.GOM_NOTIFICATION_BC);

        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH, pUri);
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION, pOperation);

        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING, pData.toString());
        return gnpIntent;

    }

}
