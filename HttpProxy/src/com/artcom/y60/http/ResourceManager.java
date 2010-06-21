package com.artcom.y60.http;

import java.util.LinkedList;
import java.util.List;

import com.artcom.y60.Logger;

public class ResourceManager {

    // Constants ---------------------------------------------------------

    public static final String     LOG_TAG           = "Cache";

    private boolean                mShutdown;
    private final HttpProxyService mService;
    private List<String>           mPendingResources = null;

    private Thread                 mRefreshLoop;

    // Constructors ------------------------------------------------------

    public ResourceManager(HttpProxyService pService) {
        mService = pService;
        mPendingResources = new LinkedList<String>();

    }

    public void deactivate() {
        mShutdown = true;
        synchronized (this) {
            mRefreshLoop = null;
        }
    }

    public void activate() {
        mShutdown = false;
        synchronized (this) {
            if (mRefreshLoop == null) {
                mRefreshLoop = new Thread(new ResourceLooper());
                mRefreshLoop.start();
            }
        }
    }

    public void requestResource(String pUri) {
        Logger.v(LOG_TAG, "adding to pending resources and getting: ", pUri);
        synchronized (mPendingResources) {
            if (!mPendingResources.contains(pUri)) {
                mPendingResources.add(pUri);
            }
        }
    }

    public class ResourceLooper implements Runnable {

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
                    mService.refreshResource(uri);
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

    public void clear() {
        mPendingResources.clear();
    }

}
