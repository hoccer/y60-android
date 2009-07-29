package com.artcom.y60.gom;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.TestHelper;
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

    public boolean isBoundToGom() {
        return mGom != null && mGom.isBound();
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
