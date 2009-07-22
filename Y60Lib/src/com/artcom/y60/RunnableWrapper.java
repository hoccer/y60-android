package com.artcom.y60;

import android.content.Context;

public class RunnableWrapper implements Runnable {

    private static final String LOG_TAG = "RunnableWrapper";

    // Instance Variables ------------------------------------------------

    private Runnable            mTarget;

    private Context             mContext;

    // Constructors ------------------------------------------------------

    public RunnableWrapper(Context pContext, Runnable pTarget) {

        mTarget = pTarget;
        mContext = pContext;
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void run() {

        try {

            mTarget.run();

        } catch (Exception x) {

            Logger.e(LOG_TAG, "runOnUiThreadWithErrorHandling caught error ", x.getMessage());
            ErrorHandling.signalUnspecifiedError("RunnableWrapper", x, mContext);
        }
    }
}
