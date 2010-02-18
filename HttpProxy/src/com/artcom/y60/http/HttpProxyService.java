package com.artcom.y60.http;

import java.io.IOException;
import java.util.Map;

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

    private static final String LOG_TAG = "HttpProxyService";
    private Cache               cache;

    // Static Methods ----------------------------------------------------

    /**
     * Helper to notify about resource changes via intent broadcast.
     */
    void resourceAvailable(String pUri) {
        Intent intent = new Intent(HttpProxyConstants.RESOURCE_UPDATE_ACTION);
        intent.putExtra(HttpProxyConstants.URI_EXTRA, pUri);
        // Logger.v(LOG_TAG, "Broadcasting 'update' for resource " + pUri);
        sendBroadcast(intent);
    }

    void resourceNotAvailable(String pUri) {
        Intent intent = new Intent(HttpProxyConstants.RESOURCE_NOT_AVAILABLE_ACTION);
        intent.putExtra(HttpProxyConstants.URI_EXTRA, pUri);
        sendBroadcast(intent);
    }

    // Instance Variables ------------------------------------------------

    private HttpProxyRemote mRemote;
    private ResetReceiver   mResetReceiver;

    protected Map<String, Bundle> getCachedContent() {
        return cache.getCachedContent();
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        cache = new Cache(this);
        cache.activate();

        mRemote = new HttpProxyRemote();

        IntentFilter filter = new IntentFilter(Y60Action.RESET_BC_HTTP_PROXY);
        mResetReceiver = new ResetReceiver();
        registerReceiver(mResetReceiver, filter);

        super.onCreate();
    }

    @Override
    public void onStart(Intent pIntent, int startId) {

        Logger.v(LOG_TAG, "on start");

        sendBroadcast(new Intent(Y60Action.SERVICE_HTTP_PROXY_READY));
        Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ sent broadcast http proxy ready");

        super.onStart(pIntent, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mResetReceiver);
        cache.deactivate();
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
        Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ sent broadcast http proxy ready");

        return mRemote;
    }

    public void requestResource(String pUri) {
        cache.requestResource(pUri);
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
        sendBroadcast(new Intent(Y60Action.SERVICE_HTTP_PROXY_CLEARED));
        Logger.d(LOG_TAG, "HTTP cache cleared");
    }

    public long getNumberOfEntries() {
        return cache.getNumberOfEntries();
    }

    // Inner Classes -----------------------------------------------------

    class HttpProxyRemote extends IHttpProxyService.Stub {

        @Override
        public void requestResource(String pUri, RpcStatus status) {
            try {
                HttpProxyService.this.requestResource(pUri);
            } catch (Exception e) {
                status.setError(e);
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

        @Override
        public void clear(RpcStatus status) throws RemoteException {
            try {
                HttpProxyService.this.clear();
            } catch (Exception e) {
                status.setError(e);
            }

        }

        @Override
        public long getNumberOfEntries(RpcStatus status) throws RemoteException {
            try {
                return HttpProxyService.this.getNumberOfEntries();
            } catch (Exception e) {
                status.setError(e);
                return 0;
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
