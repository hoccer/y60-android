package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import com.artcom.y60.Logger;
import com.artcom.y60.data.StreamableContent;

public class DropEvent extends ShareEvent {

    private static final String LOG_TAG = "DropEvent";
    private final long          mAimedLifetime;
    private long                mCreationTime;

    DropEvent(StreamableContent pOutgoingData, long lifetime, Peer peer)
            throws UnknownLocationException {
        super(pOutgoingData, peer);
        mAimedLifetime = lifetime;
        mCreationTime = System.currentTimeMillis();
    }

    @Override
    protected void onLinkEstablished() {
        mCreationTime = System.currentTimeMillis();
        super.onLinkEstablished();
    }

    @Override
    public double getRemainingLifetime() {

        // if we are still polling take the lifetime from server
        if (mStatusFetcher != null) {
            return super.getRemainingLifetime();
        }

        // if not polling, use a self computed lifetime
        return mAimedLifetime - (System.currentTimeMillis() - mCreationTime);
    }

    @Override
    /**
     * Slightly modified success method to stop polling before the drop event gets scruffy
     */
    public boolean wasSuccessful() {
        Logger.v(LOG_TAG, "checking if successful: ", getState(), " ", hasDataBeenUploaded());
        return getState().equals("ready") && hasDataBeenUploaded();
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "Drop");
        eventParams.put("event[lifetime]", String.valueOf(mAimedLifetime));
        return eventParams;
    }
}
