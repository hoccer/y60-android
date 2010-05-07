package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import com.artcom.y60.data.StreamableContent;

public class SweepOutEvent extends ShareEvent {

    private static final String LOG_TAG = "SweepOutEvent";

    SweepOutEvent(HocLocation pLocation, StreamableContent pOutgoingData, Peer peer) {
        super(pOutgoingData, peer);
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepOut");
        return eventParams;
    }

}
