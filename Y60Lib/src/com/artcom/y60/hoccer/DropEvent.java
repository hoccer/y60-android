package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;

import com.artcom.y60.data.StreamableContent;

public class DropEvent extends ShareEvent {
    
    private static final String LOG_TAG = "DropEvent";
    
    DropEvent(HocLocation pLocation, StreamableContent pOutgoingData, DefaultHttpClient pHttpClient) {
        super(pLocation, pOutgoingData, pHttpClient);
    }
    
    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "Drop");
        eventParams.put("event[lifetime]", "30");
        return eventParams;
    }
}
