package com.artcom.y60.error;

import java.lang.Thread.UncaughtExceptionHandler;

import com.artcom.y60.Logger;

public class ErrorReporter implements UncaughtExceptionHandler {

    private static final String    LOG_TAG = "ErrorReporter";
    final UncaughtExceptionHandler mOriginalExceptionHandler;

    public ErrorReporter() {
        mOriginalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread pThread, Throwable e) {
        Logger.e(LOG_TAG, "There was an uncaught excepiton:" + e);
        onException(e);
        mOriginalExceptionHandler.uncaughtException(pThread, e);
    }

    protected void onException(Throwable e) {
        Logger.e(LOG_TAG, e);
    }
}
