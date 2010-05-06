package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import com.artcom.y60.data.StreamableContent;

public class DropEvent extends ShareEvent {

    private static final String LOG_TAG = "DropEvent";
    private final long          mLifetime;

    DropEvent(StreamableContent pOutgoingData, long lifetime, Peer peer) {
        super(pOutgoingData, peer);
        mLifetime = lifetime;
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "Drop");
        eventParams.put("event[lifetime]", String.valueOf(mLifetime));
        return eventParams;
    }
}
