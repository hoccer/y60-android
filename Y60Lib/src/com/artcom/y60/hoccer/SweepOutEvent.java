package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class SweepOutEvent extends HocEvent {
    
    private static final String LOG_TAG = "SweepOutEvent";
    
    SweepOutEvent(DefaultHttpClient pHttpClient) {
        super(pHttpClient);
    }
    
    @Override
    protected Map<String, String> getHttpParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepOut");
        eventParams.put("event[latitude]", "23");
        eventParams.put("event[longitude]", "34");
        return eventParams;
    }
    
    @Override
    protected void updateStatusFromJson(JSONObject status) throws JSONException {
        if (status.has("state")) {
            setState(status.getString("state"));
        }
        if (status.has("expires")) {
            setLiftime(Double.parseDouble(status.getString("expires")));
        }
    }
}
