package com.artcom.y60.hoccer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.http.AsyncHttpGet;
import com.artcom.y60.http.AsyncHttpPost;
import com.artcom.y60.http.AsyncHttpRequest;
import com.artcom.y60.http.HttpResponseHandler;

public abstract class HocEvent {

    private static final String         LOG_TAG          = "HocEvent";
    private static String               mRemoteServer    = "http://beta.hoccer.com";
    private String                      mState           = "unborn";
    private double                      mLifetime        = -1;
    private int                         mLinkedPeerCount = 0;
    private UUID                        mUuid            = null;

    private AsyncHttpRequest            mStatusFetcher;
    private ArrayList<HocEventListener> mCallbackList;
    private String                      mMessage;

    HocEvent(HocLocation pLocation, DefaultHttpClient pHttpClient) {
        Logger.v(LOG_TAG, "creating new hoc event");

        mUuid = UUID.randomUUID();

        AsyncHttpPost eventCreation = new AsyncHttpPost(getRemoteServer() + "/events", pHttpClient);
        eventCreation.setAcceptedMimeType("application/json");

        Map<String, String> parameters = getEventParameters();
        parameters.put("event[latitude]", Double.toString(pLocation.getLatitude()));
        parameters.put("event[longitude]", Double.toString(pLocation.getLongitude()));
        eventCreation.setBody(parameters);
        eventCreation.registerResponseHandler(createResponseHandler());
        eventCreation.start();
        mStatusFetcher = eventCreation;

        mCallbackList = new ArrayList<HocEventListener>();
    }

    public void registerCallback(HocEventListener pListener) {
        Logger.v(LOG_TAG, "addHoccerUploadListener");
        mCallbackList.add(pListener);
    }

    /**
     * Must be implemented by derived classes to define the key-value pairs which should be send to
     * the server
     */
    protected abstract Map<String, String> getEventParameters();

    /**
     * @return true if lifetime is positive
     */
    public boolean isAlive() {
        return getLifetime() > 0;
    }

    /**
     * @return lifetime on the server; encodes as 'expires' in the hoccer protocol
     */
    public double getLifetime() {
        return mLifetime;
    }

    public String getMessage() {
        return mMessage;
    }

    protected void updateState(String newState) {

        if (mState.equals(newState)) {
            return;
        }

        mState = newState;
        if (mState.equals("ready")) {
            onLinkEstablished();
        } else if (mState.equals("collision")) {
            onError();
        } else if (mState.equals("no_link")) {
            onError();
        } else if (mState.equals("waiting")) {
            onProgress();
        }
    }

    public String getState() {
        return mState;
    }

    public UUID getUuid() {
        return mUuid;
    }

    public abstract StreamableContent getData();

    /**
     * @return uri to the event location
     */
    public String getResourceLocation() {
        return mStatusFetcher.getUri();
    }

    protected void setLiftime(double pLifetime) {
        mLifetime = pLifetime;
    }

    public int getLinkedPeerCount() {
        return mLinkedPeerCount;
    }

    protected void setLinkedPeerCount(int count) {
        mLinkedPeerCount = count;
    }

    protected static String getRemoteServer() {
        return mRemoteServer;
    }

    protected void updateStatusFromJson(JSONObject status) throws JSONException, IOException {

        if (status.has("state")) {
            updateState(status.getString("state"));
        }
        if (status.has("message")) {
            mMessage = status.getString("message");
        }
        if (status.has("expires")) {
            setLiftime(Double.parseDouble(status.getString("expires")));
        }
        if (status.has("peers")) {
            setLinkedPeerCount(Integer.parseInt(status.getString("peers")));
        }

        // notify about new status infos
        for (HocEventListener callback : mCallbackList) {
            callback.onProgress(mMessage);
        }
    }

    protected void onSuccess() {
        for (HocEventListener callback : mCallbackList) {
            callback.onSuccess(this);
        }
    };

    protected void onLinkEstablished() {
        for (HocEventListener callback : mCallbackList) {
            callback.onLinkEstablished();
        }
    };

    protected void onError() {
        for (HocEventListener callback : mCallbackList) {
            callback.onError(null, null);
        }
    };

    protected void onProgress() {
        for (HocEventListener callback : mCallbackList) {
            callback.onProgress("on progress");
        }
    };

    private HttpResponseHandler createResponseHandler() {
        return new HttpResponseHandler() {
            int mRequestDelay = 1;

            @Override
            public void onSuccess(int statusCode, StreamableContent body) {
                Logger.v(LOG_TAG, "onSuccess with body: ", body, " processServerResponse .. ");
                processServerResponse(body);
            }

            private void processServerResponse(StreamableContent body) {
                try {
                    updateStatusFromJson(new JSONObject(body.toString()));
                    launchNewPollingRequest();
                } catch (JSONException e) {
                    Logger.e(LOG_TAG, e);
                    mState = "json error";
                } catch (IOException e) {
                    Logger.e(LOG_TAG, e);
                    mState = "io error";
                }
            }

            /**
             * Creates a new asyncHttpGet request which replaces the old one. This reponseHandler
             * object is passed to the new request to enable fibonacci-based increasing of the delay
             * between polling requesets
             */
            private void launchNewPollingRequest() {
                try {
                    Thread.sleep(mRequestDelay * 1000);
                } catch (InterruptedException e) {
                    Logger.e(LOG_TAG, e);
                }
                mRequestDelay += mRequestDelay;
                mStatusFetcher = new AsyncHttpGet(mStatusFetcher.getUri());
                mStatusFetcher.registerResponseHandler(this);

                Logger.v(LOG_TAG, "launchNewPollingRequest");
                mStatusFetcher.start();
            }

            @Override
            public void onReceiving(double progress) {
                Logger.v(LOG_TAG, "onReceiving progress: ", progress);
            }

            @Override
            public void onError(int statusCode, StreamableContent body) {
                Logger.e(LOG_TAG, "onError: ", body, " with status code: ", statusCode);
                processServerResponse(body);
            }

            @Override
            public void onHeaderAvailable(Header[] headers) {
            }
        };
    }

    public boolean hasCollision() {
        return mState.equals("collision");
    }

}