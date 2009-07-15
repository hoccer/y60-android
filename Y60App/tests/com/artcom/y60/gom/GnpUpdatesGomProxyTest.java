package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.net.Uri;

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

    // node(<-registered):attribute(<-created) = create on node
    public void testAttributeCreatedOnObservedNode() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper proxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String nodePath = TEST_BASE_PATH + "/test_refresh_node_on_attribute_creation/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        // create the node we want to test
        GomHttpWrapper.createNode(nodeUrl);

        final GomTestObserver gto = new GomTestObserver(this);

        BroadcastReceiver rec = GomNotificationHelper.registerObserverAndNotify(nodePath, gto,
                proxy, true);
        getActivity().registerReceiver(rec, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("gnp update callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }
                });

        // create attribute in the test node
        String attrName = "test_attribute";
        Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
        GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");

        // update may take a while
        TestHelper.blockUntilTrue("gnp create callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getCreateCount() == 1;
                    }
                });

        LinkedList<String> attrNames = new LinkedList<String>();
        proxy.getNodeData(nodePath, new LinkedList<String>(), attrNames);

        assertTrue("attribute should be in cache", attrNames.contains(attrName));
    }

    // node(<-registered):attribute(<-updated) = update on node
    public void testAttributeUpdatedOnObservedNode() throws Exception {

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

        assertEquals("cache should have the same value as in gom after callback was called once",
                "original value", helper.getCachedAttributeValue(attrPath));

        TestHelper.blockUntilResourceAvailable("observer node should be registered in gom",
                GomNotificationHelper.getObserverUriFor(nodePath));

        GomHttpWrapper.updateOrCreateAttribute(Uri.parse(Constants.Gom.URI + attrPath),
                "changed value");
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

        Logger.v(LOG_TAG, "data in notification: " + gto.getData());
        JSONObject receivedJsonData = gto.getData();
        assertTrue("json has attribute", receivedJsonData.has("attribute"));
        assertTrue("json has attribute.value", receivedJsonData.getJSONObject("attribute").has(
                "value"));
        assertEquals("value should have changed", "changed value", gto.getData().getJSONObject(
                "attribute").get("value"));

        assertEquals("the value should have changed in proxy", "changed value", helper
                .getAttribute(attrPath).getValue());
    }

    // node(<-registered):attribute(<-deleted) = delete on node
    public void testAttributeDeletedOnObservedNode() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper proxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String nodePath = TEST_BASE_PATH + "/test_delete_attribute_in_node_on_notification/"
                + timestamp;
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

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver rec = GomNotificationHelper.registerObserverAndNotify(nodePath, gto,
                proxy);
        getActivity().registerReceiver(rec, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("gnp update callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }
                });

        boolean isInCache = proxy.hasInCache(attrPath);
        assertTrue("attribute is missing", isInCache);

        GomHttpWrapper.deleteAttribute(attrUri);

        // update may take a while
        TestHelper.blockUntilTrue("gnp delete callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getDeleteCount() == 1;
                    }
                });

        assertTrue("node should NOT be deleted", proxy.hasInCache(nodePath));

        isInCache = proxy.hasInCache(attrPath);
        assertFalse("attribute '" + attrPath + "' shouldn't be there", isInCache);
    }

    // node(<-registered)/subnode(<-created) = create on node
    public void testSubnodeCreatedOnObservedNode() throws Exception {

        initializeActivity();

        TestHelper.blockUntilWebServerIsRunning();

        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        GomReference nodeRef = new GomReference(Constants.Gom.URI, TEST_BASE_PATH,
                "test_register_on_node_get_on_node_created", timestamp);

        GomHttpWrapper.createNode(nodeRef.url().toString());
        TestHelper.blockUntilResourceAvailable("node should be in gom", nodeRef.url().toString());

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver receiver = GomNotificationHelper.registerObserverAndNotify(
                nodeRef.path(), gto, helper);
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

        LinkedList<String> subNodeNames = new LinkedList<String>();
        helper.getCachedNodeData(nodeRef.path(), subNodeNames, new LinkedList<String>());

        assertEquals(
                "cache should have the node with zero subNodes after callback was called once", 0,
                subNodeNames.size());

        TestHelper.blockUntilResourceAvailable("observer node should be registered in gom",
                GomNotificationHelper.getObserverUriFor(nodeRef.path()));

        GomReference subNodeRef = nodeRef.subNode("subNode");
        HttpHelper.putXML(subNodeRef.url().toString(), "<node/>");

        subNodeNames = new LinkedList<String>();
        helper.getCachedNodeData(nodeRef.path(), subNodeNames, new LinkedList<String>());
        assertEquals(
                "cache should have the node with zero subNodes after callback was called once", 0,
                subNodeNames.size());

        TestHelper.blockUntilTrue("GNP should notify our observer about the new subnode", 4000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() {
                        return gto.getCreateCount() == 1;
                    }

                });

        Thread.sleep(3000);

        assertEquals("gnp update callback should have been called once", 1, gto.getUpdateCount());
        assertEquals("gnp create callback should have been called once", 1, gto.getCreateCount());
        assertEquals("gnp delete callback should not have been called", 0, gto.getDeleteCount());

        Logger.v(LOG_TAG, "data in notification: " + gto.getData());
        JSONObject jsonData = gto.getData();
        assertTrue("json has node", jsonData.has(Constants.Gom.Keywords.NODE));
        JSONObject node = jsonData.getJSONObject(Constants.Gom.Keywords.NODE);
        assertTrue("json has entries array", node.has(Constants.Gom.Keywords.ENTRIES));
        JSONArray entries = node.getJSONArray(Constants.Gom.Keywords.ENTRIES);
        assertEquals("entries array should be empty", 0, entries.length());

        subNodeNames = new LinkedList<String>();
        helper.getCachedNodeData(nodeRef.path(), subNodeNames, new LinkedList<String>());
        assertEquals("cache should have the node with one subNode after callback was called once",
                1, subNodeNames.size());
    }

    // node(<-registered)/subnode(<-deleted) = delete on node
    public void testSubnodeDeletedOnObservedNode() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper proxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String nodePath = TEST_BASE_PATH + "/test_delete_node_in_node_on_notification/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        GomHttpWrapper.createNode(nodeUrl);

        // create subnode in the test node
        String subNodeName = "sub_node";
        String subNodeUrl = nodeUrl + "/" + subNodeName;
        String subNodePath = nodePath + "/" + subNodeName;
        GomHttpWrapper.createNode(subNodeUrl);

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver rec = GomNotificationHelper.registerObserverAndNotify(nodePath, gto,
                proxy);
        getActivity().registerReceiver(rec, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("gnp update callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }
                });

        assertTrue("Node should be in cache", proxy.hasInCache(nodePath));
        assertFalse("Subnode should not be in cache (lazy loading)", proxy.hasInCache(subNodePath));

        LinkedList<String> list = new LinkedList<String>();
        list.add(subNodeName);
        proxy.saveNode(nodePath, list, new LinkedList<String>());
        proxy.saveNode(subNodePath, new LinkedList<String>(), new LinkedList<String>());
        Logger.v(LOG_TAG, subNodePath);
        // Thread.sleep(5000);

        assertTrue("Subnode should be in cache", proxy.hasInCache(subNodePath));

        GomHttpWrapper.deleteNode(Uri.parse(subNodeUrl));

        // update may take a while
        TestHelper.blockUntilTrue("gnp delete callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getDeleteCount() == 1;
                    }
                });

        assertTrue("node should NOT be deleted", proxy.hasInCache(nodePath));
        assertFalse("Subnode '" + subNodePath + "' should be deleted from cache", proxy
                .hasInCache(subNodePath));
    }

    // node(<-registered)/subnode(<-deleted)/subsubnode = 1 delete on node
    public void testSubnodeWithNodeDeletedOnObservedNode() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper proxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String nodePath = TEST_BASE_PATH
                + "/test_delete_node_with_subnode_in_node_on_notification/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);
        GomHttpWrapper.createNode(nodeUrl);

        // create subnode in the test timestamp node
        String subNodeName = "sub_node";
        String subNodeUrl = nodeUrl + "/" + subNodeName;
        String subNodePath = nodePath + "/" + subNodeName;
        GomHttpWrapper.createNode(subNodeUrl);

        // create subSubNode in the test subnode
        String subSubNodeName = "sub_sub_node";
        String subSubNodeUrl = subNodeUrl + "/" + subSubNodeName;
        String subSubNodePath = subNodePath + "/" + subSubNodeName;
        GomHttpWrapper.createNode(subSubNodeUrl);

        TestHelper.blockUntilResourceAvailable("sub sub node should be in GOM", subSubNodeUrl);

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver rec = GomNotificationHelper.registerObserverAndNotify(nodePath, gto,
                proxy, true);
        getActivity().registerReceiver(rec, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("gnp update callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }
                });

        assertTrue("Node should be in cache", proxy.hasInCache(nodePath));
        assertFalse("Subnode should not be in cache (lazy loading)", proxy.hasInCache(subNodePath));

        GomHttpWrapper.deleteNode(Uri.parse(subNodeUrl));

        // update may take a while
        TestHelper.blockUntilTrue("gnp delete callback should have been called only once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        Logger.v(LOG_TAG, "ooooooooooooooooooooo", gto.getCreateCount());
                        Logger.v(LOG_TAG, "ooooooooooooooooooooo", gto.getUpdateCount());
                        Logger.v(LOG_TAG, "ooooooooooooooooooooo", gto.getDeleteCount());

                        return gto.getDeleteCount() == 1;
                    }
                });

        assertTrue("node should NOT be deleted", proxy.hasInCache(nodePath));
    }

    // node(<-registered, <-deleted) = delete on node
    public void testObservedNodeDeleted() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper proxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = TEST_BASE_PATH + "/test_delete_node_on_notification/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        GomHttpWrapper.createNode(nodeUrl);

        TestHelper.blockUntilResourceAvailable("node should be in GOM", nodeUrl);

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver rec = GomNotificationHelper.registerObserverAndNotify(nodePath, gto,
                proxy, true);
        getActivity().registerReceiver(rec, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("gnp update callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }
                });

        assertTrue("Node should be in cache", proxy.hasInCache(nodePath));

        GomHttpWrapper.deleteNode(Uri.parse(nodeUrl));

        // update may take a while
        TestHelper.blockUntilTrue("gnp delete callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getDeleteCount() == 1;
                    }
                });

        assertFalse("attribute should not be in cache", proxy.hasInCache(nodePath));
    }

    // node(<-registered, <-deleted), /subnode, :attribute = delete on node,
    public void testObservedNodeWithSubnodeAndAttributeDeleted() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper proxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = TEST_BASE_PATH + "/test_recursive_delete_node_on_notification/"
                + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        GomHttpWrapper.createNode(nodeUrl);
        TestHelper.blockUntilResourceAvailable("node should be in GOM", nodeUrl);

        // create sub node
        String subNodeName = "subNode";
        String subNodeUrl = UriHelper.join(nodeUrl, subNodeName);
        String subNodePath = nodePath + "/" + subNodeName;

        GomHttpWrapper.createNode(subNodeUrl);
        TestHelper.blockUntilResourceAvailable("sub node should be in GOM", subNodeUrl);

        // create attribute in the test node
        String attrName = "test_attribute";
        String attrPath = nodePath + ":" + attrName;
        Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
        GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver rec = GomNotificationHelper.registerObserverAndNotify(nodePath, gto,
                proxy, true);
        getActivity().registerReceiver(rec, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("gnp update callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }
                });

        assertTrue("Node should be in cache", proxy.hasInCache(nodePath));
        assertTrue("attribute should be in cache", proxy.hasInCache(attrPath));

        // make sure the subnode's in the cache
        proxy.getNodeData(subNodePath, new LinkedList<String>(), new LinkedList<String>());
        assertTrue("sub node should be in cache", proxy.hasInCache(subNodePath));

        GomHttpWrapper.deleteNode(Uri.parse(nodeUrl));

        // update may take a while
        TestHelper.blockUntilTrue("gnp delete callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getDeleteCount() == 1;
                    }
                });

        assertFalse("node shouldn't be in cache", proxy.hasInCache(nodePath));
        assertFalse("sub node shouldn't be in cache", proxy.hasInCache(subNodePath));
        assertFalse("attribute shouldn't be in cache", proxy.hasInCache(attrPath));

    }

    // attribute(<-registered, <-deleted) = delete on attribute
    public void testObservedAttributeDeleted() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper proxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String attrPath = TEST_BASE_PATH + "/test_observed_attribute_deleted:" + timestamp;
        String attrUrl = UriHelper.join(Constants.Gom.URI, attrPath);
        GomHttpWrapper.updateOrCreateAttribute(Uri.parse(attrUrl), "mango");

        TestHelper.blockUntilResourceAvailable("node should be in GOM", attrUrl);

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver rec = GomNotificationHelper.registerObserverAndNotify(attrPath, gto,
                proxy, true);
        getActivity().registerReceiver(rec, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("gnp update callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }
                });

        assertTrue("attribute should be in cache", proxy.hasInCache(attrPath));

        GomHttpWrapper.deleteAttribute(Uri.parse(attrUrl));

        // update may take a while
        TestHelper.blockUntilTrue("gnp delete callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getDeleteCount() == 1;
                    }
                });

        assertFalse("node is in cache", proxy.hasInCache(attrPath));

    }

    // attribute(<-registered, <-updated) = update on attribute
    public void testObservedAttributeUpdated() throws Exception {

        initializeActivity();
        TestHelper.blockUntilWebServerIsRunning();
        GomProxyHelper proxy = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String attrPath = TEST_BASE_PATH + "/test_observed_attribute_updated:" + timestamp;
        String attrUrl = UriHelper.join(Constants.Gom.URI, attrPath);
        GomHttpWrapper.updateOrCreateAttribute(Uri.parse(attrUrl), "mango");

        TestHelper.blockUntilResourceAvailable("node should be in GOM", attrUrl);

        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver rec = GomNotificationHelper.registerObserverAndNotify(attrPath, gto,
                proxy, true);
        getActivity().registerReceiver(rec, Constants.Gom.GNP_INTENT_FILTER);

        TestHelper.blockUntilTrue("gnp update callback should have been called once", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 1;
                    }
                });

        assertTrue("attribute should be in cache", proxy.hasInCache(attrPath));

        GomHttpWrapper.updateOrCreateAttribute(Uri.parse(attrUrl), "banana");

        // update may take a while
        TestHelper.blockUntilTrue("gnp update callback should have been called once more", 5000,
                new TestHelper.Condition() {
                    @Override
                    public boolean isSatisfied() {
                        return gto.getUpdateCount() == 2;
                    }
                });

        assertTrue("attribute should be in cache", proxy.hasInCache(attrPath));
        assertEquals("attributes value should be changed in proxy", "banana", proxy
                .getCachedAttributeValue(attrPath));
    }
}
