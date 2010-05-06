package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

public class CatchEvent extends ReceiveEvent {

    private static final String LOG_TAG = "CatchEvent";

    CatchEvent(HocLocation pLocation, Peer peer) {
        super(pLocation, peer);
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "Catch");
        return eventParams;
    }

}
