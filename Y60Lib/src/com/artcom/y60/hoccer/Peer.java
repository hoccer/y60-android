package com.artcom.y60.hoccer;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import com.artcom.y60.Logger;
import com.artcom.y60.data.DataContainerFactory;
import com.artcom.y60.data.DefaultDataContainerFactory;
import com.artcom.y60.data.StreamableContent;

public class Peer {
    
    private static final String  LOG_TAG = "Peer";
    
    DefaultHttpClient            mHttpClient;
    private HocLocation          mHocLocation;
    private DataContainerFactory mDataContainerFactory;
    
    public Peer(String clientName) {
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        mHttpClient = new DefaultHttpClient(httpParams);
        mHttpClient.getParams().setParameter("http.useragent", clientName);
        mDataContainerFactory = new DefaultDataContainerFactory();
    }
    
    public SweepOutEvent sweepOut(StreamableContent pStreamableData) {
        return new SweepOutEvent(mHocLocation, pStreamableData, mHttpClient);
    }
    
    public SweepInEvent sweepIn() {
        return new SweepInEvent(mHocLocation, mHttpClient, this);
    }
    
    public ThrowEvent throwIt(StreamableContent pStreamableData) {
        return new ThrowEvent(mHocLocation, pStreamableData, mHttpClient);
    }
    
    public DropEvent drop(StreamableContent pStreamableData) {
        return new DropEvent(mHocLocation, pStreamableData, mHttpClient);
    }
    
    public void setLocation(HocLocation pLocation) {
        mHocLocation = pLocation;
        Logger.v(LOG_TAG, mHocLocation);
    }
    
    public void setDataContainerFactory(DataContainerFactory pDataContainerFactory) {
        mDataContainerFactory = pDataContainerFactory;
    }
    
    public DataContainerFactory getContentFactory() {
        return mDataContainerFactory;
    }
    
    public CatchEvent catchIt() {
        return new CatchEvent(mHocLocation, mHttpClient, this);
    }
}
