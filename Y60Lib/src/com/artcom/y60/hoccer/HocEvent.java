package com.artcom.y60.hoccer;

import org.apache.http.client.HttpClient;

public class HocEvent {
    
    private static String    mRemoteServer     = "http://beta.hoccer.com";
    private String           mState            = "unborn";
    private double           mLifetime         = -1;
    private final String     mResourceLocation = null;
    private final HttpClient mHttpClient;
    
    HocEvent(HttpClient pHttpClient) {
        mHttpClient = pHttpClient;
    }
    
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
    
    protected HttpClient getHttpClient() {
        return mHttpClient;
    }
    
    protected void setLiftime(double pLifetime) {
        mLifetime = pLifetime;
    }
    
    protected static String getRemoteServer() {
        return mRemoteServer;
    }
    
}
