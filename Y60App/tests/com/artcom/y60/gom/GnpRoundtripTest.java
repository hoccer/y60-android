package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.StatusLine;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.test.suitebuilder.annotation.LargeTest;

import com.artcom.y60.Constants;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.http.HttpHelper;

public class GnpRoundtripTest extends GomActivityUnitTestCase {
    
    protected final String LOG_TAG        = "GnpRoundtripTest";
    protected final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/" + LOG_TAG;
    
    @LargeTest
    public void testRegExpConstraintOnObserver() throws Exception {
        
        initializeActivity();
        
        TestHelper.blockUntilDeviceControllerIsRunning();
        
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
        String visibleAttrUrl = Constants.Gom.URI + visibleAttrPath;
        GomHttpWrapper.updateOrCreateAttribute(visibleAttrUrl, "who cares?");
        
        String invisibleAttrPath = visibleNodePath + ":invalid_attribute";
        String invisibleAttrUrl = Constants.Gom.URI + invisibleAttrPath;
        GomHttpWrapper.updateOrCreateAttribute(invisibleAttrUrl, "who else cares?");
        
        String content = HttpHelper.getAsString(invisibleAttrUrl);
        assertNotNull(content);
        
        String content2 = HttpHelper.getAsString(visibleAttrUrl);
        assertNotNull(content2);
        
        GomTestObserver observer = new GomTestObserver(this);
        BroadcastReceiver receiver = GomNotificationHelper.createObserverAndNotify(observedPath,
                observer, helper, false);
        
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
        
        GomHttpWrapper.deleteNode(Constants.Gom.URI + invisibleNodePath);
        Thread.sleep(2500);
        observer.assertCreateNotCalled();
        observer.assertDeleteNotCalled();
        // observer.assertUpdateNotCalled();
        observer.reset();
        
        GomHttpWrapper.deleteNode(Constants.Gom.URI + visibleNodePath);
        Thread.sleep(4000);
        observer.assertCreateNotCalled();
        observer.assertDeleteCalled();
        // observer.assertUpdateCalled();
        observer.reset();
        
        GomHttpWrapper.deleteNode(Constants.Gom.URI + observedPath);
        Thread.sleep(4000);
        observer.assertCreateNotCalled();
        observer.assertDeleteCalled();
        // observer.assertUpdateCalled();
        observer.reset();
        
    }
    
    // initial state: value in gom, NOT in proxy
    @LargeTest
    public void testRegisterObserverMultipleTimes() throws Exception {
        
        initializeActivity();
        TestHelper.blockUntilDeviceControllerIsRunning();
        GomProxyHelper helper = createHelper();
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String testPath = TEST_BASE_PATH + "/test_register_observer_multiple_times";
        String nodePath = testPath + "/" + timestamp;
        
        final GomTestObserver observer = new GomTestObserver(this);
        
        // create node in gom
        GomHttpWrapper.createNode(Constants.Gom.URI + nodePath);
        assertNotNull("missing node in GOM", HttpHelper.getAsString(Constants.Gom.URI + nodePath));
        
        BroadcastReceiver receiver = GomNotificationHelper.createObserverAndNotify(nodePath,
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
        
        // create attribute in gom
        String visibleAttrPath = nodePath + ":attribute";
        //Uri visibleAttrUrl = Uri.parse(Constants.Gom.URI + visibleAttrPath);
        GomHttpWrapper.updateOrCreateAttribute(Constants.Gom.URI + visibleAttrPath, "who cares?");
        
        TestHelper.blockUntilTrue("create not called", 4000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getCreateCount() == 1;
            }
            
        });
        
        Thread.sleep(3000);
        observer.assertDeleteNotCalled();
        assertEquals("create should be called only once", 1, observer.getCreateCount());
        assertEquals("update should be called only once", 1, observer.getUpdateCount());
        
        // register multiple times
        GomNotificationHelper.createObserverAndNotify(nodePath, observer, helper);
        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 2;
            }
            
        });
        Thread.sleep(2500);
        observer.assertDeleteNotCalled();
        assertEquals("create should be called only once", 1, observer.getCreateCount());
        assertEquals("update should be called 2 times", 2, observer.getUpdateCount());
        
        GomNotificationHelper.createObserverAndNotify(nodePath, observer, helper);
        TestHelper.blockUntilTrue("update not called", 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                return observer.getUpdateCount() == 3;
            }
            
        });
        Thread.sleep(2500);
        observer.assertDeleteNotCalled();
        assertEquals("create should be called only once", 1, observer.getCreateCount());
        assertEquals("update should be called 3 times", 3, observer.getUpdateCount());
        
        GomHttpWrapper.updateOrCreateAttribute(Constants.Gom.URI + visibleAttrPath, "who else cares?");
        
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
    
    // ROUNDTRIPS
    
    // create attribute in gom, register gnp, get first onEntryUpdate, change
    // value, get second onEntryUpdate
    public void testSimpleGnpRoundtrip() throws Exception {
        
        initializeActivity();
        
        TestHelper.blockUntilDeviceControllerIsRunning();
        
        GomProxyHelper helper = createHelper();
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        String attrPath = TEST_BASE_PATH + "/test_on_attribute_updated" + ":" + timestamp;
        
        HttpHelper.putXML(Constants.Gom.URI + attrPath, "<attribute>original value</attribute>");
        assertEquals("original value", HttpHelper
                .getAsString(Constants.Gom.URI + attrPath + ".txt"));
        
        final GomTestObserver gto = new GomTestObserver(this);
        BroadcastReceiver receiver = GomNotificationHelper.createObserverAndNotify(attrPath, gto,
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
        // Its so unbelievably fast, that this condition isnt fullfilled anymore. maybe nice to test
        // in the future.
        // assertEquals("cache should still have the old value", "original value", helper
        // .getCachedAttributeValue(attrPath));
        
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
    
}
