package com.artcom.y60.hoccer;

import java.util.Map;

import org.apache.http.client.HttpClient;

import com.artcom.y60.http.AsyncHttpPost;
import com.artcom.y60.http.AsyncHttpRequest;

public abstract class HocEvent {
    
    private static String          mRemoteServer     = "http://beta.hoccer.com";
    private String                 mState            = "unborn";
    private double                 mLifetime         = -1;
    private final String           mResourceLocation = null;
    private final AsyncHttpRequest mStatusRequest;
    
    HocEvent(HttpClient pHttpClient) {
        AsyncHttpPost eventCreation = new AsyncHttpPost(getRemoteServer() + "/events", pHttpClient);
        eventCreation.setBody(getHttpParameters());
        eventCreation.start();
        mStatusRequest = eventCreation;
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
