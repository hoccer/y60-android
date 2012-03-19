package com.artcom.y60;


public class ErrorHandling {

    public static String getCurrentStackTrace() {
        return stackTraceToString(Thread.currentThread().getStackTrace());
    }

    public static String stackTraceToString(StackTraceElement[] trace) {
        String stacktrace = "";
        for (StackTraceElement line : trace) {
            stacktrace += line.toString() + "\n";
        }
        return stacktrace;
    }

}
