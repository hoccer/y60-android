package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.StatusLine;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.test.suitebuilder.annotation.Suppress;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.TestHelper;
import com.artcom.y60.Y60Action;
import com.artcom.y60.gom.GomTestObserver.Event;

public class GomNotificationHelperIntegrationTest extends GomActivityUnitTestCase {

    private static final String LOG_TAG        = "GomNotificationHelperIntegrationTest";
    private static final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/gom_notification_helper_integration_test";

    public GomNotificationHelperIntegrationTest() {

    }

    @Override
    protected void initializeActivity() {
        super.initializeActivity();
        getActivity().startService(new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER));
    }

    public void testCallbackWithOldAttributeDataInProxyAndNewVDataInGom() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String attrPath = TEST_BASE_PATH
                + "/test_callback_with_old_attribute_data_in_proxy_and_new_data_in_gom" + ":"
                + timestamp;
        Uri attrUrl = Uri.parse(Constants.Gom.URI + attrPath);
        String oldAttrValue = "alt_im_proxy";
        String newAttrValue = "neu_im_gom";

        helper.saveAttribute(attrPath, oldAttrValue);
        GomHttpWrapper.updateOrCreateAttribute(attrUrl, newAttrValue);

        final GomTestObserver gto = new GomTestObserver();
        GomNotificationHelper.registerObserver(attrPath, gto, helper);

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
    }

    public void testCallbackWithoutAttributeInCache() throws Exception {

        initializeActivity();
        GomProxyHelper helper = createHelper();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String attrPath = TEST_BASE_PATH + "/test_callback_without_attribute_in_cache" + ":"
                + timestamp;
        Uri attrUrl = Uri.parse(Constants.Gom.URI + attrPath);
        String attrValue = "value_im_gom";

        GomHttpWrapper.updateOrCreateAttribute(attrUrl, attrValue);

        final GomTestObserver gto = new GomTestObserver();
        GomNotificationHelper.registerObserver(attrPath, gto, helper);

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
        assertFalse("Update is called another time", gto.getUpdateCount() > 1);

    }

    @Suppress
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

        GomTestObserver observer = new GomTestObserver();
        BroadcastReceiver receiver = GomNotificationHelper.registerObserver(observedPath, observer,
                helper);
        getActivity().registerReceiver(receiver, Constants.Gom.NOTIFICATION_FILTER);

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

}
