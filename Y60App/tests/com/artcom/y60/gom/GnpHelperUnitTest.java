package com.artcom.y60.gom;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.artcom.y60.Constants;
import com.artcom.y60.Logger;

public class GnpHelperUnitTest extends TestCase {

    private final String LOG_TAG = "GnpHelperUnitTest";

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

}
