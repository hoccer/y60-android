package com.artcom.y60.hoccer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.ReflectionHelper;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.http.AsyncHttpGet;
import com.artcom.y60.http.AsyncHttpPost;
import com.artcom.y60.http.AsyncHttpRequest;
import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpHelper;
import com.artcom.y60.http.HttpResponseHandler;
import com.artcom.y60.http.HttpServerException;

public abstract class HocEvent {

    private static final String               LOG_TAG             = "HocEvent";
    String                                    mState              = "unborn";
    private double                            mRemainingLifetime  = -1;
    private int                               mLinkedPeerCount    = 0;
    private UUID                              mUuid               = null;

    AsyncHttpRequest                          mStatusFetcher;
    private final ArrayList<HocEventListener> mCallbackList;
    private String                            mMessage;
    private final int                         mStatusPollingDelay = 1;
    private final Peer                        mPeer;
    private String                            mResourceLocation;

    private boolean                           isAborted           = false;
    private boolean                           isReady             = false;

    HocEvent(Peer peer) {
        mUuid = UUID.randomUUID();
        mPeer = peer;
        mCallbackList = new ArrayList<HocEventListener>();

        // The post-to-server action is done in a thread to make sure it's called AFTER the Event
        // object is constructed. This was the only way to hide all "start" logic
        // in the constructor (without doing a hocEvent.start() or similar), and enable sub-classes
        // to be parameterizable by overwriting getEventParameters().
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                postEventToServer();
            }
        }.start();
    }

    private void postEventToServer() {

        AsyncHttpPost eventCreation = new AsyncHttpPost(getPeer().getRemoteServer() + "/events",
                mPeer.getHttpClient());
        eventCreation.setAcceptedMimeType("application/json");

        Map<String, String> parameters = getEventParameters();

        parameters.putAll(mPeer.getEventDnaParameters());
        parameters.putAll(mPeer.getEventParameters());

        eventCreation.setBody(parameters);
        eventCreation.registerResponseHandler(createResponseHandler());
        eventCreation.setUncaughtExceptionHandler(getPeer().getErrorReporter());

        mStatusFetcher = eventCreation;

        eventCreation.start();

        mMessage = "Connecting";
        onFeedback();
    }

    protected Peer getPeer() {
        return mPeer;
    }

    public void addCallback(HocEventListener pListener) {
        synchronized (mCallbackList) {
            mCallbackList.add(pListener);
        }
    }

    public void removeCallback(HocEventListener hocEventListener) {
        synchronized (mCallbackList) {
            mCallbackList.remove(hocEventListener);
        }
    }

    /**
     * Must be implemented by derived classes to define the key-value pairs which should be send to
     * the server
     */
    protected abstract Map<String, String> getEventParameters();

    /**
     * @return true if lifetime is positive
     */
    public boolean isOpenForLinking() {
        return (!mState.equals("unborn")) && getRemainingLifetime() > 0;
    }

    /**
     * @return lifetime on the server; encodes as 'expires' in the hoccer protocol
     */
    public double getRemainingLifetime() {
        return mRemainingLifetime;
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
            onError(new HocEventException(getMessage(), mState, getResourceLocation()));
        } else if (mState.equals("no_link")) {
            onError(new HocEventException(getMessage(), mState, getResourceLocation()));
        } else if (mState.equals("no_peers")) {
            onError(new HocEventException(getMessage(), mState, getResourceLocation()));
        } else if (mState.equals("no_seeders")) {
            onError(new HocEventException(getMessage(), mState, getResourceLocation()));
        } else if (mState.equals("waiting")) {
            onFeedback();
        } else {
            onError(new HocEventException(getMessage(), mState, getResourceLocation()));
        }
    }

    public boolean hasError() {
        return !(mState.equals("waiting") || mState.equals("ready") || mState.equals("unborn"));
    }

    /**
     * @return true if event is 'ready' and has found an peer
     */
    public boolean isLinkEstablished() {
        return mState.equals("ready") && mLinkedPeerCount > 0;
    }

    public boolean hasCollision() {
        return mState.equals("collision");
    }

    public UUID getUuid() {
        return mUuid;
    }

    public abstract StreamableContent getData();

    /**
     * @return uri to the event location
     */
    public String getResourceLocation() {
        return mResourceLocation;
    }

    protected void setRemainingLifetime(double expires) {
        mRemainingLifetime = expires;
    }

    public int getLinkedPeerCount() {
        return mLinkedPeerCount;
    }

    protected void setLinkedPeerCount(int count) {
        mLinkedPeerCount = count;
    }

    protected void updateStatusFromJson(JSONObject status) throws JSONException, IOException {
        Logger.v(LOG_TAG, "updating status to ", status);
        if (status.has("message")) {
            mMessage = status.getString("message");
        }

        if (status.has("expires")) {
            setRemainingLifetime(Double.parseDouble(status.getString("expires")));
        }
        if (status.has("peers")) {
            setLinkedPeerCount(Integer.parseInt(status.getString("peers")));
        }

        if (status.has("state")) {
            updateState(status.getString("state"));
        }

        // notify about new status infos
        if (isReady || isAborted) {
            return;
        }

        synchronized (mCallbackList) {
            for (HocEventListener callback : mCallbackList) {
                callback.onFeedback(mMessage);
            }
        }
    }

    public boolean wasSuccessful() {
        return isLinkEstablished();
    }

    protected void tryForSuccess() {

        Logger.v(LOG_TAG, "try for success");

        if (!wasSuccessful()) {
            return;
        }

        isReady = true;
        stopPolling();

        Logger.v(LOG_TAG, ReflectionHelper.callingMethodName());

        synchronized (mCallbackList) {
            for (HocEventListener callback : mCallbackList) {
                Logger.v(LOG_TAG, "try for success ", callback, " size: ", mCallbackList.size());
                callback.onDataExchanged(this);
            }
        }
    };

    private void stopPolling() {
        if (mStatusFetcher == null) {
            return;
        }
        mStatusFetcher.removeResponseHandler();
        mStatusFetcher = null;
    }

    protected void onLinkEstablished() {
        synchronized (mCallbackList) {
            for (HocEventListener callback : mCallbackList) {
                callback.onLinkEstablished();
            }
        }
        tryForSuccess();
    };

    protected void onTransferProgress(double progress) {
        synchronized (mCallbackList) {
            for (HocEventListener callback : mCallbackList) {
                callback.onTransferProgress(progress);
            }
        }
    };

    protected void onAbort() {
        synchronized (mCallbackList) {
            for (HocEventListener callback : mCallbackList) {
                callback.onAbort(this);
            }
        }
    };

    protected void onError(HocEventException e) {

        stopPolling();

        if (isAborted) {
            return;
        }

        isReady = true;
        synchronized (mCallbackList) {
            // copying list, so some callback listener can remove themself without a concurrent
            // modification exception.
            ArrayList<HocEventListener> temporaryCallbackList = new ArrayList<HocEventListener>(
                    mCallbackList);
            for (HocEventListener callback : temporaryCallbackList) {
                callback.onError(e);
            }
        }
    };

    protected void onFeedback() {
        synchronized (mCallbackList) {
            for (HocEventListener callback : mCallbackList) {
                callback.onFeedback(mMessage);
            }
        }
    };

    private HttpResponseHandler createResponseHandler() {
        return new HttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, StreamableContent body) {
                processServerResponse(body);
            }

            private void processServerResponse(StreamableContent body) {
                if (mStatusFetcher == null)
                    return;
                try {
                    mResourceLocation = mStatusFetcher.getUri();
                    Logger.v(LOG_TAG, "rtt: ", mStatusFetcher.getRtt(), " http request is: ",
                            mStatusFetcher.getClass());
                    updateStatusFromJson(new JSONObject(body.toString()));
                    launchNewPollingRequest();
                } catch (JSONException e) {
                    getPeer().getErrorReporter().notify(LOG_TAG, e);
                    updateState("json error");
                } catch (IOException e) {
                    getPeer().getErrorReporter().notify(LOG_TAG, e);
                    updateState("io error");
                } catch (NullPointerException e) {
                    getPeer().getErrorReporter().notify(LOG_TAG, e);
                    updateState("empty");
                    mMessage = "empty response from server";
                }
            }

            /**
             * Creates a new asyncHttpGet request which replaces the old one. This reponseHandler
             * object is passed to the new request to enable
             */
            private void launchNewPollingRequest() {
                try {
                    Thread.sleep(mStatusPollingDelay * 1000);
                    if (mStatusFetcher == null) {
                        return; // we dont want no more polling
                    }
                } catch (InterruptedException e) {
                    Logger.e(LOG_TAG, e);
                    if (mStatusFetcher == null) {
                        return; // we dont want no more polling
                    }
                }
                // mStatusPollingDelay += mStatusPollingDelay;
                long tmpRtt = mStatusFetcher.getRtt();
                mStatusFetcher = new AsyncHttpGet(mStatusFetcher.getUri(), mPeer.getHttpClient());
                mStatusFetcher.addAdditionalHeaderParam("X-Rtt", String.valueOf(tmpRtt));
                mStatusFetcher.addAdditionalHeaderParam("X-Client-Uuid", mPeer.getClientUuid());
                mStatusFetcher.registerResponseHandler(this);
                mStatusFetcher.setUncaughtExceptionHandler(getPeer().getErrorReporter());

                Logger.v(LOG_TAG, mStatusFetcher.getRequestHeaders(), " http request is: ",
                        mStatusFetcher.getClass());
                mStatusFetcher.start();
            }

            @Override
            public void onReceiving(double progress) {
            }

            @Override
            public void onError(int statusCode, StreamableContent body) {
                Logger.e(LOG_TAG, "onError: ", body, " with status code: ", statusCode);
                processServerResponse(body);
            }

            @Override
            public void onError(Exception e) {
                mState = "connection_error";
                HocEvent.this.onError(new HocEventException(e));
            }

            @Override
            public void onHeaderAvailable(HashMap<String, String> headers) {
            }

        };
    }

    public void abort() throws HocEventException {
        isAborted = true;
        Logger.v(LOG_TAG, "aborting event ", mResourceLocation);
        try {
            if (mStatusFetcher != null) {
                mStatusFetcher.interrupt();

                if (mResourceLocation != null) {
                    HttpHelper.delete(mResourceLocation);
                }
            }
        } catch (HttpClientException e) {
            throw new HocEventException(e);
        } catch (HttpServerException e) {
            Logger.e(LOG_TAG, e);
        } catch (IOException e) {
            Logger.e(LOG_TAG, e);
        } finally {
            mStatusFetcher = null;
        }
        onAbort();
    }

}
