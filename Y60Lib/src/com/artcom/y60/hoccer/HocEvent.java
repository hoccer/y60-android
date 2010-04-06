package com.artcom.y60.hoccer;

import java.io.OutputStream;
import java.util.Map;

import org.apache.http.client.HttpClient;

import com.artcom.y60.Logger;
import com.artcom.y60.http.AsyncHttpPost;
import com.artcom.y60.http.AsyncHttpRequest;
import com.artcom.y60.http.HttpResponseHandler;

public abstract class HocEvent {
    
    private static final String    LOG_TAG           = "HocEvent";
    private static String          mRemoteServer     = "http://beta.hoccer.com";
    private String                 mState            = "unborn";
    private double                 mLifetime         = -1;
    private final String           mResourceLocation = null;
    private final AsyncHttpRequest mStatusPollingRequest;
    
    HocEvent(HttpClient pHttpClient) {
        Logger.v(LOG_TAG, "creating new hoc event");
        AsyncHttpPost eventCreation = new AsyncHttpPost(getRemoteServer() + "/events", pHttpClient);
        eventCreation.setAcceptedMimeType("application/json");
        eventCreation.setBody(getHttpParameters());
        eventCreation.registerResponseHandler(new HttpResponseHandler() {
            
            @Override
            public void onSuccess(int statusCode, OutputStream body) {
                Logger.v(LOG_TAG, body);
            }
            
            @Override
            public void onReceiving(double progress) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onError(int statusCode, OutputStream body) {
                Logger.e(LOG_TAG, body);
            }
            
            @Override
            public void onConnecting() {
                // TODO Auto-generated method stub
                
            }
        });
        eventCreation.start();
        mStatusPollingRequest = eventCreation;
    }
    
    protected abstract Map<String, String> getHttpParameters();
    
    public boolean isAlive() {
        return getLifetime() > 0;
    }
    
    public double getLifetime() {
        return mLifetime;
    }
    
    protected void setState(String pStatus) {
        mState = pStatus;
    }
    
    public String getState() {
        return mState;
    }
    
    public String getResourceLocation() {
        return mResourceLocation;
    }
    
    protected void setLiftime(double pLifetime) {
        mLifetime = pLifetime;
    }
    
    protected static String getRemoteServer() {
        return mRemoteServer;
    }
    
}
