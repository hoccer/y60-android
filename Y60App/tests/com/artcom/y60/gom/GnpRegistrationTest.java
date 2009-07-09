package com.artcom.y60.gom;

import java.util.LinkedList;
import java.util.List;

import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.gom.GomTestObserver.Event;

public class GnpRegistrationTest extends GomActivityUnitTestCase {

    protected final String LOG_TAG        = "GnpRegistrationTest";
    protected final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/" + LOG_TAG;

    public void setUp() throws Exception {
        super.setUp();

    }

    // The 5 different cases of callbacks during registerObserver()

    // 1. entry neither in proxy nor in gom
    // -> exception | entry deleted callback

    public void testAttributeNotInProxyNotInGom() throws Exception {

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

}
