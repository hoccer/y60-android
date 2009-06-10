package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.StatusLine;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.test.suitebuilder.annotation.Suppress;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.Y60Action;
import com.artcom.y60.Y60ActivityInstrumentationTest;
import com.artcom.y60.Y60TestActivity;

public class GomNotificationHelperIntegrationTest extends
        Y60ActivityInstrumentationTest<Y60TestActivity> {

    private static final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/gom_notification_helper_integration_test";

    public GomNotificationHelperIntegrationTest() {

        super("com.artcom.y60", Y60TestActivity.class);
    }

    public void setUp() {

        getActivity().startService(new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER));
    }

    @Suppress
    public void testRegExpConstraintOnObserver() throws Exception {

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
        BroadcastReceiver receiver = GomNotificationHelper.registerObserver(observedPath, observer);
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
