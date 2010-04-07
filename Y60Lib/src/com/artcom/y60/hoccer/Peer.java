package com.artcom.y60.hoccer;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class Peer {
    
    DefaultHttpClient mHttpClient;
    
    public Peer(String clientName) {
        HttpParams httpParams = new BasicHttpParams();
        // HttpClientParams.setRedirecting(httpParams, false);
        mHttpClient = new DefaultHttpClient(httpParams);
        mHttpClient.getParams().setParameter("http.useragent", clientName);
    }
    
    public HocEvent sweepOut() {
        return new SweepOutEvent(mHttpClient);
    }
    
}
