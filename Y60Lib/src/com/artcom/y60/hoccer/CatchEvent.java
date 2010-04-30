package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;

public class CatchEvent extends ReceiveEvent {

    private static final String LOG_TAG = "CatchEvent";

    CatchEvent(HocLocation pLocation, DefaultHttpClient pHttpClient, Peer peer) {
        super(pLocation, pHttpClient, peer);
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "Catch");
        return eventParams;
    }

}
