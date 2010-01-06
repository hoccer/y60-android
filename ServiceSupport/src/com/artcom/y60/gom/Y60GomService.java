package com.artcom.y60.gom;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.Y60Action;
import com.artcom.y60.Y60Service;
import com.artcom.y60.http.HttpProxyHelper;

import android.content.Intent;
import android.os.AsyncTask;

public abstract class Y60GomService extends Y60Service {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG       = "Y60GomService";
    private GomProxyHelper      mGom;
    private HttpProxyHelper     mHttpProxy;
    private Runnable            mNotificationCallbackForBindToGom;
    private Runnable            mNotificationCallbackForBindToHttpProxy;
    private AsyncTask           mAsyncTaskForBindToGom;

    protected static boolean    sIsBoundToGom = false;

    @Override
    public void onCreate() {

        super.onCreate();
        bindToGom();
        bindToHttpProxy();
    }

    @Override
    public void onDestroy() {
        unbindFromGom();
        unbindFromHttpProxy();
        super.onDestroy();
    }

    protected GomProxyHelper getGom() {
        if (mGom == null || !mGom.isBound()) {
            throw new BindingException("requested gom, but it is not bound");
        }
        return mGom;
    }

    protected HttpProxyHelper getHttpProxy() {
        if (mHttpProxy == null || !mHttpProxy.isBound()) {
            throw new BindingException("requested httpProxy, but it is not bound");
        }
        return mHttpProxy;
    }

    public static boolean isStaticBoundToGom() {
        return sIsBoundToGom;
    }

    public boolean isBoundToGom() {
        return mGom != null && mGom.isBound();
    }

    public boolean isBoundToHttpProxy() {
        return mHttpProxy != null && mHttpProxy.isBound();
    }

    public Thread callOnBoundToGom(Runnable pRunnable) {

        mNotificationCallbackForBindToGom = pRunnable;
        Logger.v(LOG_TAG, "callOnBoundToGom() with Runnable: ", mNotificationCallbackForBindToGom);

        Thread thread = new Thread(mNotificationCallbackForBindToGom);
        if (isBoundToGom()) {
            Logger.v(LOG_TAG, "callOnBoundToGom() and bound to gom, got Runnable, executing it: ",
                    mNotificationCallbackForBindToGom);
            thread.start();
            return thread;
        }
        return null;
    }

    public void callOnBoundToGom(AsyncTask<Object, Object, Object> pAsyncTask, Object pInputParams) {

        mAsyncTaskForBindToGom = pAsyncTask;
        if (isBoundToGom()) {
            mAsyncTaskForBindToGom.execute(pInputParams);
        }
    }

    public void callOnBoundToHttpProxy(Runnable pRunnable) {
        mNotificationCallbackForBindToHttpProxy = pRunnable;
        Logger.v(LOG_TAG, "callOnBoundToHttpProxy() with Runnable: ",
                mNotificationCallbackForBindToHttpProxy);

        if (isBoundToHttpProxy()) {
            Logger.v(LOG_TAG,
                    "callOnBoundToHttpProxy() and bound http, got Runnable, executing it: ",
                    mNotificationCallbackForBindToHttpProxy);
            new Thread(mNotificationCallbackForBindToHttpProxy).start();
        }
    }

    public void blockUntilBoundToGom() {
        try {
            TestHelper.blockUntilTrue("Y60GomService could not bind to gom proxy", 2000,
                    new TestHelper.Condition() {

                        @Override
                        public boolean isSatisfied() {
                            return Y60GomService.this.isBoundToGom();
                        }

                    });
        } catch (Exception e) {
            ErrorHandling.signalBackendError(LOG_TAG, e, Y60GomService.this);
        }
    }

    public void bindToGom() {

        Logger.v(LOG_TAG, "bindToGom()");

        new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {

            public void bound(GomProxyHelper phelper) {

                sendBroadcast(new Intent(Y60Action.SERVICE_GOM_PROXY_READY));
                Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ sent broadcast gom proxy ready");

                Logger.v(LOG_TAG, "GomProxy bound");
                mGom = phelper;
                if (mNotificationCallbackForBindToGom != null) {
                    Logger.v(LOG_TAG, "bindToGom(), got Runnable, executing it: ",
                            mNotificationCallbackForBindToGom.toString());
                    new Thread(mNotificationCallbackForBindToGom).start();
                }

                if (mAsyncTaskForBindToGom != null) {
                    mAsyncTaskForBindToGom.execute(null);
                }

            }

            public void unbound(GomProxyHelper helper) {
                Logger.v(LOG_TAG, "GomProxy unbound");
                mGom = null;
            }
        });
    }

    public void bindToHttpProxy() {

        Logger.v(LOG_TAG, "bindToHttpProxy()");

        new HttpProxyHelper(this, new BindingListener<HttpProxyHelper>() {

            public void bound(HttpProxyHelper phelper) {

                sendBroadcast(new Intent(Y60Action.SERVICE_HTTP_PROXY_READY));
                Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ sent broadcast http proxy ready");

                Logger.v(LOG_TAG, "HttpProxy bound");
                mHttpProxy = phelper;
                if (mNotificationCallbackForBindToHttpProxy != null) {
                    Logger.v(LOG_TAG, "bindToHttp(), got Runnable, executing it: ",
                            mNotificationCallbackForBindToHttpProxy);
                    new Thread(mNotificationCallbackForBindToHttpProxy).start();
                }
            }

            public void unbound(HttpProxyHelper helper) {
                Logger.v(LOG_TAG, "HttpProxy unbound");
                mHttpProxy = null;
            }
        });
    }

    public void unbindFromGom() {

        if (mGom != null) {
            mGom.unbind();
        }
    }

    public void unbindFromHttpProxy() {

        if (mHttpProxy != null) {
            mHttpProxy.unbind();
        }
    }
}
