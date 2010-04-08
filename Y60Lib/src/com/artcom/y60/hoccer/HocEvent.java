package com.artcom.y60.hoccer;

import java.io.OutputStream;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.http.AsyncHttpGet;
import com.artcom.y60.http.AsyncHttpPost;
import com.artcom.y60.http.AsyncHttpRequest;
import com.artcom.y60.http.HttpResponseHandler;

public abstract class HocEvent {
    
    private static final String LOG_TAG       = "HocEvent";
    private static String       mRemoteServer = "http://beta.hoccer.com";
    private String              mState        = "unborn";
    private double              mLifetime     = -1;
    private AsyncHttpRequest    mStatusPollingRequest;
    
    HocEvent(DefaultHttpClient pHttpClient) {
        Logger.v(LOG_TAG, "creating new hoc event");
        AsyncHttpPost eventCreation = new AsyncHttpPost(getRemoteServer() + "/events", pHttpClient);
        eventCreation.setAcceptedMimeType("application/json");
        eventCreation.setBody(getHttpParameters());
        eventCreation.registerResponseHandler(createResponseHandler());
        eventCreation.start();
        mStatusPollingRequest = eventCreation;
    }
    
    /**
     * Must be implemented by derived classes to define the key-value pairs which should be send to
     * the server
     */
    protected abstract Map<String, String> getHttpParameters();
    
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
    
    protected void setState(String pStatus) {
        mState = pStatus;
    }
    
    public String getState() {
        return mState;
    }
    
    /**
     * @return uri to the event location
     */
    public String getResourceLocation() {
        return mStatusPollingRequest.getUri();
    }
    
    protected void setLiftime(double pLifetime) {
        mLifetime = pLifetime;
    }
    
    protected static String getRemoteServer() {
        return mRemoteServer;
    }
    
    protected void updateStatusFromJson(JSONObject status) throws JSONException {
        if (status.has("state")) {
            setState(status.getString("state"));
        }
        if (status.has("expires")) {
            setLiftime(Double.parseDouble(status.getString("expires")));
        }
    };
    
    private HttpResponseHandler createResponseHandler() {
        return new HttpResponseHandler() {
            int mRequestDelay = 1;
            
            @Override
            public void onSuccess(int statusCode, OutputStream body) {
                Logger.v(LOG_TAG, "success with data: ", body);
                try {
                    updateStatusFromJson(new JSONObject(body.toString()));
                    launchNewPollingRequest();
                } catch (JSONException e) {
                    Logger.e(LOG_TAG, e);
                    mState = "json error";
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
                mStatusPollingRequest = new AsyncHttpGet(mStatusPollingRequest.getUri());
                mStatusPollingRequest.registerResponseHandler(this);
                mStatusPollingRequest.start();
            }
            
            @Override
            public void onReceiving(double progress) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onError(int statusCode, OutputStream body) {
                Logger.e(LOG_TAG, body);
                Logger.v(LOG_TAG, "failure with data: ", body);
                try {
                    updateStatusFromJson(new JSONObject(body.toString()));
                    launchNewPollingRequest();
                } catch (JSONException e) {
                    Logger.e(LOG_TAG, e);
                    mState = "json error";
                }
            }
            
            @Override
            public void onConnecting() {
                // TODO Auto-generated method stub
                
            }
        };
    }
}
