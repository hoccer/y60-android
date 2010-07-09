package com.artcom.y60;

public class ReflectionHelper {

    private static final String LOG_TAG = "ReflectionHelper";

    public static String thisMethodName() {

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // logStackTrace(stack);

        return stack[3].getMethodName();
    }

    private static void logStackTrace(StackTraceElement[] pStack) {

        Logger.v(LOG_TAG, "stack trace:");

        for (StackTraceElement elem : pStack) {
            Logger.v(LOG_TAG, elem.getMethodName());
        }
    }

    public static String callingMethodName() {

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        // logStackTrace(stack);

        String name = stack[4].getMethodName();
        if (name.equals("<init>")) {
            name = stack[5].getMethodName();
        }
        return name;
    }
}
