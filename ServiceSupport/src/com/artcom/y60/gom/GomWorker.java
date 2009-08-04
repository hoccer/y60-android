package com.artcom.y60.gom;

import com.artcom.y60.BindingException;
import com.artcom.y60.Logger;

public abstract class GomWorker {

    private static final String LOG_TAG = "GomWorker";

    protected GomProxyHelper    mGom;
    private boolean             mHasFinished;

    public GomWorker(GomProxyHelper gom) {
        mGom = gom;
        if (mGom == null) {
            throw new NullPointerException("No gom proxy provided: GomWorker got a null reference");
        }
        if (!mGom.isBound()) {
            throw new BindingException("GomProxy was not bound. GomWorker can't possibly work.");
        }
        mHasFinished = false;
        launch();
    }

    public abstract void execute();

    private void launch() {
        try {
            execute();
        } catch (BindingException e) {
            Logger.v(LOG_TAG,
                    "ignoring bind exception -- gom seems have been unbound while execution");
        }
        mHasFinished = true;
    }

    public GomProxyHelper getGom() {
        return mGom;
    }

    public boolean hasFinished() {
        return mHasFinished;
    }
}
