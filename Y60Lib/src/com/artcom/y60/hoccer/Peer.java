package com.artcom.y60.hoccer;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

public class Peer {
    
    DefaultHttpClient mHttpClient;
    
    public Peer(String clientName) {
        mHttpClient = new DefaultHttpClient(new BasicHttpParams());
        mHttpClient.getParams().setParameter("http.useragent", clientName);
    }
    
    public SweepOutEvent sweepOut() {
        return new SweepOutEvent(mHttpClient);
    }
    
    public SweepInEvent sweepIn() {
        return new SweepInEvent(mHttpClient);
    }
}
