package com.artcom.y60.http;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.RpcStatus;
import com.artcom.y60.Y60Action;
import com.artcom.y60.Y60Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Implementation of client-side caching for HTTP resources.
 * 
 * @author arne
 * @see HttpProxyHelper
 */
public class HttpProxyService extends Y60Service {

    // Constants ---------------------------------------------------------

    private static final String          LOG_TAG = "HttpProxyService";
    private Cache                        cache;

    // Class Variables ---------------------------------------------------

    private static Set<HttpProxyService> sInstances;

    // Static Initializer ------------------------------------------------

    static {
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
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        cache = new Cache();

        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());

        super.onCreate();

        mRemote = new HttpProxyRemote();

        IntentFilter filter = new IntentFilter(Y60Action.RESET_BC);
        mResetReceiver = new ResetReceiver();
        registerReceiver(mResetReceiver, filter);

        synchronized (sInstances) {
            sInstances.add(this);
            cache.resume();
        }
    }

    @Override
    public void onStart(Intent pIntent, int startId) {

        Logger.v(LOG_TAG, "on start, ", pIntent.hasExtra(IntentExtraKeys.IS_IN_INIT_CHAIN));

        sendBroadcast(new Intent(Y60Action.SERVICE_HTTP_PROXY_READY));

        super.onStart(pIntent, startId);
    }

    @Override
    public void onDestroy() {

        synchronized (sInstances) {
            sInstances.remove(this);

            if (sInstances.size() < 1) {
                cache.stop();
            }
        }

        unregisterReceiver(mResetReceiver);

        clear();

        super.onDestroy();
    }

    @Override
    protected void kill() {
        // do not kill me upon shutdown services bc
    }

    @Override
    public IBinder onBind(Intent pIntent) {
        sendBroadcast(new Intent(Y60Action.SERVICE_HTTP_PROXY_READY));

        return mRemote;
    }

    public Bundle get(String pUri) {

        return cache.get(pUri);
    }

    public Bundle getDataSyncronously(String pUri) throws HttpClientException, HttpServerException,
            IOException {
        return cache.getDataSyncronously(pUri);
    }

    public Bundle fetchFromCache(String pUri) {

        return cache.fetchFromCache(pUri);
    }

    public boolean isInCache(String pUri) {

        return cache.isInCache(pUri);
    }

    public void removeFromCache(String pUri) {
        cache.remove(pUri);
    }

    // Private Instance Methods ------------------------------------------

    private void clear() {

        Logger.d(LOG_TAG, "clearing HTTP cache");
        cache.clear();
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
