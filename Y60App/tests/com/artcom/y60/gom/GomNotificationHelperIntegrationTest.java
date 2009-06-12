package com.artcom.y60.gom;

import android.content.Intent;

import com.artcom.y60.Y60Action;

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

}
