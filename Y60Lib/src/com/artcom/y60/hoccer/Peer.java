package com.artcom.y60.hoccer;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import com.artcom.y60.Logger;

public class Peer {
    
    private static final String LOG_TAG = "Peer";
    
    DefaultHttpClient           mHttpClient;
    private HocLocation         mHocLocation;
    
    public Peer(String clientName) {
        mHttpClient = new DefaultHttpClient(new BasicHttpParams());
        mHttpClient.getParams().setParameter("http.useragent", clientName);
    }
    
    public SweepOutEvent sweepOut() {
        return new SweepOutEvent(mHocLocation, mHttpClient);
    }
    
    public SweepInEvent sweepIn() {
        return new SweepInEvent(mHocLocation, mHttpClient);
    }
    
    public void setLocation(HocLocation pLocation) {
        mHocLocation = pLocation;
        Logger.v(LOG_TAG, mHocLocation);
    }
}
