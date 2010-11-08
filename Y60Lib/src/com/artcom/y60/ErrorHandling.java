package com.artcom.y60;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.artcom.y60.http.HttpHelper;

public class ErrorHandling {

    public enum Category {

        // system or software errors
        FILE_NOT_FOUND, MALFORMED_URI, MALFORMED_DATA, UNSUPPORTED_ENCODING, SAX_ERROR, JSON_ERROR, MISSING_GOM_ENTRY, MISSING_MANDATORY_OBJECT, LOW_ON_MEMORY_ERROR, INIT_ERROR,

        // development and environmental errors
        COMPONENT_NOT_FOUND, NETWORK_ERROR, IO_ERROR, ILLEGAL_ARGUMENT, GOM_ERROR, BACKEND_ERROR, SERVICE_ERROR, DEFECTIVE_CONTENT_ERROR, NOT_IMPLEMENTED, HTTP_ERROR, UNKNOWN_ASSET, ASSET_CREATION,

        UNSPECIFIED
    }

    public static final int     Y60_ERROR_NOTIFICATION_ID = 2342;

    private static final String LOG_TAG                   = "ErrorHandling";

    public static final String  ID_MESSAGE                = "error";
    public static final String  ID_STACKTRACE             = "stacktrace";
    public static final String  ID_LOGTAG                 = "logtag";
    public static final String  ID_CATEGORY               = "category";

    public static void signalError(String logTag, Throwable error, Context context,
            Category category) {
        Logger.e(LOG_TAG, "signaling error: ", error);

        Intent intent = createErrorPresentationIntent(logTag, error, category);
        // context.startActivity(intent);
        sendErrorNotification(logTag, error, context, intent);
        saveErrorOnSdcard(logTag, error, category);
        try {
            String path = Constants.Gom.URI + "/log/mobile:"
                    + DeviceConfiguration.load().getDeviceId() + "-" + logTag;
            Logger.e(LOG_TAG, "putting to path ", path);
            HashMap<String, String> pData = new HashMap<String, String>();
            pData.put("type", "string");
            StringWriter s = new StringWriter();
            PrintWriter p = new PrintWriter(s);
            error.printStackTrace(p);
            pData.put("attribute", s.getBuffer().toString());
            HttpHelper.putUrlEncoded(path, pData);
        } catch (Exception e) {
            Logger.e(LOG_TAG, e);
        }
    }

    public static void signalWarning(String logTag, String warning, Context context,
            Category category) {
        Logger.e(LOG_TAG, "signaling warning: ", warning);
        Intent intent = createWarningPresentationIntent(logTag, warning, category);
        sendWarningNotification(logTag, warning, context, intent);
    }

    private static void saveErrorOnSdcard(String pLogTag, Throwable pError, Category pCategory) {

        try {
            String errorLog = null;

            File f = new File("/sdcard/error_log.txt");
            if (f.exists()) {
                FileReader fr;
                fr = new FileReader("/sdcard/error_log.txt");
                char[] inputBuffer = new char[(int) f.length()];
                fr.read(inputBuffer);
                errorLog = new String(inputBuffer);
            }

            FileWriter fw;

            fw = new FileWriter("/sdcard/error_log.txt");
            if (errorLog != null) {
                fw.write(errorLog + "\n");
            }
            fw.write(pLogTag + " (" + pCategory + ") " + "\n");
            fw.write(Log.getStackTraceString(pError));
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    public static void signalAssetCreationError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.ASSET_CREATION);
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

    public static void signalHttpError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.HTTP_ERROR);
    }

    public static void signalIllegalArgumentError(String logTag, IllegalArgumentException error,
            Context context) {
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

    public static void signalMandentoryServiceKilled(String logTag, Context context) {
        signalError(logTag, new Exception(
                "This service is ment to be a deamon and should not have been killed"), context,
                Category.SERVICE_ERROR);
    }

    public static void signalMissingGomEntryError(String logTag, NoSuchElementException error,
            Context context) {
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

    public static void signalLowOnMemoryError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.LOW_ON_MEMORY_ERROR);
    }

    public static void signalMalformedDataError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.MALFORMED_DATA);
    }

    public static void signalNotImplementedError(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.NOT_IMPLEMENTED);
    }

    public static void signalUnkownHostException(String logTag, Throwable error, Context context) {
        signalError(logTag, error, context, Category.NETWORK_ERROR);
    }

    public static void signalUnknownAssetError(String pLogTag, UnknownAssetException pE,
            Context pContext) {
        signalError(pLogTag, pE, pContext, Category.UNKNOWN_ASSET);
    }

    public static void signalInitProcessError(String pLogTag,
            InitProcessException initProcessException, Context tgInitService) {
        signalError(pLogTag, initProcessException, tgInitService, Category.INIT_ERROR);
    }

    public static void clearErrorAndWarningNotifications(Context pContext) {
        NotificationManager notifier = (NotificationManager) pContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notifier.cancel(Y60_ERROR_NOTIFICATION_ID);
    }

    private static Intent createErrorPresentationIntent(String pLogTag, Throwable pError,
            Category pCategory) {
        Intent intent = new Intent("y60.intent.ERROR_PRESENTATION");
        intent.putExtra(ID_MESSAGE, pError.toString());

        intent.putExtra(ID_LOGTAG, pLogTag);
        intent.putExtra(ID_CATEGORY, pCategory);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        String stacktrace = stackTraceToString(pError.getStackTrace());
        intent.putExtra(ID_STACKTRACE, stacktrace);

        return intent;
    }

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

    private static Intent createWarningPresentationIntent(String pLogTag, String pWarning,
            Category pCategory) {
        Intent intent = new Intent("y60.intent.ERROR_PRESENTATION");
        intent.putExtra(ID_MESSAGE, pWarning.toString());

        intent.putExtra(ID_LOGTAG, pLogTag);
        intent.putExtra(ID_CATEGORY, pCategory);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return intent;
    }

    private static void sendErrorNotification(String pLogTag, Throwable pError, Context pContext,
            Intent pErrorPresentationIntent) {

        NotificationManager notifier = (NotificationManager) pContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pint = PendingIntent.getActivity(pContext, 0, pErrorPresentationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification(android.R.drawable.stat_notify_error, "Error",
                System.currentTimeMillis());
        notification.setLatestEventInfo(pContext, "Error", pLogTag + ": " + pError.getMessage(),
                pint);
        notifier.notify(Y60_ERROR_NOTIFICATION_ID, notification);
    }

    private static void sendWarningNotification(String pLogTag, String pWarning, Context pContext,
            Intent pErrorPresentationIntent) {

        NotificationManager notifier = (NotificationManager) pContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pint = PendingIntent.getActivity(pContext, 0, pErrorPresentationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification(android.R.drawable.stat_sys_warning,
                "Warning", System.currentTimeMillis());
        notification.setLatestEventInfo(pContext, "Warning", pLogTag + ": " + pWarning, pint);
        notifier.notify(Y60_ERROR_NOTIFICATION_ID, notification);
    }

    public static void signalWarningToLog(String pLogTag, String pMsg, Context pContext) {

        // by now, we just log it as a warning
        Logger.w(pLogTag, pMsg);
    }

    public static void signalInitProcessWarning(String pLogTag, String initProcessWarning,
            Context tgInitService) {
        signalWarning(pLogTag, initProcessWarning, tgInitService, Category.INIT_ERROR);
    }

}
