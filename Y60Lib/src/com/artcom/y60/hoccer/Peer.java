package com.artcom.y60.hoccer;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import com.artcom.y60.Logger;
import com.artcom.y60.data.ContentFactory;
import com.artcom.y60.data.DefaultStreamableContentFactory;
import com.artcom.y60.data.StreamableContent;

public class Peer {

    private static final String LOG_TAG = "Peer";

    DefaultHttpClient           mHttpClient;
    private HocLocation         mHocLocation;
    private ContentFactory      mContentFactory;

    public ContentFactory getContentFactory() {
        return mContentFactory;
    }

    public Peer(String clientName) {
        mHttpClient = new DefaultHttpClient(new BasicHttpParams());
        mHttpClient.getParams().setParameter("http.useragent", clientName);
        mContentFactory = new DefaultStreamableContentFactory();
    }

    public SweepOutEvent sweepOut(StreamableContent pStreamableData) {
        return new SweepOutEvent(mHocLocation, pStreamableData, mHttpClient);
    }

    public SweepInEvent sweepIn() {
        return new SweepInEvent(mHocLocation, mHttpClient, this);
    }

    public void setLocation(HocLocation pLocation) {
        mHocLocation = pLocation;
        Logger.v(LOG_TAG, mHocLocation);
    }
}
