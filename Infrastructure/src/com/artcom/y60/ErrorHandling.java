package com.artcom.y60;

import android.content.Context;
import android.content.Intent;


public class ErrorHandling {

    enum Category {

        FILE_NOT_FOUND, MALFORMED_URI, UNSUPPORTED_ENCODING, SAX_ERROR, JSON_ERROR, MISSING_GOM_ENTRY, MISSING_MANDENTORY_OBJECT,

        COMPONENT_NOT_FOUND, NETWORK_ERROR, ILLEGAL_ARGUMENT, GOM_ERROR, BACKEND_ERROR, SERVICE_ERROR,

        UNSPECIFIED,
    }

    public static final String ID_ERROR = "error";
    public static final String ID_LOGTAG = "logtag";
    public static final String ID_CATEGORY = "category";

    public static void signalError(String logTag, Throwable error, Context context,
            Category category) {
        Intent intent = new Intent("y60.intent.ERROR_PRESENTATION");
        intent.putExtra(ID_ERROR, error);
        intent.putExtra(ID_LOGTAG, logTag);
        // intent.putExtra(ID_CATEGORY, category);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void signalFileNotFoundError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.FILE_NOT_FOUND);
    }

    public static void signalMalformedUriError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.MALFORMED_URI);
    }

    public static void signalUnsupportedEncodingError(String logTag, Throwable error,
            Context context) {
        signalError(logTag, error, context, Category.UNSUPPORTED_ENCODING);
    }

    public static void signalSaxError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.SAX_ERROR);
    }

    public static void signalJsonError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.JSON_ERROR);
    }

    public static void signalComponentNotFoundError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.COMPONENT_NOT_FOUND);
    }

    public static void signalNetworkError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.NETWORK_ERROR);
    }

    public static void signalIllegalArgumentError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.ILLEGAL_ARGUMENT);
    }

    public static void signalGomError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.GOM_ERROR);
    }

    public static void signalBackendError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.BACKEND_ERROR);
    }

    public static void signalServiceError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.SERVICE_ERROR);
    }

    public static void signalMissingGomEntryError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.MISSING_GOM_ENTRY);
    }

    public static void signalMissingMandentoryObjectError(String logTag, Throwable error,
            Context context) {
        signalError(logTag, error, context, Category.MISSING_MANDENTORY_OBJECT);
    }

    public static void signalMissingMandentoryObjectError(String logTag, Throwable error) {
        Logger.e(logTag, "MISSING_MANDENTORY_OBJECT", error);
    }

    public static void signalUnspecifiedError(String logTag, Throwable error, Context context) {
        // TODO this method should not be used eventually
        signalError(logTag, error, context, Category.UNSPECIFIED);
    }

}