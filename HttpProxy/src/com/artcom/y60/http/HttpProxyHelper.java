package com.artcom.y60.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

/**
 * Helper class for activities which encapsulates the interaction with the
 * HttpProxyService, especially the listening to resource update broadcasts.
 * Activities can register listeners to this helper for particular URI which
 * they are interested in.
 * 
 * @author arne
 * @see HttpProxyService
 */
public class HttpProxyHelper {

    // Instance Variables ------------------------------------------------

    private static final String LOG_TAG = "HttpProxyHelper";

    /**
     * The client for this HttpProxyHelper
     */
    private Context mContext;

    /**
     * Handler for resource updates, which are received as broadcasts from the
     * HttpProxy
     */
    private Map<URI, Set<ResourceChangeListener>> mListeners;

    /**
     * Thread for dispatching incoming broadcasts to the resource update handler
     */
    private Thread mUpdateThread;

    /**
     * The queue of URIs of resources which have been updated
     */
    private List<URI> mNotificationQueue;

    /**
     * Flag indicating if the updater thread should shutdown
     */
    private boolean mShutdown;

    private IHttpProxyService mProxy;

    private HttpProxyServiceConnection mConnection;

    private BindingListener<HttpProxyHelper> mBindingListener;

    private HttpResourceUpdateReceiver mReceiver;

    // Constructors ------------------------------------------------------

    public HttpProxyHelper(Context pContext, BindingListener<HttpProxyHelper> pBindingListener) {

        mShutdown = false;
        mContext = pContext;
        mNotificationQueue = new LinkedList<URI>();
        mListeners = new HashMap<URI, Set<ResourceChangeListener>>();
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
        
        try {
            
            return get(new URI(pUri.toString()));
            
        } catch (URISyntaxException usx) {
            
            Logger.e(LOG_TAG, "uri is not wellformed:", usx);
            throw new RuntimeException("uri is not wellformed:" + usx);
        }
    }
    
    public byte[] get(URI pUri) {

        String uri = pUri.toString();

        Bundle resourceDescription;
        try {
            resourceDescription = mProxy.get(uri);
        } catch (RemoteException rex) {
            Logger.e(logTag(), "get(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }
        if (resourceDescription == null) {
            return null;
        }
        return convertResourceBundleToByteArray(resourceDescription);
    }

    public Drawable get(Uri uri, Drawable defaultDrawable) {
        try {
            return get(new URI(uri.toString()), defaultDrawable);
        } catch (URISyntaxException e) {
            ErrorHandling.signalMalformedUriError(logTag(), e, mContext);
            return defaultDrawable;
        }
    }

    public Drawable get(URI pUri, Drawable pDefault) {

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

    public String get(URI pUri, String pDefault) {

        if (mProxy == null) {

            return pDefault;
        }

        byte[] bytes = get(pUri);

        if (bytes == null) {

            return pDefault;
        }

        return new String(bytes);
    }

    public byte[] fetchFromCache(URI pUri) {

        try {
            mProxy.fetchFromCache(pUri.toString());

            Bundle resourceDescription = mProxy.fetchFromCache(pUri.toString());
            return HttpProxyHelper.convertResourceBundleToByteArray(resourceDescription);

        } catch (RemoteException rex) {

            Logger.e(logTag(), "fetchFromCache(", pUri, ") failed", rex);
            throw new RuntimeException(rex);
        }
    }
    
    public void fetchFromCacheToFile(Uri pUri, File pTargetFile) throws IOException {
        
        try {
            URI              uri     = new URI(pUri.toString());
            byte[]           content = fetchFromCache(uri);
            FileOutputStream out     = new FileOutputStream(pTargetFile);
            for (byte b: content) {
                
                out.write(b);
            }
            
        } catch (URISyntaxException usx) {
            
            Logger.e(LOG_TAG, "uri is not wellformed:", usx);
            throw new RuntimeException("uri is not wellformed:" + usx);
        }
    }

    public Drawable fetchDrawableFromCache(URI pUri) {

        byte[] bytes = fetchFromCache(pUri);
        InputStream is = new ByteArrayInputStream(bytes);
        return Drawable.createFromStream(is, pUri.toString());
    }

    public String fetchStringFromCache(URI pUri) {

        byte[] bytes = fetchFromCache(pUri);
        return new String(bytes);
    }

    public void addResourceChangeListener(URI[] pUris, ResourceChangeListener pLsner) {

        for (URI uri : pUris) {

            addResourceChangeListener(uri, pLsner);
        }
    }

    public void addResourceChangeListener(URI pUri, ResourceChangeListener pLsner) {

        Set<ResourceChangeListener> lsners = getOrCreateListenersFor(pUri);
        synchronized (lsners) {

            lsners.add(pLsner);
        }
    }

    public void addResourceChangeListener(Uri uri, ResourceChangeListener lsner) {

        try {
            addResourceChangeListener(new URI(uri.toString()), lsner);
        } catch (URISyntaxException e) {
            throw new RuntimeException("uri is not wellformed:" + e);
        }
    }

    public void shutdown() {

        mShutdown = true;
        mContext.unbindService(mConnection);
    }

    public static byte[] convertResourceBundleToByteArray(Bundle resourceDescription) {

        String resourcePath = resourceDescription.getString(Cache.LOCAL_RESOURCE_PATH_TAG);
        if (resourcePath == null) {
            return resourceDescription.getByteArray(Cache.BYTE_ARRY_TAG);
        }

        byte[] buffer;
        try {
            File file = new File(resourcePath);
            FileInputStream stream = new FileInputStream(file);
            if (file.length() > Integer.MAX_VALUE) {
                throw new RuntimeException("file '" + file + "' is to big");
            }
            buffer = new byte[(int) file.length()];
            stream.read(buffer);
        } catch (IOException e) {
            Logger.e(LOG_TAG, "io error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return buffer;
    }

    // Private Instance Methods ------------------------------------------

    private Set<ResourceChangeListener> getOrCreateListenersFor(URI pUri) {

        Set<ResourceChangeListener> listeners = getListenersFor(pUri);
        if (listeners == null) {

            listeners = new HashSet<ResourceChangeListener>();
            synchronized (mListeners) {

                mListeners.put(pUri, listeners);
            }
        }

        return listeners;
    }

    private Set<ResourceChangeListener> getListenersFor(URI pUri) {

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
                try {

                    mNotificationQueue.add(new URI(uri));

                } catch (URISyntaxException ux) {

                    Logger.e(logTag(), "couldn't parse URI extra", ux);
                }
            }
        }
    }

    class Updater implements Runnable {

        public void run() {

            while (!mShutdown) {

                URI uri = null;
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
                    Set<ResourceChangeListener> listeners = getListenersFor(uri);
                    if (listeners != null) {
                        synchronized (listeners) {

                            for (ResourceChangeListener listener : listeners) {
                                listener.onResourceChanged(uri);
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
            mBindingListener.unbound(HttpProxyHelper.this);
        }
    }
}
