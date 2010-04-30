package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;

import com.artcom.y60.data.StreamableContent;

public class ThrowEvent extends ShareEvent {

    private static final String LOG_TAG = "ShareEvent";

    ThrowEvent(HocLocation pLocation, StreamableContent pOutgoingData,
            DefaultHttpClient pHttpClient) {
        super(pLocation, pOutgoingData, pHttpClient);
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepOut");
        return eventParams;
    }

}
