package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class PickEvent extends ReceiveEvent {
    private JSONArray mPieces;

    PickEvent(Peer peer) throws UnknownLocationException {
        super(peer);
    }

    @Override
    protected void onPossibleDownloadsAvailable(JSONArray pieces) {
        stopPolling();
        mPieces = pieces;
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "Pick");
        return eventParams;
    }

    public JSONArray getListOfPieces() {
        return mPieces;
    }

    @Override
    public void downloadDataFrom(String uri) {
        super.downloadDataFrom(uri);
    }

}
