package com.artcom.y60.hoccer;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

public class Peer {
    
    DefaultHttpClient mHttpClient;
    
    public Peer(String clientName) {
        mHttpClient = new DefaultHttpClient(new BasicHttpParams());
        mHttpClient.getParams().setParameter("http.useragent", clientName);
    }
    
    public HocEvent sweepOut() {
        return new SweepOutEvent(mHttpClient);
    }
    
    public HocEvent sweepIn() {
        return new SweepInEvent(mHttpClient);
    }
}
