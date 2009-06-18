package com.artcom.y60;

import android.content.Intent;

public class Y60ActivityTest extends Y60ActivityInstrumentationTest<Y60TestActivity> {

    public Y60ActivityTest() {
        super("com.artcom.y60", Y60TestActivity.class);

    }

    public void testStartServiceTwice() throws Exception {
        Intent startIntent = new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER);

        getActivity().startService(startIntent);
        TestHelper.blockUntilWebServerIsRunning();
        assertEquals(404, HttpHelper.getStatusCode("http://localhost:4042"));

        getActivity().startService(startIntent);
        TestHelper.blockUntilWebServerIsRunning();
        assertEquals(404, HttpHelper.getStatusCode("http://localhost:4042"));

    }
}
