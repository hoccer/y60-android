package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class PickEvent extends ReceiveEvent {
    private static String LOG_TAG = "PickEvent";

    PickEvent(HocLocation pLocation, Peer peer) {
        super(pLocation, peer);
    }

    @Override
    protected void onPossibleDownloadsAvailable(JSONArray uris) {
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "Pick");
        return eventParams;
    }

}
