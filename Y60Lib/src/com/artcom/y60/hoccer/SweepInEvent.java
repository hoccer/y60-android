package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;

public class SweepInEvent extends HocEvent {
    
    SweepInEvent(DefaultHttpClient pHttpClient) {
        super(pHttpClient);
    }
    
    @Override
    protected Map<String, String> getHttpParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepIn");
        eventParams.put("event[latitude]", "23");
        eventParams.put("event[longitude]", "34");
        return eventParams;
    }
    
}
