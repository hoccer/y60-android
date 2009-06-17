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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.artcom.y60.BindingListener;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.ResourceBundleHelper;
import com.artcom.y60.RpcStatus;

/**
 * Helper class for activities which encapsulates the interaction with the
 * HttpProxyService, especially the listening to resou.rce update broadcasts.
 * Activities can register listeners to this helper for particular URI which
 * they are interested in.
 * 
 * @author arne
 * @see HttpProxyService
 */
public class HttpProxyHelper {

    // Instance Variables ------------------------------------------------

    private static final String              LOG_TAG = "HttpProxyHelper";

    /**
     * The client for this HttpProxyHelper
     */
    private Context                          mContext;

    /**
     * Handler for resource updates, which are received as broadcasts from the
     * HttpProxy
     */
    private Map<Uri, Set<ResourceListener>>  mListeners;

    /**
     * Thread for dispatching incoming broadcasts to the resource update handler
     */
    private Thread                           mUpdateThread;

    /**
     * The queue of URIs of resources which have been updated
     */
    private List<Uri>                        mNotificationQueue;

    /**
     * Flag indicating if the updater thread should shutdown
     */
    private boolean                          mShutdown;

    private IHttpProxyService                mProxy;

    private HttpProxyServiceConnection       mConnection;

    private BindingListener<HttpProxyHelper> mBindingListener;

    private HttpResourceUpdateReceiver       mReceiver;

    // Constructors ------------------------------------------------------

    public HttpProxyHelper(Context pContext, BindingListener<HttpProxyHelper> pBindingListener) {

        mShutdown = false;
        mContext = pContext;
        mNotificationQueue = new LinkedList<Uri>();
        mListeners = new HashMap<Uri, Set<ResourceListener>>();
        mBindingListener = pBindingListener;

        mReceiver = new HttpResourceUpdateReceiver();
        mContext.registerReceiver(mReceiver, new IntentFilter(
                HttpProxyConstants.RESOURCE_UPDATE_ACTION));

        Intent proxyIntent = new Intent(IHttpProxyService.class.getName());
        mConnection = new HttpProxyServiceConnection();
        Logger.v(logTag(), "binding to HttpProxy");
        if (!mContext.bindService(proxyIntent, mConnection, Context.BIND_AUTO_CREATE)) {

            throw new RuntimeException("bindService failed for HttpProxyService");
        }

        mUpdateThread = new Thread(new Updater());
        mUpdateThread.start();
    }

    // Public Instance Methods -------------------------------------------

    public void unbind() {
        mContext.unbindService(mConnection);
        mContext.unregisterReceiver(mReceiver);
    }

    public void assertConnected() {

        if (mProxy == null) {

            throw new RuntimeException("Unable to bind proxy!");
        }
    }

    public byte[] get(Uri pUri) {

        String uri = pUri.toString();

        Bundle resourceDescription;
        RpcStatus status = new RpcStatus();
        try {
            resourceDescription = mProxy.get(uri, status);
        } catch (RemoteException rex) {
            Logger.e(logTag(), "get(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }
        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }
        if (resourceDescription == null) {
            return null;
        }
        return ResourceBundleHelper.convertResourceBundleToByteArray(resourceDescription);
    }

    public Drawable get(Uri pUri, Drawable pDefault) {

        if (mProxy == null) {

            Logger.v(logTag(), "get called, but proxy is still null - returning fallback");
            return pDefault;
        }

        byte[] bytes = get(pUri);

        if (bytes == null) {

            Logger.v(logTag(), "get called, but result is empty - returning fallback");
            return pDefault;
        }

        InputStream is = new ByteArrayInputStream(bytes);
        return Drawable.createFromStream(is, pUri.toString());
    }

    public String get(Uri pUri, String pDefault) {

        if (mProxy == null) {

            return pDefault;
        }

        byte[] bytes = get(pUri);

        if (bytes == null) {

            return pDefault;
        }

        return new String(bytes);
    }

    public boolean isInCache(Uri pUri) {

        boolean isInCache;
        RpcStatus status = new RpcStatus();
        try {
            isInCache = mProxy.isInCache(pUri.toString(), status);

        } catch (RemoteException rex) {

            Logger.e(logTag(), "isInCache(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }

        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }

        return isInCache;
    }

    public byte[] fetchFromCache(Uri pUri) {

        RpcStatus status = new RpcStatus();
        Bundle content;
        try {
            // mProxy.fetchFromCache(pUri.toString(), status);

            content = mProxy.fetchFromCache(pUri.toString(), status);

        } catch (RemoteException rex) {

            Logger.e(logTag(), "fetchFromCache(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }

        if (status.hasError()) {
            throw new RuntimeException(status.getError());
        }

        return ResourceBundleHelper.convertResourceBundleToByteArray(content);
    }

    public File fetchFromCacheAsFile(Uri pUri) throws IOException {

        RpcStatus status = new RpcStatus();
        Bundle bundle;

        try {
            bundle = mProxy.get(pUri.toString(), status);

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

    public void addResourceChangeListener(Uri pUri, ResourceListener pListener) {

        Set<ResourceListener> listeners = getOrCreateListenersFor(pUri);
        synchronized (listeners) {

            listeners.add(pListener);
            if (isInCache(pUri)) {
                pListener.onResourceAvailable(pUri);
            }
        }
    }

    public void shutdown() {

        mShutdown = true;
        mContext.unbindService(mConnection);
    }

    // Private Instance Methods ------------------------------------------

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

    // Inner Classes -----------------------------------------------------

    class HttpResourceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context pCtx, Intent pIntent) {

            synchronized (mNotificationQueue) {

                String uri = pIntent.getStringExtra(HttpProxyConstants.URI_EXTRA);
                Logger.v(logTag(), "received broadcast for uri ", uri);
                mNotificationQueue.add(Uri.parse(uri));
            }
        }
    }

    class Updater implements Runnable {

        public void run() {

            while (!mShutdown) {

                Uri uri = null;
                synchronized (mNotificationQueue) {

                    if (mNotificationQueue.size() > 0) {

                        uri = mNotificationQueue.remove(0);
                    }
                }

                // do processing out of synchronization to avoid
                // that the broadcast receiver gets blocked when
                // adding new URIs
                if (uri != null) {

                    Logger.v(logTag(), "registered listeners: ", mListeners.keySet());
                    Logger.v(logTag(), "updating listeners for uri ", uri);
                    Set<ResourceListener> listeners = getListenersFor(uri);
                    if (listeners != null) {
                        synchronized (listeners) {

                            for (ResourceListener listener : listeners) {
                                listener.onResourceChanged(uri);
                                listener.onResourceAvailable(Uri.parse(uri.toString()));
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
}
