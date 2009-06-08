package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.StatusLine;

import android.content.Intent;
import android.net.Uri;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.Suppress;

import com.artcom.y60.Constants;
import com.artcom.y60.HTTPHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.UriHelper;

public class GomProxyServiceTest extends ServiceTestCase<GomProxyService> {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "GomProxyServiceTest";

    private static final String BASE_TEST_PATH = "/test/android/y60/infrastructure_gom/gom_proxy_service_test";

    // Instance Variables ------------------------------------------------

    private Intent mIntent;

    // Constructors ------------------------------------------------------

    public GomProxyServiceTest() {

        super(GomProxyService.class);
    }

    // Public Instance Methods -------------------------------------------

    public void testGetBaseUri() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        assertNotNull("service must not be null", service);

        assertEquals("http://t-gom.service.t-gallery.act", service.getBaseUri());
    }

    public void testGetAttribute() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        assertNotNull("service must not be null", service);

        String attrPath = "/test/android/y60/infrastructure_gom/gom_proxy_service_test:attribute";
        // creating the fixture
        HTTPHelper.putXML(service.getBaseUri() + attrPath, "<attribute>honolulu</attribute>");
        assertEquals("honolulu", service.getAttributeValue(attrPath));
        assertTrue(service.hasAttributeInCache(attrPath));
    }

    public void testGetNode() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        assertNotNull("service must not be null", service);

        List<String> subNodeNames = new LinkedList<String>();
        List<String> attributeNames = new LinkedList<String>();
        String nodePath = "/test/android/y60/infrastructure_gom/gom_proxy_service_test/node";

        // creating the fixture
        HTTPHelper.putXML(service.getBaseUri() + nodePath, "<node></node>");
        HTTPHelper.putXML(service.getBaseUri() + nodePath + "/a_sub_node", "<node></node>");
        HTTPHelper.putXML(service.getBaseUri() + nodePath + ":an_attribute",
                "<attribute>honolulu</attribute>");

        service.getNodeData(nodePath, subNodeNames, attributeNames);

        assertTrue(service.hasNodeInCache(nodePath));

        assertEquals(1, subNodeNames.size());
        assertEquals("a_sub_node", subNodeNames.get(0));

        assertEquals(1, attributeNames.size());
        assertEquals("an_attribute", attributeNames.get(0));

        assertEquals(
                "honolulu",
                service
                        .getAttributeValue("/test/android/y60/infrastructure_gom/gom_proxy_service_test:attribute"));
    }

    @Suppress
    public void testUpdateAttributeOnNotification() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String attrPath = BASE_TEST_PATH + "/test_refresh_attribute_on_notification:" + timestamp;
        String attrUrl = UriHelper.join(Constants.Gom.URI, attrPath);

        Logger.d(LOG_TAG, "attribute URL: ", attrUrl);

        // create the attribute we want to test
        Map<String, String> formData = new HashMap<String, String>();
        String oldValue = "Gehen sie weiter, es gibt nichts zu sehen.";
        formData.put(Constants.Gom.Keywords.ATTRIBUTE, oldValue);
        StatusLine statusLine = HTTPHelper.putUrlEncoded(attrUrl, formData);
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT old value to the GOM - status code is: "
                + statusCode, statusCode < 300);

        // make sure the proxy has the attribute cached
        service.getAttributeValue(attrPath);

        // change the value in GOM
        String newValue = "Der Wert ist mal wieder ganz egal.";
        formData.put(Constants.Gom.Keywords.ATTRIBUTE, newValue);
        statusLine = HTTPHelper.putUrlEncoded(attrUrl, formData);
        assertTrue("something went wrong with the PUT new value to the GOM", statusLine
                .getStatusCode() < 300);

        // update may take a while
        Thread.sleep(3000);

        // check that the value in the service has been updated as well
        String actualValue = service.getAttributeValue(attrPath);
        assertEquals("value in GOM is not the updated value", newValue, actualValue);
    }

    @Suppress
    public void testDeleteAttributeOnNotification() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = BASE_TEST_PATH + "/test_delete_attribute_on_notification/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        // create the node we want to test
        Map<String, String> formData = new HashMap<String, String>();
        StatusLine statusLine = HTTPHelper.putUrlEncoded(nodeUrl, formData);
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // create attribute in the test node
        String attrName = "test_attribute";
        Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
        GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");

        String attrPath = nodePath + ":" + attrName;

        service.getAttributeValue(attrPath);
        boolean isInCache = getService().hasAttributeInCache(attrPath);
        assertTrue("attribute is missing", isInCache);

        GomHttpWrapper.deleteAttribute(attrUri);

        // update may take a while
        Thread.sleep(3000);

        isInCache = getService().hasAttributeInCache(attrPath);
        assertFalse("attribute '" + attrPath + "' shouldn't be there", isInCache);
    }

    @Suppress
    public void testDeleteNodeOnNotification() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = BASE_TEST_PATH + "/test_delete_node_on_notification/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        // create the node we want to test
        Map<String, String> formData = new HashMap<String, String>();
        StatusLine statusLine = HTTPHelper.putUrlEncoded(nodeUrl, formData);
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // make sure it's in the cache
        service.getNodeData(nodePath, new LinkedList<String>(), new LinkedList<String>());

        boolean isInCache = service.hasNodeInCache(nodePath);
        assertTrue("node is missing", isInCache);

        GomHttpWrapper.deleteNode(Uri.parse(nodeUrl));

        // update may take a while
        Thread.sleep(3000);

        isInCache = service.hasNodeInCache(nodePath);
        assertFalse("node is in cache", isInCache);
    }

    @Suppress
    public void testRecursiveDeleteNodeOnNotification() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = BASE_TEST_PATH + "/test_recursive_delete_node_on_notification/"
                + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        Map<String, String> formData = new HashMap<String, String>();
        StatusLine statusLine = HTTPHelper.putUrlEncoded(nodeUrl, formData);
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // create sub node
        String subNodeName = "subNode";
        String subNodeUrl = UriHelper.join(nodeUrl, subNodeName);
        statusLine = HTTPHelper.putUrlEncoded(subNodeUrl, formData);
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        String subNodePath = nodePath + "/" + subNodeName;
        // make sure it's in the cache
        service.getNodeData(nodePath, new LinkedList<String>(), new LinkedList<String>());
        service.getNodeData(subNodePath, new LinkedList<String>(), new LinkedList<String>());

        boolean isInCache = service.hasNodeInCache(subNodePath);
        assertTrue("sub node is not in cache", isInCache);

        // create attribute in the test node
        String attrName = "test_attribute";
        String attrPath = nodePath + ":" + attrName;
        Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
        GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");
        service.getAttributeValue(attrPath);

        isInCache = service.hasAttributeInCache(attrPath);
        assertTrue("attribute is not in cache", isInCache);

        GomHttpWrapper.deleteNode(Uri.parse(nodeUrl));

        // update may take a while
        Thread.sleep(3000);

        isInCache = service.hasNodeInCache(subNodePath);
        assertFalse("sub node shouldn't be in cache", isInCache);

        isInCache = service.hasAttributeInCache(attrPath);
        assertFalse("attribute shouldn't be in cache", isInCache);

    }

    @Suppress
    public void testRefreshNodeOnAttributeCreation() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = BASE_TEST_PATH + "/test_refresh_node_on_attribute_creation/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        // create the node we want to test
        Map<String, String> formData = new HashMap<String, String>();
        StatusLine statusLine = HTTPHelper.putUrlEncoded(nodeUrl, formData);
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // make sure it's in the cache
        service.getNodeData(nodePath, new LinkedList<String>(), new LinkedList<String>());

        // create attribute in the test node
        String attrName = "test_attribute";
        Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
        GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");

        // update may take a while
        Thread.sleep(3000);

        LinkedList<String> attrNames = new LinkedList<String>();
        service.getNodeData(nodePath, new LinkedList<String>(), attrNames);

        assertTrue("attribute is missing", attrNames.contains(attrName));
    }

    @Suppress
    public void testRefreshNodeOnAttributeDeletion() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = BASE_TEST_PATH + "/test_refresh_node_on_attribute_creation/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        // create the node we want to test
        Map<String, String> formData = new HashMap<String, String>();
        StatusLine statusLine = HTTPHelper.putUrlEncoded(nodeUrl, formData);
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // create attribute in the test node
        String attrName = "test_attribute";
        Uri attrUri = Uri.parse(nodeUrl + ":" + attrName);
        GomHttpWrapper.updateOrCreateAttribute(attrUri, "who cares?");

        // make sure the node's in the cache
        LinkedList<String> attrNames = new LinkedList<String>();
        service.getNodeData(nodePath, new LinkedList<String>(), attrNames);
        assertTrue("attribute is missing", attrNames.contains(attrName));

        GomHttpWrapper.deleteAttribute(attrUri);

        // update may take a while
        Thread.sleep(3000);

        attrNames = new LinkedList<String>();
        service.getNodeData(nodePath, new LinkedList<String>(), attrNames);
        assertFalse("attribute shouldn't be there", attrNames.contains(attrName));
    }

    @Suppress
    public void testRefreshNodeOnSubNodeCreation() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = BASE_TEST_PATH + "/test_refresh_node_on_attribute_creation/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        // create the node we want to test
        Map<String, String> formData = new HashMap<String, String>();
        StatusLine statusLine = HTTPHelper.putUrlEncoded(nodeUrl, formData);
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // make sure it's in the cache
        service.getNodeData(nodePath, new LinkedList<String>(), new LinkedList<String>());

        // create sub node
        String subNodeName = "subNode";
        String subNodeUrl = UriHelper.join(nodeUrl, subNodeName);
        statusLine = HTTPHelper.putUrlEncoded(subNodeUrl, formData);
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // update may take a while
        Thread.sleep(3000);

        LinkedList<String> subNodeNames = new LinkedList<String>();
        service.getNodeData(nodePath, subNodeNames, new LinkedList<String>());

        assertTrue("sub node is missing", subNodeNames.contains(subNodeName));
    }

    @Suppress
    public void testRefreshNodeOnSubNodeDeletion() throws Exception {

        startService(mIntent);

        GomProxyService service = getService();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String nodePath = BASE_TEST_PATH + "/test_refresh_node_on_attribute_creation/" + timestamp;
        String nodeUrl = UriHelper.join(Constants.Gom.URI, nodePath);

        Logger.d(LOG_TAG, "node URL: ", nodeUrl);

        // create the node we want to test
        Map<String, String> formData = new HashMap<String, String>();
        StatusLine statusLine = HTTPHelper.putUrlEncoded(nodeUrl, formData);
        int statusCode = statusLine.getStatusCode();
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // create sub node
        String subNodeName = "subNode";
        String subNodeUrl = UriHelper.join(nodeUrl, subNodeName);
        statusLine = HTTPHelper.putUrlEncoded(subNodeUrl, formData);
        assertTrue("something went wrong with the PUT to the GOM - status code is: " + statusCode,
                statusCode < 300);

        // make sure it's in the cache
        service.getNodeData(nodePath, new LinkedList<String>(), new LinkedList<String>());

        LinkedList<String> subNodeNames = new LinkedList<String>();
        service.getNodeData(nodePath, subNodeNames, new LinkedList<String>());
        assertTrue("sub node is missing", subNodeNames.contains(subNodeName));

        // update may take a while
        Thread.sleep(3000);

        GomHttpWrapper.deleteNode(Uri.parse(subNodeUrl));

        // update may take a while
        Thread.sleep(3000);

        subNodeNames = new LinkedList<String>();
        service.getNodeData(nodePath, subNodeNames, new LinkedList<String>());
        assertFalse("sub node name shouldn't be there", subNodeNames.contains(subNodeName));
    }

    // Protected Instance Methods ----------------------------------------

    protected void setUp() throws Exception {

        super.setUp();
        mIntent = new Intent(getContext(), GomProxyService.class);
    }

    protected void tearDown() throws Exception {

        super.tearDown();
    }

}
