package com.artcom.y60.infrastructure.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.util.Log;

import com.artcom.y60.infrastructure.HTTPHelper;
import com.artcom.y60.logging.Logger;

public class Cache {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = Cache.class.getName();
    
    
    

    // Instance Variables ------------------------------------------------

    private Map<String, byte[]> mCachedContent;
    
    private List<String> mPendingResources;
    
    private Thread mRefresher;
    
    private boolean mShutdown;

    
    
    // Constructors ------------------------------------------------------

    public Cache() {
        
        mCachedContent    = new HashMap<String, byte[]>();
        mPendingResources = new LinkedList<String>();
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public byte[] get(String pUri) {
        
        Log.v(LOG_TAG, "HttpProxyService.get("+pUri+")");
        synchronized (mPendingResources) {
            
            if (!mPendingResources.contains(pUri)) {
                
                mPendingResources.add(pUri);
            }
            Log.v(LOG_TAG, "pending resources: "+mPendingResources.size());
        }
        
        synchronized (mCachedContent) {
         
            return mCachedContent.get(pUri);
        }
    }
    

    public byte[] fetchFromCache(String pUri) {
        
        Logger.v(LOG_TAG, "fetchFromCache(", pUri, ")");
        synchronized (mCachedContent) {
            
            return mCachedContent.get(pUri);
        }
    }
    
    
    public void stop() {
        
        mShutdown  = true;
        synchronized(this) {
            mRefresher = null;
        }
    }
    
    
    public void resume() {
        
        mShutdown         = false;
        
        synchronized(this) {
            if (mRefresher == null) {
                mRefresher = new Thread(new ResourceRefresher());
                mRefresher.start();
            }
        }
    }
    
    
    
    // Package Protected Instance Methods --------------------------------

    void clear() {
        
        synchronized (mPendingResources) {
            synchronized (mCachedContent) {
                
                mCachedContent.clear();
                mPendingResources.clear();
            }
        }
    }
    
    
    void logCache() {
        
        Logger.v(LOG_TAG, "cached content: ", mCachedContent);
    }

    
    
    
    // Private Instance Methods ------------------------------------------

    private void refresh(String pUri) {
        
        Logger.v(LOG_TAG, "refresh(", pUri, ")");
        try {
            
            synchronized (mCachedContent) {
                byte[] oldContent = mCachedContent.get(pUri);
                byte[] newContent = HTTPHelper.getAsByteArray(Uri.parse(pUri));
                
                if (!Arrays.equals(oldContent, newContent)) {
                    
                    Logger.v(LOG_TAG, "storing new content for '", pUri, "'");
                    mCachedContent.put(pUri, newContent);
                    
                    Logger.v(LOG_TAG, "broadcast update for resource '", pUri, "'");
                    HttpProxyService.resourceUpdated(pUri);
                }
            }
            
        } catch (Exception e) {
            
            Logger.e(LOG_TAG, "refreshing ", pUri, " failed!", e);
        }
    }
    
    

    class ResourceRefresher implements Runnable {

        public void run() {
            
            Logger.v("ResourceRefresher", "refresher thread for HttpServiceProxy starts");
            while (!mShutdown) {
                
//                Logger.v("ResourceRefresher", "refresher thread is waking up");
                
                String uri = null;
                synchronized (mPendingResources) {
                    
                    if (mPendingResources.size() > 0) {
                        
                        uri = mPendingResources.remove(0);
                        Logger.v("ResourceRefresher", "found uri: ", uri);
                        
                    } else {
                        
//                        Logger.v("ResourceRefresher", "nothing to do");
                    }
                }
                
                // do processing out of synchronization to avoid
                // that clients get blocked
                if (uri != null) {
                    
                    refresh(uri);
                }
                
                try {
                    
                    Thread.sleep(20);
                    
                } catch (InterruptedException ix) {
                    
                    // not interested
                }
            }
            Logger.v("ResourceRefresher", "refresher thread STOPS ------------ ");
        }
    }

        
}
