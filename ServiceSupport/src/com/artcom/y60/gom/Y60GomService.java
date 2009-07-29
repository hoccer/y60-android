package com.artcom.y60.gom;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.Logger;
import com.artcom.y60.Y60Service;

public abstract class Y60GomService extends Y60Service {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "Y60GomService";
    private GomProxyHelper      mGom;

    @Override
    public void onCreate() {

        super.onCreate();
        bindToGom();

    }

    @Override
    public void onDestroy() {
        unbindFromGom();
        super.onDestroy();
    }

    protected GomProxyHelper getGom() {
        if (mGom == null || !mGom.isBound()) {
            throw new BindingException("requested gom, but it is not bound");
        }
        return mGom;
    }

    protected boolean isBoundToGom() {
        return mGom != null && mGom.isBound();
    }

    public void bindToGom() {

        new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {

            public void bound(GomProxyHelper phelper) {
                Logger.v(LOG_TAG, "GomProxy bound");
                mGom = phelper;
            }

            public void unbound(GomProxyHelper helper) {
                Logger.v(LOG_TAG, "GomProxy unbound");
                mGom = null;
            }
        });
    }

    public void unbindFromGom() {

        if (mGom != null) {
            mGom.unbind();
        }
    }
}
