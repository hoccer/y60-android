package com.artcom.y60.http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IoHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.ResourceDownloadHelper;
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
import android.app.Notification;
import android.app.PendingIntent;

/**
 * Implementation of client-side caching for HTTP resources.
 * 
 * @author arne
 * @see HttpProxyHelper
 */
public class HttpProxyService extends Y60Service {

    private static final String LOG_TAG        = "HttpProxyService";
    private final int           notificationId = 60;
    static final String         CACHE_DIR      = "/sdcard/HttpProxyCache/";
    private ResourceManager     mResourceManager;
    private Map<String, Bundle> mCachedContent = null;

    private HttpProxyRemote     mRemote;
    private ResetReceiver       mResetReceiver;

    @Override
    public void onCreate() {
        Notification notification = new Notification(0, LOG_TAG, System.currentTimeMillis());
        notification.setLatestEventInfo(this, LOG_TAG, "", PendingIntent.getBroadcast(this, 0, new Intent(), 0) );
        startForeground(notificationId,notification);

        File dir = new File(CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            Logger.e(LOG_TAG, "creating dir " + dir.toString());
        }
        mCachedContent = new HashMap<String, Bundle>();

        mResourceManager = new ResourceManager(this);
        mResourceManager.activate();

        mRemote = new HttpProxyRemote();

        IntentFilter filter = new IntentFilter(Y60Action.RESET_BC_HTTP_PROXY);
        mResetReceiver = new ResetReceiver();
        registerReceiver(mResetReceiver, filter);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent pIntent, int flags, int startId) {
        Logger.v(LOG_TAG,"service command started");
        sendBroadcast(new Intent(Y60Action.SERVICE_HTTP_PROXY_READY));
        Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ sent broadcast http proxy ready");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mResetReceiver);
        mResourceManager.deactivate();
        clear();
        stopForeground(true);
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
        mResourceManager.requestResource(pUri);
    }

    /**
     * Stores the uri in a bundle. Big files will be written to sdcard, small ones a kept in memory.
     * 
     * @throws Exception
     */
    void refreshResource(final String pUri) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.v(LOG_TAG, "refreshing (", pUri, ")");
                Bundle oldContent;

                synchronized (mCachedContent) {
                    Logger.v(LOG_TAG, "in 'synchronized'");
                    oldContent = mCachedContent.get(pUri);
                }
                Logger.v(LOG_TAG, "out of 'synchronized'");
                Bundle newContent = null;
                try {
                    Logger.v(LOG_TAG, "get new content...");
                    newContent = ResourceDownloadHelper.downloadAndCreateResourceBundle(CACHE_DIR,
                            pUri);
                    Logger.v(LOG_TAG, "got new content...");
                } catch (Exception e) {
                    ErrorHandling.signalHttpError(LOG_TAG, e, HttpProxyService.this);
                    sendResourceNotAvailableBc(pUri);
                    Logger.e(LOG_TAG, "refreshing ", pUri, " failed!");
                }

                byte[] oldContentBytes = IoHelper.convertResourceBundleToByteArray(oldContent);
                byte[] newContentBytes = IoHelper.convertResourceBundleToByteArray(newContent);

                if (!IoHelper.areWeEqual(oldContentBytes, newContentBytes)) {

                    if (newContentBytes == null) {
                        synchronized (mCachedContent) {
                            mCachedContent.remove(pUri);
                        }
                        sendResourceNotAvailableBc(pUri);
                        Logger.e(LOG_TAG, "refreshing ", pUri, " failed!");

                    } else {
                        Logger.v(LOG_TAG, "storing new content and sending bc 'updated' for '",
                                pUri, "'");
                        synchronized (mCachedContent) {
                            mCachedContent.put(pUri, newContent);
                        }
                        sendResourceAvailableBc(pUri);
                    }
                } else {
                    Logger.v(LOG_TAG, "NO new content for '", pUri,
                            "' i am not broadcasting resource available through system");
                }
            }
        }).start();
    }

    /**
     * Helper to notify about resource changes via intent broadcast.
     */
    void sendResourceAvailableBc(String pUri) {
        Intent intent = new Intent(HttpProxyConstants.RESOURCE_UPDATE_ACTION);
        intent.putExtra(HttpProxyConstants.URI_EXTRA, pUri);
        // Logger.v(LOG_TAG, "Broadcasting 'update' for resource " + pUri);
        sendBroadcast(intent);
    }

    void sendResourceNotAvailableBc(String pUri) {
        Intent intent = new Intent(HttpProxyConstants.RESOURCE_NOT_AVAILABLE_ACTION);
        intent.putExtra(HttpProxyConstants.URI_EXTRA, pUri);
        sendBroadcast(intent);
    }

    public Bundle getDataSyncronously(String pUri) throws HttpClientException, HttpServerException,
            IOException {
        Bundle newContent = ResourceDownloadHelper.downloadAndCreateResourceBundle(CACHE_DIR, pUri);
        synchronized (mCachedContent) {
            mCachedContent.put(pUri, newContent);
        }
        return newContent;
    }

    public Bundle fetchFromCache(String pUri) {
        Logger.v(LOG_TAG, "fetchFromCache(", pUri, ")");
        synchronized (mCachedContent) {
            return mCachedContent.get(pUri);
        }
    }

    public boolean isInCache(String pUri) {
        boolean isInCache;
        synchronized (mCachedContent) {
            isInCache = mCachedContent.containsKey(pUri);
        }
        return isInCache;
    }

    public void removeFromCache(String pUri) {
        Logger.v(LOG_TAG, "removeFromCache(", pUri, ")");
        synchronized (mCachedContent) {
            mCachedContent.remove(pUri);
        }
    }

    private void clear() {
        Logger.d(LOG_TAG, "clearing HTTP cache");
        synchronized (mCachedContent) {
            mCachedContent.clear();
            mResourceManager.clear();
        }
        IoHelper.deleteDir(new File(CACHE_DIR));
        File dir = new File(CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            Logger.e(LOG_TAG, "creating dir " + dir.toString());
        }
        sendBroadcast(new Intent(Y60Action.SERVICE_HTTP_PROXY_CLEARED));
        Logger.d(LOG_TAG, "HTTP cache cleared");
    }

    public long getNumberOfEntries() {
        synchronized (mCachedContent) {
            return mCachedContent.size();
        }
    }

    Map<String, Bundle> getCachedContent() {
        return mCachedContent;
    }

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
