package com.artcom.y60.error;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;

public class FallbackExceptionHandler implements UncaughtExceptionHandler {

    private static String                  LOG_TAG = "FallbackExceptionHandler";
    private final Context                  mContext;
    private final UncaughtExceptionHandler mOriginalExceptionHandler;

    public FallbackExceptionHandler(Context pContext,
            UncaughtExceptionHandler pOriginalExceptionHandler) {
        mContext = pContext;
        mOriginalExceptionHandler = pOriginalExceptionHandler;
    }

    @Override
    public void uncaughtException(Thread pThread, Throwable e) {
        Logger.e(LOG_TAG, "There was an uncaught excepiton:" + e);
        ErrorHandling.signalUnspecifiedError(LOG_TAG, e, mContext);
        mOriginalExceptionHandler.uncaughtException(pThread, e);
    }

    public static void register(Context pContext) {
        Thread.setDefaultUncaughtExceptionHandler(new FallbackExceptionHandler(pContext, Thread
                .getDefaultUncaughtExceptionHandler()));
    }
}
