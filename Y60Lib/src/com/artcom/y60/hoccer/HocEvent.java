package com.artcom.y60.hoccer;

public class HocEvent {
    
    private static String mRemoteServer     = "http://beta.hoccer.com";
    private String        mState            = "unborn";
    private double        mLifetime         = -1;
    private final String  mResourceLocation = null;
    
    public boolean isAlive() {
        return getLifetime() > 0;
    }
    
    public double getLifetime() {
        return mLifetime;
    }
    
    protected void setLiftime(double pLifetime) {
        mLifetime = pLifetime;
    }
    
    protected static String getRemoteServer() {
        return mRemoteServer;
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
}
