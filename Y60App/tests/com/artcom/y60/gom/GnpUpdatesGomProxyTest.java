package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.net.Uri;
import android.test.suitebuilder.annotation.Suppress;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.UriHelper;

public class GnpUpdatesGomProxyTest extends GomActivityUnitTestCase {

    protected final String LOG_TAG        = "GnpUpdatesGomProxyTest";
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

    @Suppress
    public void testGnpUpdatesGomProxyRoundtrip() throws Exception {

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
        getActivity().registerReceiver(receiver, Constants.Gom.GNP_INTENT_FILTER);

        // not in cache, we do not get immediate response
        assertEquals("gnp update callback should not have been called", 0, gto.getUpdateCount());
        assertEquals("gnp create callback should not have been called", 0, gto.getCreateCount());
        assertEquals("gnp delete callback should not have been called", 0, gto.getDeleteCount());

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

        assertEquals("cache should have the same value as in gom after callback was called once",
                "original value", helper.getCachedAttributeValue(attrPath));

        TestHelper.blockUntilResourceAvailable("observer node should be registered in gom",
                GomNotificationHelper.getObserverUriFor(nodePath));

        HttpHelper.putXML(Constants.Gom.URI + attrPath, "<attribute>changed value</attribute>");
        assertEquals("cache should still have the old gom value", "original value", helper
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

        // ensure we do not get any other gom notifications
        Thread.sleep(3000);

        assertEquals("gnp update callback should have been called once", 2, gto.getUpdateCount());
        assertEquals("gnp create callback should not have been called", 0, gto.getCreateCount());
        assertEquals("gnp delete callback should not have been called", 0, gto.getDeleteCount());

        Logger.v(LOG_TAG, "data in cache: " + gto.getData());
        JSONObject receivedJsonData = gto.getData();
        assertTrue("json has attribute", receivedJsonData.has("attribute"));
        assertTrue("json has attribute.value", receivedJsonData.getJSONObject("attribute").has(
                "value"));
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

}
