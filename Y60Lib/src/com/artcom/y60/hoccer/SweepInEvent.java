package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;

public class SweepInEvent extends HocEvent {
    
    SweepInEvent(HocLocation pLocation, DefaultHttpClient pHttpClient) {
        super(pLocation, pHttpClient);
    }
    
    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepIn");
        return eventParams;
    }
    
}
