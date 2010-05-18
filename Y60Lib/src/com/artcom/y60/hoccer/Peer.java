package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import com.artcom.y60.Logger;
import com.artcom.y60.data.DataContainerFactory;
import com.artcom.y60.data.DefaultDataContainerFactory;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.error.ErrorReporter;

public class Peer {

    private static final String  LOG_TAG = "Peer";
    private final String         mRemoteServer;

    DefaultHttpClient            mHttpClient;
    private HocLocation          mHocLocation;
    private ErrorReporter        mErrorReporter;
    private DataContainerFactory mDataContainerFactory;

    public Peer(String clientName, String remoteServer) {
        mRemoteServer = remoteServer;
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        mHttpClient = new DefaultHttpClient(httpParams);
        mHttpClient.getParams().setParameter("http.useragent", clientName);
        mDataContainerFactory = new DefaultDataContainerFactory();
    }

    public void setErrorReporter(ErrorReporter reporter) {
        mErrorReporter = reporter;
    }

    public ErrorReporter getErrorReporter() {
        return mErrorReporter;
    }

    public SweepOutEvent sweepOut(StreamableContent pStreamableData) {
        return new SweepOutEvent(mHocLocation, pStreamableData, this);
    }

    public SweepInEvent sweepIn() {
        return new SweepInEvent(this);
    }

    public ThrowEvent throwIt(StreamableContent pStreamableData) {
        return new ThrowEvent(pStreamableData, this);
    }

    public DropEvent drop(StreamableContent pStreamableData, long lifetime) {
        return new DropEvent(pStreamableData, lifetime, this);
    }

    public PickEvent pick() {
        return new PickEvent(this);
    }

    public void setLocation(HocLocation pLocation) {
        mHocLocation = pLocation;
        Logger.v(LOG_TAG, "new hoc location is", mHocLocation);
    }

    public void setDataContainerFactory(DataContainerFactory pDataContainerFactory) {
        mDataContainerFactory = pDataContainerFactory;
    }

    public DataContainerFactory getContentFactory() {
        return mDataContainerFactory;
    }

    public CatchEvent catchIt() {
        return new CatchEvent(this);
    }

    public DefaultHttpClient getHttpClient() {
        return mHttpClient;
    }

    public Map<String, String> getEventParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("event[latitude]", Double.toString(mHocLocation.getLatitude()));
        parameters.put("event[longitude]", Double.toString(mHocLocation.getLongitude()));
        parameters.put("event[location_accuracy]", Double.toString(mHocLocation.getAccuracy()));
        parameters.put("event[bssids]", getAccessPointSightings());
        return parameters;
    }

    private String getAccessPointSightings() {
        if (mHocLocation.getScanResults() == null) {
            return "";
        }
        StringBuffer sightings = new StringBuffer();
        Iterator<AccessPointSighting> iter = mHocLocation.getScanResults().iterator();
        while (iter.hasNext()) {
            sightings.append(iter.next().bssid);
            if (iter.hasNext()) {
                sightings.append(",");
            }
        }
        return sightings.toString();
    }

    public String getRemoteServer() {
        return mRemoteServer;
    }
}
