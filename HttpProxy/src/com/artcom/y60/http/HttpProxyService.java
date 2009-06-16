package com.artcom.y60.http;

import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.Logger;
import com.artcom.y60.RpcStatus;

/**
 * Implementation of client-side caching for HTTP resources.
 * 
 * @author arne
 * @see HttpProxyHelper
 */
public class HttpProxyService extends Service {

    // Constants ---------------------------------------------------------

    public static final String           LOG_TAG;

    private static final Cache           CACHE;

    // Class Variables ---------------------------------------------------

    private static Set<HttpProxyService> sInstances;

    // Static Initializer ------------------------------------------------

    static {

        LOG_TAG = "HttpProxyService[class " + System.currentTimeMillis() + "]";
        CACHE = new Cache();
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
                Logger.v(LOG_TAG, "Broadcasting update for resource " + pUri);
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

    private String          mId;

    // Constructors ------------------------------------------------------

    public HttpProxyService() {

        mId = String.valueOf(System.currentTimeMillis());
        Logger.v(tag(), "HttpProxyService instantiated");
    }

    // Public Instance Methods -------------------------------------------

    public void onCreate() {

        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());

        Logger.i(tag(), "HttpProxyService.onCreate");

        super.onCreate();

        mRemote = new HttpProxyRemote();

        synchronized (sInstances) {
            sInstances.add(this);
            CACHE.resume();
            Logger.v(tag(), "instances: ", countInstances());
        }
    }

    public void onStart(Intent intent, int startId) {

        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());

        Logger.i(tag(), "HttpProxyService.onStart");

        super.onStart(intent, startId);

        Logger.d(tag(), "instances: " + countInstances());
    }

    public void onDestroy() {

        Logger.i(tag(), "HttpProxyService.onDestroy");

        super.onDestroy();

        synchronized (sInstances) {
            sInstances.remove(this);

            if (sInstances.size() < 1) {
                CACHE.stop();
            }
            Logger.d(tag(), "instances: " + countInstances());
        }
    }

    public IBinder onBind(Intent pIntent) {

        return mRemote;
    }

    public Bundle get(String pUri) {

        return CACHE.get(pUri);
    }

    public Bundle fetchFromCache(String pUri) {

        return CACHE.fetchFromCache(pUri);
    }

    public boolean isInCache(String pUri) {

        return CACHE.isInCache(pUri);
    }

    public void removeFromCache(String pUri) {
        CACHE.remove(pUri);
    }

    // Private Instance Methods ------------------------------------------

    private String tag() {

        return LOG_TAG + "[instance " + mId + "]";
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
    }

}
