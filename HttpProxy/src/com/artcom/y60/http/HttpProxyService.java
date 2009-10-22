package com.artcom.y60.http;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.Logger;
import com.artcom.y60.RpcStatus;
import com.artcom.y60.Y60Action;
import com.artcom.y60.Y60Service;

/**
 * Implementation of client-side caching for HTTP resources.
 * 
 * @author arne
 * @see HttpProxyHelper
 */
public class HttpProxyService extends Y60Service {

    // Constants ---------------------------------------------------------

    private static final String          LOG_TAG = "HttpProxyService";
    private static final Cache           CACHE;

    // Class Variables ---------------------------------------------------

    private static Set<HttpProxyService> sInstances;

    // Static Initializer ------------------------------------------------

    static {

        CACHE = new Cache();
        sInstances = new HashSet<HttpProxyService>();
    }

    // Static Methods ----------------------------------------------------

    /**
     * Helper to notify about resource changes via intent broadcast.
     */
    static void resourceUpdated(String pUri) {

        synchronized (sInstances) {

            if (sInstances.size() == 0) {

                // this must never happen!
                Logger.e(LOG_TAG, "Can't send broadcast since all services have died!");

            } else {
                HttpProxyService service = sInstances.iterator().next();
                Intent intent = new Intent(HttpProxyConstants.RESOURCE_UPDATE_ACTION);
                intent.putExtra(HttpProxyConstants.URI_EXTRA, pUri);
                Logger.v(LOG_TAG, "Broadcasting 'update' for resource " + pUri);
                service.sendBroadcast(intent);
            }
        }
    }

    /**
     * Helper to notify about resource changes via intent broadcast.
     */
    static void resourceNotAvailable(String pUri) {

        synchronized (sInstances) {

            if (sInstances.size() == 0) {

                // this must never happen!
                Logger.e(LOG_TAG, "Can't send broadcast since all services have died!");

            } else {
                HttpProxyService service = sInstances.iterator().next();
                Intent intent = new Intent(HttpProxyConstants.RESOURCE_NOT_AVAILABLE_ACTION);
                intent.putExtra(HttpProxyConstants.URI_EXTRA, pUri);
                Logger.v(LOG_TAG, "Broadcasting 'not available' for resource " + pUri);
                service.sendBroadcast(intent);
            }
        }
    }

    private static int countInstances() {

        synchronized (sInstances) {

            return sInstances.size();
        }
    }

    // Instance Variables ------------------------------------------------

    private HttpProxyRemote mRemote;

    private final String    mId;

    private ResetReceiver   mResetReceiver;

    // Constructors ------------------------------------------------------

    public HttpProxyService() {

        mId = String.valueOf(System.currentTimeMillis());
        Logger.v(tag(), "HttpProxyService instantiated");
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());

        Logger.i(tag(), "HttpProxyService.onCreate");

        super.onCreate();

        mRemote = new HttpProxyRemote();

        IntentFilter filter = new IntentFilter(Y60Action.RESET_BC);
        mResetReceiver = new ResetReceiver();
        registerReceiver(mResetReceiver, filter);

        synchronized (sInstances) {
            sInstances.add(this);
            CACHE.resume();
            Logger.v(tag(), "instances: ", countInstances());
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ send broadcast HTTP PROXY READY");
        sendBroadcast(new Intent(Y60Action.SERVICE_HTTP_PROXY_READY));
        // Toast.makeText(this, "HTTP PROXY is ready",
        // Toast.LENGTH_SHORT).show();
        Logger.i(tag(), "HttpProxyService.onStart");
        super.onStart(intent, startId);
        Logger.d(tag(), "instances: " + countInstances());

    }

    @Override
    public void onDestroy() {

        Logger.i(tag(), "HttpProxyService.onDestroy");

        synchronized (sInstances) {
            sInstances.remove(this);

            if (sInstances.size() < 1) {
                CACHE.stop();
            }
            Logger.d(tag(), "instances: " + countInstances());
        }

        unregisterReceiver(mResetReceiver);

        clear();

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent pIntent) {

        return mRemote;
    }

    public Bundle get(String pUri) {

        return CACHE.get(pUri);
    }

    public Bundle getDataSyncronously(String pUri) throws HttpClientException, HttpServerException,
            IOException {
        return CACHE.getDataSyncronously(pUri);
    }

    public Bundle fetchFromCache(String pUri) {

        return CACHE.fetchFromCache(pUri);
    }

    public boolean isInCache(String pUri) {

        return CACHE.isInCache(pUri);
    }

    public void removeFromCache(String pUri) {
        CACHE.remove(pUri);
    }

    // Private Instance Methods ------------------------------------------

    private String tag() {

        return LOG_TAG + "[instance " + mId + "]";
    }

    private void clear() {

        Logger.d(LOG_TAG, "clearing HTTP cache");
        CACHE.clear();
        Logger.d(LOG_TAG, "HTTP cache cleared");
    }

    // Inner Classes -----------------------------------------------------

    class HttpProxyRemote extends IHttpProxyService.Stub {

        @Override
        public Bundle get(String pUri, RpcStatus status) {
            try {
                return HttpProxyService.this.get(pUri);
            } catch (Exception e) {
                status.setError(e);
                return null;
            }
        }

        @Override
        public Bundle fetchFromCache(String pUri, RpcStatus status) {
            try {
                return HttpProxyService.this.fetchFromCache(pUri);
            } catch (Exception e) {
                status.setError(e);
                return null;
            }
        }

        @Override
        public boolean isInCache(String pUri, RpcStatus status) throws RemoteException {
            try {
                return HttpProxyService.this.isInCache(pUri);
            } catch (Exception e) {
                status.setError(e);
                return false;
            }
        }

        @Override
        public void removeFromCache(String pUri, RpcStatus status) throws RemoteException {
            try {
                HttpProxyService.this.removeFromCache(pUri);
            } catch (Exception e) {
                status.setError(e);
            }
        }

        @Override
        public Bundle getDataSyncronously(String pUri, RpcStatus pStatus) throws RemoteException {
            try {
                return HttpProxyService.this.getDataSyncronously(pUri);
            } catch (Exception e) {
                pStatus.setError(e);
                return null;
            }
        }
    }

    class ResetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context pContext, Intent pIntent) {

            HttpProxyService.this.clear();
        }
    }

}
