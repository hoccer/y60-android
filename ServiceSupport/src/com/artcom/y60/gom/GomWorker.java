package com.artcom.y60.gom;

import com.artcom.y60.BindingException;
import com.artcom.y60.Logger;

public abstract class GomWorker implements Runnable {

    private static final String LOG_TAG = "GomWorker";

    private GomProxyHelper      mGom;
    private boolean             mHasFinished;

    public GomWorker(GomProxyHelper gom) throws GomProxyException {
        mGom = gom;
        if (mGom == null) {
            throw new NullPointerException("No gom proxy provided: GomWorker got a null reference");
        }
        if (!mGom.isBound()) {
            throw new BindingException("GomProxy was not bound. GomWorker can't possibly work.");
        }
        mHasFinished = false;
        run();
    }

    abstract void execute();

    @Override
    public void run() {
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
