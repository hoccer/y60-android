package com.artcom.y60.http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IoHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.ResourceDownloadHelper;

import android.os.Bundle;

public class Cache {

    // Constants ---------------------------------------------------------

    public static final String        LOG_TAG   = "Cache";
    static final String               CACHE_DIR = "/sdcard/HttpProxyCache/";
    private final Map<String, Bundle> mCachedContent;

    private final List<String>        mPendingResources;

    private Thread                    mRefresher;

    private boolean                   mShutdown;
    private final HttpProxyService    mService;

    // Constructors ------------------------------------------------------

    public Cache(HttpProxyService pService) {

        mService = pService;

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
    public void requestResource(String pUri) {
        Logger.v(LOG_TAG, "adding to pending resources and getting: ", pUri);

        synchronized (mPendingResources) {
            if (!mPendingResources.contains(pUri)) {
                mPendingResources.add(pUri);
            }
            Logger.v(LOG_TAG, "pending resources: ", mPendingResources.size());
        }
    }

    public Bundle getDataSyncronously(String pUri) throws HttpClientException, HttpServerException,
            IOException {

        Bundle newContent = ResourceDownloadHelper.downloadAndCreateResourceBundle(CACHE_DIR, pUri);
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
        synchronized (mCachedContent) {
            return mCachedContent.containsKey(pUri);
        }
    }

    public void deactivate() {
        mShutdown = true;
        synchronized (this) {
            mRefresher = null;
        }
    }

    public void activate() {
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

    public long getNumberOfEntries() {
        logCache();
        return mCachedContent.size();
    }

    void logCache() {

        Logger.v(LOG_TAG, "cached content: ", mCachedContent);
    }

    // Private Instance Methods ------------------------------------------

    /**
     * Stores the uri in a bundle. Big files will be written to sdcard, small ones a kept in memory.
     * 
     * @throws Exception
     */
    private void refresh(String pUri) {

        Logger.v(LOG_TAG, "refreshing (", pUri, ")");
        synchronized (mCachedContent) {

            Bundle oldContent = mCachedContent.get(pUri);
            Bundle newContent = null;
            try {
                newContent = ResourceDownloadHelper
                        .downloadAndCreateResourceBundle(CACHE_DIR, pUri);
            } catch (HttpException e) {
                ErrorHandling.signalHttpError(LOG_TAG, e, mService);
                mService.resourceNotAvailable(pUri);
                Logger.e(LOG_TAG, "refreshing ", pUri, " failed!");
            } catch (IOException e) {
                ErrorHandling.signalIOError(LOG_TAG, e, mService);
                mService.resourceNotAvailable(pUri);
                Logger.e(LOG_TAG, "refreshing ", pUri, " failed!");
            }

            byte[] oldContentBytes = IoHelper.convertResourceBundleToByteArray(oldContent);
            byte[] newContentBytes = IoHelper.convertResourceBundleToByteArray(newContent);

            if (!IoHelper.areWeEqual(oldContentBytes, newContentBytes)) {

                if (newContentBytes == null) {
                    mCachedContent.remove(pUri);
                    mService.resourceNotAvailable(pUri);
                    Logger.e(LOG_TAG, "refreshing ", pUri, " failed!");

                } else {
                    Logger.v(LOG_TAG, "storing new content and sending bc 'updated' for '", pUri,
                            "'");
                    mCachedContent.put(pUri, newContent);
                    mService.resourceAvailable(pUri);
                }
            } else {
                Logger.v(LOG_TAG, "NO new content for '", pUri, "'");
            }
        }
    }

    class ResourceRefresher implements Runnable {

        public void run() {

            Logger.v(LOG_TAG, "ResourceRefresher, refresher thread for HttpServiceProxy starts");
            while (!mShutdown) {

                String uri = null;
                synchronized (mPendingResources) {
                    if (mPendingResources.size() > 0) {
                        uri = mPendingResources.remove(0);
                        Logger.v(LOG_TAG, "ResourceRefresher, removing and processing uri: ", uri);
                    }
                }

                // do processing out of synchronization to avoid
                // that clients get blocked
                if (uri != null) {
                    refresh(uri);
                } else {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ix) {
                    }
                }
            }
            Logger.v(LOG_TAG, "ResourceRefresher, refresher thread STOPS ------------ ");
        }
    }

    public Map<String, Bundle> getCachedContent() {
        return mCachedContent;
    }
}
