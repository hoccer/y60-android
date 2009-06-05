package com.artcom.y60;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import android.content.Context;
import android.content.Intent;

public class ErrorHandling {

    public enum Category {

        // system or software errors
        FILE_NOT_FOUND, MALFORMED_URI, MALFORMED_DATA, UNSUPPORTED_ENCODING, SAX_ERROR, JSON_ERROR, MISSING_GOM_ENTRY, MISSING_MANDATORY_OBJECT,

        // development and environmental errors
        COMPONENT_NOT_FOUND, NETWORK_ERROR, IO_ERROR, ILLEGAL_ARGUMENT, GOM_ERROR, BACKEND_ERROR, SERVICE_ERROR, DEFECTIVE_CONTENT_ERROR,

        UNSPECIFIED,
    }

    private static final String LOG_TAG = "ErrorHandling";

    public static final String ID_MESSAGE = "error";
    public static final String ID_LOGTAG = "logtag";
    public static final String ID_CATEGORY = "category";

    public static void signalError(String logTag, Throwable error, Context context,
            Category category) {
        Logger.e(LOG_TAG, "signaling error: ", error);
        Intent intent = new Intent("y60.intent.ERROR_PRESENTATION");
        intent.putExtra(ID_MESSAGE, error.toString());
        intent.putExtra(ID_LOGTAG, logTag);
        intent.putExtra(ID_CATEGORY, category);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void signalFileNotFoundError(String logTag, FileNotFoundException error, Context context) {
        signalError(logTag, error, context, Category.FILE_NOT_FOUND);
    }

    public static void signalMalformedUriError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.MALFORMED_URI);
    }

    public static void signalUnsupportedEncodingError(String logTag, Throwable error,
            Context context) {
        signalError(logTag, error, context, Category.UNSUPPORTED_ENCODING);
    }

    public static void signalSaxError(String logTag, SAXException error, Context context) {
        signalError(logTag, error, context, Category.SAX_ERROR);
    }

    public static void signalSaxError(String logTag, SAXParseException error, Context context) {
        signalError(logTag, error, context, Category.SAX_ERROR);
    }

    public static void signalJsonError(String logTag, JSONException error, Context context) {
        signalError(logTag, error, context, Category.JSON_ERROR);
    }

    public static void signalComponentNotFoundError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.COMPONENT_NOT_FOUND);
    }

    public static void signalNetworkError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.NETWORK_ERROR);
    }

    public static void signalIllegalArgumentError(String logTag, IllegalArgumentException error, Context context) {
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

    public static void signalMissingGomEntryError(String logTag, NoSuchElementException error, Context context) {
        signalError(logTag, error, context, Category.MISSING_GOM_ENTRY);
    }

    public static void signalMissingMandatoryObjectError(String logTag, Throwable error,
            Context context) {
        signalError(logTag, error, context, Category.MISSING_MANDATORY_OBJECT);
    }

    public static void signalDefectiveContentError(String logTag, DefectiveContentException error,
                    Context context) {
        signalError(logTag, error, context, Category.DEFECTIVE_CONTENT_ERROR);
    }
   
    public static void signalUnspecifiedError(String logTag, Throwable error, Context context) {
        // TODO this method should not be used eventually
        signalError(logTag, error, context, Category.UNSPECIFIED);
    }

    public static void signalIOError(String logTag, IOException error, Context context) {
        signalError(logTag, error, context, Category.IO_ERROR);
    }
}
