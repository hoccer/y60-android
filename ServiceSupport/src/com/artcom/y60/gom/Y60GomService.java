package com.artcom.y60.gom;

import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
import com.artcom.y60.Y60Action;
import com.artcom.y60.Y60Service;
import com.artcom.y60.http.HttpProxyHelper;

public abstract class Y60GomService extends Y60Service {

    // Constants ---------------------------------------------------------

    private static final String  LOG_TAG       = "Y60GomService";
    private GomProxyHelper       mGom;
    private HttpProxyHelper      mHttpProxy;
    private final List<Runnable> mExecutablesForBindToGom;
    private final List<Runnable> mExecutablesForBindToHttpProxy;
    private AsyncTask            mAsyncTaskForBindToGom;

    protected static boolean     sIsBoundToGom = false;

    public Y60GomService() {
        mExecutablesForBindToGom = new LinkedList<Runnable>();
        mExecutablesForBindToHttpProxy = new LinkedList<Runnable>();
    }

    @Override
    public void onCreate() {
        bindProxys();
        super.onCreate();
    }

    protected void bindProxys() {
        bindToGom();
        bindToHttpProxy();
    }

    @Override
    public void onDestroy() {
        unbindFromGom();
        unbindFromHttpProxy();
        super.onDestroy();
    }

    public GomProxyHelper getGom() {
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

        if (isBoundToGom()) {
            Thread thread = new Thread(pRunnable);
            Logger.v(LOG_TAG, "callOnBoundToGom() and bound to gom, got Runnable, executing it: ",
                    pRunnable);
            thread.start();
            return thread;
        } else {
            mExecutablesForBindToGom.add(pRunnable);
            Logger.v(LOG_TAG, "callOnBoundToGom() with Runnable: ", pRunnable,
                    " and adding to list, size: ", mExecutablesForBindToGom.size());
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
        Logger.v(LOG_TAG, "callOnBoundToHttpProxy() with Runnable: ",
                mExecutablesForBindToHttpProxy);

        if (isBoundToHttpProxy()) {
            Logger.v(LOG_TAG,
                    "callOnBoundToHttpProxy() and bound http, got Runnable, executing it: ",
                    mExecutablesForBindToHttpProxy);
            new Thread(pRunnable).start();
        } else {
            mExecutablesForBindToHttpProxy.add(pRunnable);
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

                for (Runnable runnable : mExecutablesForBindToGom) {
                    Logger.v(LOG_TAG, "bindToGom(), got Runnable, executing it: ",
                            mExecutablesForBindToGom.toString());
                    new Thread(runnable).start();
                }

                mExecutablesForBindToGom.clear();

                if (mAsyncTaskForBindToGom != null) {
                    mAsyncTaskForBindToGom.execute(null);
                }

            }

            public void unbound(GomProxyHelper helper) {
                Logger.v(LOG_TAG, "GomProxy unbound: ", getClass().getSimpleName());
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

                for (Runnable runnable : mExecutablesForBindToHttpProxy) {
                    Logger.v(LOG_TAG, "bindToHttp(), got Runnable, executing it: ", runnable);
                    new Thread(runnable).start();
                }

                mExecutablesForBindToHttpProxy.clear();
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
