package com.artcom.y60.error;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

import com.artcom.y60.ErrorHandling;

public class FallbackExceptionHandler extends ErrorReporter {

    static String LOG_TAG = "FallbackExceptionHandler";
    final Context mContext;

    public FallbackExceptionHandler(Context pContext,
            UncaughtExceptionHandler pOriginalExceptionHandler) {
        mContext = pContext;
    }

    public static void register(Context pContext) {
        Thread.setDefaultUncaughtExceptionHandler(new FallbackExceptionHandler(pContext, Thread
                .getDefaultUncaughtExceptionHandler()));
    }

    @Override
    protected void onException(Throwable e) {
        ErrorHandling.signalUnspecifiedError(LOG_TAG, e, mContext);
    }
}
