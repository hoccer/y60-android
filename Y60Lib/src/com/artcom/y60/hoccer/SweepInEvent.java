package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

public class SweepInEvent extends ReceiveEvent {

    private static String LOG_TAG = "SweepInEvent";

    SweepInEvent(Peer peer) throws UnknownLocationException {
        super(peer);
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepIn");
        return eventParams;
    }

}
