package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import com.artcom.y60.data.StreamableContent;

public class ThrowEvent extends ShareEvent {

    private static final String LOG_TAG = "ThrowEvent";

    ThrowEvent(HocLocation pLocation, StreamableContent pOutgoingData, Peer peer) {
        super(pLocation, pOutgoingData, peer);
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "Throw");
        return eventParams;
    }

}
