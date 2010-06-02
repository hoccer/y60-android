package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.artcom.y60.IpAddressNotFoundException;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.data.DataContainerFactory;
import com.artcom.y60.data.DefaultDataContainerFactory;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.error.ErrorReporter;

public class Peer {

    public static class Parameter {
        public static final String LATITUDE          = "latitude";
        public static final String LONGITUDE         = "longitude";
        public static final String LOCATION_ACCURACY = "location_accuracy";
        public static final String BSSIDS            = "bssids";
        public static final String NETWORK_TYPE      = "network_type";
        public static final String NETWORK_OPERATOR  = "network_operator";
        public static final String BRAND             = "brand";
        public static final String DEVICE            = "device";
        public static final String MANUFACTURER      = "manufacturer";
        public static final String MODEL             = "model";
        public static final String VERSION_SDK       = "version_sdk";
        public static final String LOCAL_IP          = "local_ip";
        public static final String TIMESTAMP         = "timestamp";
        public static final String UUID              = "uuid";
    }

    private static final String  LOG_TAG  = "Peer";
    private final String         mRemoteServer;

    DefaultHttpClient            mHttpClient;
    private HocLocation          mHocLocation;
    private ErrorReporter        mErrorReporter;
    private DataContainerFactory mDataContainerFactory;
    private Context              mContext = null;

    public Peer(String clientName, String remoteServer) {
        mRemoteServer = remoteServer;
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
        ConnManagerParams.setMaxTotalConnections(httpParams, 100);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        mHttpClient = new DefaultHttpClient(cm, httpParams);
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

    public void setContextForMeaningfulParams(Context context) {
        mContext = context;
    }

    public Map<String, String> getEventParameters() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("event[" + Parameter.LATITUDE + "]", Double.toString(mHocLocation
                .getLatitude()));
        parameters.put("event[" + Parameter.LONGITUDE + "]", Double.toString(mHocLocation
                .getLongitude()));
        parameters.put("event[" + Parameter.LOCATION_ACCURACY + "]", Double.toString(mHocLocation
                .getAccuracy()));
        parameters.put("event[" + Parameter.BSSIDS + "]", getAccessPointSightings());

        parameters.put("event[" + Parameter.BRAND + "]", Build.BRAND);
        parameters.put("event[" + Parameter.DEVICE + "]", Build.DEVICE);
        parameters.put("event[" + Parameter.MANUFACTURER + "]", Build.MANUFACTURER);
        parameters.put("event[" + Parameter.MODEL + "]", Build.MODEL);
        parameters.put("event[" + Parameter.VERSION_SDK + "]", String
                .valueOf(Build.VERSION.SDK_INT));
        parameters.put("event[" + Parameter.TIMESTAMP + "]", String.valueOf(System
                .currentTimeMillis()));

        try {
            parameters.put("event[" + Parameter.LOCAL_IP + "]", NetworkHelper.getDeviceIpAddress());
        } catch (IpAddressNotFoundException e) {
            Logger.e(LOG_TAG, e.toString());
        }

        if (mContext != null) {
            parameters.put("event[" + Parameter.UUID + "]", getUUIDFromSharedPreferences(mContext));

            TelephonyManager telephonyManager = (TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            parameters.put("event[" + Parameter.NETWORK_TYPE + "]", NetworkHelper
                    .getNetworkType(telephonyManager));
            parameters.put("event[" + Parameter.NETWORK_OPERATOR + "]", telephonyManager
                    .getNetworkOperatorName());
        }
        Logger.v(LOG_TAG, parameters);
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

    private String getUUIDFromSharedPreferences(Context pContext) {
        SharedPreferences prefs = pContext.getSharedPreferences("hoccer", Context.MODE_PRIVATE);

        String tmpUUID = UUID.randomUUID().toString();
        String storedUUID = prefs.getString("uuid", tmpUUID);

        if (tmpUUID.equals(storedUUID)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("uuid", tmpUUID);
            editor.commit();
        }
        return storedUUID;
    }
}
