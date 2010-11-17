package com.artcom.y60.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.artcom.y60.BindingListener;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IoHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.RpcStatus;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Helper class for activities which encapsulates the interaction with the HttpProxyService,
 * especially the listening to resou.rce update broadcasts. Activities can register listeners to
 * this helper for particular URI which they are interested in.
 * 
 * @author arne
 * @see HttpProxyService
 */
public class HttpProxyHelper {

    // Instance Variables ------------------------------------------------

    private static final String                    LOG_TAG = "HttpProxyHelper";

    /**
     * The client for this HttpProxyHelper
     */
    private final Context                          mContext;

    /**
     * Handler for resource updates, which are received as broadcasts from the HttpProxy
     */
    private final Map<Uri, Set<ResourceListener>>  mListeners;

    /**
     * Thread for dispatching incoming broadcasts to the resource update handler
     */
    private final Thread                           mUpdateThread;

    /**
     * The queue of URIs of resources which have been updated
     */
    private final List<Uri>                        mUpdateNotificationQueue;
    /**
     * The queue of URIs of resources which are not available
     */
    private final List<Uri>                        mNotAvailableNotificationQueue;

    /**
     * Flag indicating if the updater thread should shutdown
     */
    private boolean                                mShutdown;

    private IHttpProxyService                      mProxy;
    private final HttpProxyServiceConnection       mConnection;
    private final BindingListener<HttpProxyHelper> mBindingListener;
    private final HttpResourceUpdateReceiver       mUpdateReceiver;
    private final HttpResourceNotAvailableReceiver mNotAvailableReceiver;

    public HttpProxyHelper(Context pContext, BindingListener<HttpProxyHelper> pBindingListener) {
        mShutdown = false;
        mContext = pContext;
        mUpdateNotificationQueue = new LinkedList<Uri>();
        mNotAvailableNotificationQueue = new LinkedList<Uri>();
        mListeners = new HashMap<Uri, Set<ResourceListener>>();
        mBindingListener = pBindingListener;

        mUpdateReceiver = new HttpResourceUpdateReceiver();
        mContext.registerReceiver(mUpdateReceiver, new IntentFilter(
                HttpProxyConstants.RESOURCE_UPDATE_ACTION));
        mNotAvailableReceiver = new HttpResourceNotAvailableReceiver();
        mContext.registerReceiver(mNotAvailableReceiver, new IntentFilter(
                HttpProxyConstants.RESOURCE_NOT_AVAILABLE_ACTION));

        Intent proxyIntent = new Intent(IHttpProxyService.class.getName());
        mConnection = new HttpProxyServiceConnection();
        Logger.v(logTag(), "binding to HttpProxy");
        if (!mContext.bindService(proxyIntent, mConnection, Context.BIND_AUTO_CREATE)) {

            throw new RuntimeException("bindService failed for HttpProxyService");
        }

        mUpdateThread = new Thread(new Updater());
        mUpdateThread.start();
    }

    public void unbind() {
        mContext.unbindService(mConnection);
        mContext.unregisterReceiver(mUpdateReceiver);
        mContext.unregisterReceiver(mNotAvailableReceiver);
    }

    public void assertConnected() {
        if (mProxy == null) {
            throw new RuntimeException("Unable to bind proxy!");
        }
    }

    public void requestDownload(Uri pUri) {
        requestResource(pUri.toString());
    }

    public String getDataSyncronously(Uri pUri) {
        String uri = pUri.toString();

        Bundle resourceDescription;
        RpcStatus status = new RpcStatus();
        try {
            resourceDescription = mProxy.getDataSyncronously(uri, status);
        } catch (RemoteException rex) {
            Logger.e(logTag(), "get(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }
        if (status.hasError()) {
            return null;
            // throw new RuntimeException(status.getError());
        }
        if (resourceDescription == null) {
            return null;
        }
        return new String(IoHelper.convertResourceBundleToByteArray(resourceDescription));
    }

    /*@Deprecated
    public void requestResourceWhichIsDeprecated(Uri pUri) {
        requestResource(pUri.toString());
    }*/

    public void requestResource(String pUri) {
        RpcStatus status = new RpcStatus();
        try {
            mProxy.requestResource(pUri, status);
        } catch (RemoteException rex) {
            Logger.e(logTag(), "get(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }
        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }
    }

    public boolean isBound() {
        return mProxy != null;
    }

    public boolean isInCache(String pUri) {
        Logger.v(LOG_TAG, "before: isInCache");
        boolean isInCache;
        RpcStatus status = new RpcStatus();
        try {
            isInCache = mProxy.isInCache(pUri, status);
        } catch (RemoteException rex) {
            Logger.e(logTag(), "isInCache(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }

        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }

        Logger.v(LOG_TAG, "after: isInCache");
        return isInCache;
    }

    public byte[] fetchFromCache(Uri pUri) {
        RpcStatus status = new RpcStatus();
        Bundle content;
        try {
            content = mProxy.fetchFromCache(pUri.toString(), status);
        } catch (RemoteException rex) {
            Logger.e(logTag(), "fetchFromCache(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }

        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }

        if (content == null) {
            throw new RuntimeException("content bundle is null");
        }

        return IoHelper.convertResourceBundleToByteArray(content);
    }

    public File fetchFromCacheAsFile(Uri pUri) throws IOException {
        RpcStatus status = new RpcStatus();
        Bundle bundle;

        try {
            bundle = mProxy.fetchFromCache(pUri.toString(), status);
        } catch (RemoteException rx) {
            ErrorHandling.signalServiceError(LOG_TAG, rx, mContext);
            // this should never be reached:
            Logger.e(LOG_TAG, "ErrorHandling didn't abort thread after error!");
            throw new IllegalStateException("ErrorHandling didn't abort thread after error", rx);
        }
        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }

        String resourcePath = bundle.getString(HttpProxyConstants.LOCAL_RESOURCE_PATH_TAG);
        if (resourcePath == null) {
            ErrorHandling.signalIllegalArgumentError(LOG_TAG, new IllegalArgumentException(
                    "Resource for URI " + pUri + " is not available as file!"), mContext);
        }
        return new File(resourcePath);
    }

    public Drawable fetchDrawableFromCache(Uri pUri) {
        byte[] bytes = fetchFromCache(pUri);
        InputStream is = new ByteArrayInputStream(bytes);
        return Drawable.createFromStream(is, pUri.toString());
    }

    public Bitmap fetchBitmapFromCache(Uri pUri) {
        byte[] bytes = fetchFromCache(pUri);
        InputStream is = new ByteArrayInputStream(bytes);
        return BitmapFactory.decodeStream(is);
    }

    public String fetchStringFromCache(Uri pUri) {
        byte[] bytes = fetchFromCache(pUri);
        return new String(bytes);
    }

    public void removeFromCache(Uri pUri) {
        removeFromCache(pUri.toString());
    }

    public void removeFromCache(String pUri) {
        RpcStatus status = new RpcStatus();
        try {
            mProxy.removeFromCache(pUri, status);
        } catch (RemoteException rex) {

            Logger.e(logTag(), "removeFromCache(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }
        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }
    }

    public void clear() {
        RpcStatus status = new RpcStatus();
        try {
            mProxy.clear(status);
        } catch (RemoteException rex) {
            Logger.e(logTag(), "clear failed", rex);
            throw new RuntimeException(rex);
        }
        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }
    }

    public long getNumberOfEntries() {
        long numberOfEntries = 0;
        RpcStatus status = new RpcStatus();
        try {
            numberOfEntries = mProxy.getNumberOfEntries(status);
        } catch (RemoteException rex) {
            Logger.e(logTag(), "getNumberOfEntries failed", rex);
            throw new RuntimeException(rex);
        }
        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }
        return numberOfEntries;
    }

    public void addResourceChangeListenerAndReport(final Uri pUri, final ResourceListener pListener) {
        Set<ResourceListener> listeners = getOrCreateListenersFor(pUri);
        synchronized (listeners) {
            listeners.add(pListener);
        }
        if (isInCache(pUri.toString())) {
            new Thread() {
                @Override
                public void run() {
                    Logger.v(LOG_TAG, "Uri: ", pUri,
                            " is in cache, calling: onResourceAvailable on: ", pListener);
                    pListener.onResourceAvailable(pUri);
                }
            }.start();
        } else {
            Logger.v(LOG_TAG, "Uri: ", pUri, " is NOT in cache, do nothing");
        }
    }

    public void addResourceChangeListener(final Uri pUri, final ResourceListener pListener) {
        Set<ResourceListener> listeners = getOrCreateListenersFor(pUri);
        synchronized (listeners) {
            listeners.add(pListener);
        }
    }

    public void shutdown() {
        mShutdown = true;
        mContext.unbindService(mConnection);
    }

    private Set<ResourceListener> getOrCreateListenersFor(Uri pUri) {
        Set<ResourceListener> listeners = getListenersFor(pUri);
        if (listeners == null) {
            listeners = new HashSet<ResourceListener>();
            synchronized (mListeners) {
                mListeners.put(pUri, listeners);
            }
        }
        return listeners;
    }

    private Set<ResourceListener> getListenersFor(Uri pUri) {
        synchronized (mListeners) {
            return mListeners.get(pUri);
        }
    }

    private String logTag() {
        return "HttpProxyHelper(" + mContext.getPackageName() + ")";
    }

    class HttpResourceUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context pCtx, Intent pIntent) {
            synchronized (mUpdateNotificationQueue) {
                String uri = pIntent.getStringExtra(HttpProxyConstants.URI_EXTRA);
                Logger.v(logTag(), "received update broadcast for uri ", uri);
                mUpdateNotificationQueue.add(Uri.parse(uri));
            }
        }
    }

    class HttpResourceNotAvailableReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context pCtx, Intent pIntent) {
            synchronized (mNotAvailableNotificationQueue) {
                String uri = pIntent.getStringExtra(HttpProxyConstants.URI_EXTRA);
                Logger.v(logTag(), "received not available broadcast for uri ", uri);
                mNotAvailableNotificationQueue.add(Uri.parse(uri));
            }
        }
    }

    class Updater implements Runnable {
        public void run() {
            while (!mShutdown) {
                Uri updatedUri = null;
                synchronized (mUpdateNotificationQueue) {
                    if (mUpdateNotificationQueue.size() > 0) {
                        updatedUri = mUpdateNotificationQueue.remove(0);
                    }
                }

                // do processing out of synchronization to avoid
                // that the broadcast receiver gets blocked when
                // adding new URIs
                if (updatedUri != null) {
                    Logger.v(logTag(), "registered listeners: ", mListeners.keySet());
                    Logger.v(logTag(), "updating listeners for uri ", updatedUri);

                    Set<ResourceListener> listeners = getListenersFor(updatedUri);
                    if (listeners != null) {
                        synchronized (listeners) {
                            for (ResourceListener listener : listeners) {
                                Logger.v(logTag(), "updating listener: ", listener);
                                listener.onResourceAvailable(Uri.parse(updatedUri.toString()));
                            }
                        }
                    }
                }

                Uri notAvailableUri = null;
                synchronized (mNotAvailableNotificationQueue) {
                    if (mNotAvailableNotificationQueue.size() > 0) {
                        notAvailableUri = mNotAvailableNotificationQueue.remove(0);
                    }
                }

                // do processing out of synchronization to avoid
                // that the broadcast receiver gets blocked when
                // adding new URIs
                if (notAvailableUri != null) {
                    Logger.v(logTag(), "registered listeners: ", mListeners.keySet());
                    Logger.v(logTag(), "updating listeners for uri ", notAvailableUri);
                    Set<ResourceListener> listeners = getListenersFor(notAvailableUri);
                    if (listeners != null) {
                        synchronized (listeners) {
                            for (ResourceListener listener : listeners) {
                                listener.onResourceNotAvailable(Uri.parse(notAvailableUri
                                        .toString()));
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ix) {
                    // not interested
                }
            }
        }
    }

    class HttpProxyServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName pName, IBinder pBinder) {
            mProxy = IHttpProxyService.Stub.asInterface(pBinder);
            mBindingListener.bound(HttpProxyHelper.this);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mProxy = null;
            Logger.w(LOG_TAG, "HTTP proxy service has been disconnected unexpectedly!");
            mBindingListener.unbound(HttpProxyHelper.this);
        }
    }

    public int getSizeInCache(Uri pUri) {
        byte[] bytes = fetchFromCache(pUri);
        if (bytes == null) {
            return 0;
        } else {
            return bytes.length;
        }
    }
}