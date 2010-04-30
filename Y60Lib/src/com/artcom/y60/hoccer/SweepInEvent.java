package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;

public class SweepInEvent extends ReceiveEvent {

    private static String LOG_TAG = "SweepInEvent";

    SweepInEvent(HocLocation pLocation, DefaultHttpClient pHttpClient, Peer pPeer) {
        super(pLocation, pHttpClient, pPeer);
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepIn");
        return eventParams;
    }

}
