package com.artcom.y60.http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;

import com.artcom.y60.IoHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.ResourceBundleHelper;

public class Cache {

    // Constants ---------------------------------------------------------

    public static final String        LOG_TAG   = "Cache";
    static final String               CACHE_DIR = "/sdcard/HttpProxyCache/";
    private final Map<String, Bundle> mCachedContent;                       // _the_
    // cache

    private final List<String>        mPendingResources;

    private Thread                    mRefresher;

    private boolean                   mShutdown;

    // Constructors ------------------------------------------------------

    public Cache() {

        mCachedContent = new HashMap<String, Bundle>();
        mPendingResources = new LinkedList<String>();

        File dir = new File(CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
            Logger.e(LOG_TAG, "creating dir " + dir.toString());
        }

    }

    // Public Instance Methods -------------------------------------------

    /**
     * @return parcelable key/value list describing the resource
     */
    public Bundle get(String pUri) {

        Logger.v(LOG_TAG, "HttpProxyService.get(", pUri, ")");
        synchronized (mPendingResources) {

            if (!mPendingResources.contains(pUri)) {

                mPendingResources.add(pUri);
            }
            Logger.v(LOG_TAG, "pending resources: ", mPendingResources.size());
        }

        synchronized (mCachedContent) {

            return mCachedContent.get(pUri);
        }
    }

    public Bundle getDataSyncronously(String pUri) throws HttpClientException, HttpServerException,
            IOException {

        Bundle newContent = ResourceBundleHelper.createResourceBundle(CACHE_DIR, pUri);

        mCachedContent.put(pUri, newContent);

        return newContent;
    }

    /**
     * @return parcelable key/value list describing the resource
     */
    public Bundle fetchFromCache(String pUri) {

        Logger.v(LOG_TAG, "fetchFromCache(", pUri, ")");
        synchronized (mCachedContent) {

            return mCachedContent.get(pUri);
        }
    }

    public void remove(String pUri) {
        Logger.v(LOG_TAG, "removeFromCache(", pUri, ")");
        synchronized (mCachedContent) {

            mCachedContent.remove(pUri);
        }

    }

    public boolean isInCache(String pUri) {
        Logger.v(LOG_TAG, "isInCache(", pUri, ")");
        synchronized (mCachedContent) {
            return mCachedContent.containsKey(pUri);
        }
    }

    public void stop() {

        mShutdown = true;
        synchronized (this) {
            mRefresher = null;
        }
    }

    public void resume() {

        mShutdown = false;

        synchronized (this) {
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

                IoHelper.deleteDir(new File(CACHE_DIR));
                File dir = new File(CACHE_DIR);
                if (!dir.exists()) {
                    dir.mkdirs();
                    Logger.e(LOG_TAG, "creating dir " + dir.toString());
                }
            }
        }
    }

    void logCache() {

        Logger.v(LOG_TAG, "cached content: ", mCachedContent);
    }

    // Private Instance Methods ------------------------------------------

    /**
     * Stores the uri in a bundle. Big files will be written to sdcard, small
     * ones a kept in memory.
     * 
     * @throws Exception
     */
    private void refresh(String pUri) {

        Logger.v(LOG_TAG, "refresh(", pUri, ")");
        try {

            synchronized (mCachedContent) {
                Bundle oldContent = mCachedContent.get(pUri);

                Bundle newContent = ResourceBundleHelper.createResourceBundle(CACHE_DIR, pUri);

                // if resource has changed (TODO get header and check the
                // modification date)
                if (oldContent == null
                        || oldContent.getLong(HttpProxyConstants.SIZE_TAG) != newContent
                                .getLong(HttpProxyConstants.SIZE_TAG)) {

                    Logger.v(LOG_TAG, "storing new content for '", pUri, "'");
                    mCachedContent.put(pUri, newContent);

                    Logger.v(LOG_TAG, "broadcast update for resource '", pUri, "'");
                    HttpProxyService.resourceUpdated(pUri);
                }
            }

        } catch (Exception e) {
            HttpProxyService.resourceNotAvailable(pUri);
            Logger.e(LOG_TAG, "refreshing ", pUri, " failed!", e);
        }
    }

    class ResourceRefresher implements Runnable {

        public void run() {

            Logger.v("ResourceRefresher", "refresher thread for HttpServiceProxy starts");
            while (!mShutdown) {

                // Logger.v("ResourceRefresher",
                // "refresher thread is waking up");

                String uri = null;
                synchronized (mPendingResources) {

                    if (mPendingResources.size() > 0) {

                        uri = mPendingResources.remove(0);
                        Logger.v("ResourceRefresher", "found uri: ", uri);

                    } else {

                        // Logger.v("ResourceRefresher", "nothing to do");
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
