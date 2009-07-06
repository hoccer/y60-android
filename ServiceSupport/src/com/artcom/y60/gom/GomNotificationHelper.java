package com.artcom.y60.gom;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Constants;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandler;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.IpAddressNotFoundException;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;

public class GomNotificationHelper {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "GomNotificationHelper";

    // Static Methods ----------------------------------------------------

    // REGISTER AND NOTIFY
    public static BroadcastReceiver registerObserverAndNotify(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom) throws IOException,
            IpAddressNotFoundException {

        return registerObserverAndNotify(pPath, pGomObserver, pGom, new ErrorHandler() {

            @Override
            public void handle(Exception pE) {
                Logger.e(LOG_TAG, "*******", pE);
            }

        });

    }

    public static BroadcastReceiver registerObserverAndNotify(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom,
            final ErrorHandler pErrorHandler) throws IOException, IpAddressNotFoundException {

        return registerObserverAndNotify(pPath, pGomObserver, pGom, false, pErrorHandler);
    }

    /**
     * Register a GOM observer for a given path. Filtering options are currently
     * not supported.
     * 
     * @param pPath
     *            Gom path to observe
     * @param pGomObserver
     *            the observer??? Don't know!
     * @param pErrorHandler
     *            pass your own error handling code to the registration thread
     * 
     */
    public static BroadcastReceiver registerObserverAndNotify(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom, final boolean pBubbleUp,
            final ErrorHandler pErrorHandler) throws IOException, IpAddressNotFoundException {

        BroadcastReceiver rec = createBroadcastReceiver(pPath, pGomObserver, pBubbleUp);

        if (!pGom.isBound()) {
            throw new IllegalStateException("GomProxyHelper " + pGom.toString() + " is not bound!");
        }

        new Thread(new Runnable() {
            public void run() {

                try {
                    putObserverToGom(pPath, pBubbleUp);
                    boolean doRefresh = pGom.hasInCache(pPath);
                    GomEntry entry = pGom.getEntry(pPath);
                    Logger.v(LOG_TAG, "OLD PROXY ENTRY: pGomObserver.onEntryUpdated( ", pPath,
                            entry.toJson(), " )");
                    pGomObserver.onEntryUpdated(pPath, entry.toJson());

                    if (doRefresh) {
                        pGom.refreshEntry(pPath);
                        GomEntry newEntry = pGom.getEntry(pPath);

                        // make sure old and new entry are properly loaded
                        // (lazily)
                        if (newEntry instanceof GomNode) {

                            ((GomNode) newEntry).entries();
                        }
                        if (entry instanceof GomNode) {

                            ((GomNode) entry).entries();
                        }

                        if (!newEntry.equals(entry)) {
                            Logger.v(LOG_TAG, "NEW GOM ENTRY: pGomObserver.onEntryUpdated( ",
                                    pPath, entry.toJson(), " )");
                            pGomObserver.onEntryUpdated(pPath, newEntry.toJson());
                        }
                    }
                } catch (GomEntryNotFoundException gx) {
                    pGom.deleteEntry(pPath);
                    Logger.v(LOG_TAG, "pGomObserver.onEntryDeleted( ", pPath, " )");
                    pGomObserver.onEntryDeleted(pPath, null);
                } catch (Exception ex) {
                    pErrorHandler.handle(ex);
                } catch (Throwable t) {
                    Logger.v(LOG_TAG, t.toString());
                }
            }
        }).start();

        return rec;
    }

    // JUST REGISTER, DO NOT GET NOTIFIED IMMEDIATELY NOTIFY
    public static BroadcastReceiver registerObserver(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom) throws IOException,
            IpAddressNotFoundException {

        return registerObserverAndNotify(pPath, pGomObserver, pGom, new ErrorHandler() {

            @Override
            public void handle(Exception pE) {
                Logger.e(LOG_TAG, "*******", pE);
            }

        });

    }

    public static BroadcastReceiver registerObserver(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom,
            final ErrorHandler pErrorHandler) throws IOException, IpAddressNotFoundException {

        return registerObserver(pPath, pGomObserver, pGom, false, pErrorHandler);
    }

    /**
     * Use this with care, the callback will not immediately return with the
     * latest data
     * 
     * @param pPath
     *            Gom path to observe
     * @param pGomObserver
     *            the observer??? Don't know!
     * @param pErrorHandler
     *            pass your own error handling code to the registration thread
     * 
     */
    public static BroadcastReceiver registerObserver(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom, final boolean pBubbleUp,
            final ErrorHandler pErrorHandler) throws IOException, IpAddressNotFoundException {

        BroadcastReceiver rec = createBroadcastReceiver(pPath, pGomObserver, pBubbleUp);

        if (!pGom.isBound()) {
            throw new IllegalStateException("GomProxyHelper " + pGom.toString() + " is not bound!");
        }

        new Thread(new Runnable() {
            public void run() {

                try {
                    putObserverToGom(pPath, pBubbleUp);
                } catch (Exception ex) {
                    pErrorHandler.handle(ex);
                } catch (Throwable t) {
                    Logger.v(LOG_TAG, t.toString());
                }
            }
        }).start();

        return rec;
    }

    /**
     * @param pPath
     * @throws IOException
     */
    public static HttpResponse putObserverToGom(String pPath) throws IOException,
            IpAddressNotFoundException {

        return putObserverToGom(pPath, false);
    }

    public static HttpResponse putObserverToGom(String pPath, boolean pWithBubbleUp)
            throws IOException, IpAddressNotFoundException {

        String observerId = getObserverId();

        HashMap<String, String> formData = new HashMap<String, String>();

        InetAddress myIp = NetworkHelper.getStagingIp();
        String ip = myIp.getHostAddress();
        String callbackUrl = "http://" + ip + ":" + Constants.Network.DEFAULT_PORT
                + Constants.Network.GNP_TARGET;
        formData.put("callback_url", callbackUrl);
        formData.put("accept", "application/json");

        if (!pWithBubbleUp) {

            // constrain gom notifications using a regexp, so that for each
            // entry we get only
            // events on that entry and one level below, i.e. for nodes events
            // on immediate subnodes
            // or attributes (no bubbling up from way below)
            // the structure of the regexp is
            // base path + optional segment consisting of separator (/, :) +
            // node/attribute name
            // (no separator allowed in names)
            formData.put("uri_regexp", createRegularExpression(pPath));
        }

        String observerPath = getObserverPathFor(pPath);
        String observerUri = Constants.Gom.URI + observerPath;

        Logger.d(LOG_TAG, "posting observer for GOM entry " + pPath + " to " + observerUri
                + " for callback " + callbackUrl, " use bubble up? ", pWithBubbleUp);

        // Deactivated because the implementation does not unsubscribe registerd
        // observers

        HttpResponse response = GomHttpWrapper.putNodeWithAttributes(
                observerUri + "/" + observerId, formData);
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() >= 300) {

            throw new IOException("Unexpected HTTP status code: " + status.getStatusCode());
        }

        String result = HttpHelper.extractBodyAsString(response.getEntity());
        Logger.v(LOG_TAG, "result of post to observer: ", observerUri, " ", result);

        return response;

    }

    private static BroadcastReceiver createBroadcastReceiver(final String pPath,
            final GomObserver pGomObserver, final boolean pBubbleUp) {
        // TODO Auto-generated method stub

        if (pPath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        // reg ex for paths of entries in which we are interested
        // i.e. the path we observe or one level below
        final String regEx = createRegularExpression(pPath);

        BroadcastReceiver br = new BroadcastReceiver() {

            @Override
            public void onReceive(Context pArg0, Intent pArg1) {

                Logger.d(LOG_TAG, "BroadcastReceiver for ", pPath, " onReceive with intent: ",
                        pArg1.toString(), " - with bubble up? ", pBubbleUp);
                Logger.v(LOG_TAG, "BroadcastReceiver for ", pPath, " - getting path: ", pArg1
                        .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH));

                String notificationPath = pArg1
                        .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH);
                if (notificationPathIsObservedByMe(notificationPath)) {

                    Logger.d(LOG_TAG, "BroadcastReceiver ", pPath,
                            " , ok, the path is relevant to me");
                    Logger.v(LOG_TAG, "BroadcastReceiver ", pPath, "  - data: ", pArg1
                            .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING));

                    String jsnStr = pArg1
                            .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING);
                    JSONObject data;
                    try {
                        data = new JSONObject(jsnStr);

                    } catch (JSONException e) {

                        throw new RuntimeException(e);
                    }

                    String operation = pArg1
                            .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION);
                    if ("create".equals(operation)) {

                        Logger.v(LOG_TAG, "BroadcastReceiver ", pPath,
                                " , it's a CREATE notification");
                        pGomObserver.onEntryCreated(pPath, data);

                    } else if ("update".equals(operation)) {

                        Logger.v(LOG_TAG, "BroadcastReceiver ", pPath,
                                " , it's an UPDATE notification");
                        pGomObserver.onEntryUpdated(pPath, data);

                    } else if ("delete".equals(operation)) {

                        Logger.v(LOG_TAG, "BroadcastReceiver ", pPath,
                                " , it's a DELETE notification");
                        pGomObserver.onEntryDeleted(pPath, data);

                    } else {

                        Logger.w(LOG_TAG, "BroadcastReceiver ", pPath,
                                " , GOM notification with unknown operation: ", operation);
                    }
                } else {

                    Logger.d(LOG_TAG, "BroadcastReceiver ", pPath, " , path is not relevant to me");
                }
            }

            public boolean notificationPathIsObservedByMe(String pNotificationPath) {

                return (pBubbleUp && GomReference.isSelfOrAncestorOf(pPath, pNotificationPath))
                        || (Pattern.matches(regEx, pNotificationPath));
            }
        };

        return br;
    }

    /**
     * Convenience Method which returns the path to the node in which all the
     * observers of the given path reside.
     * 
     * @param pGomEntryPath
     * @return
     */
    public static String getObserverPathFor(String pGomEntryPath) {

        String base = Constants.Gom.OBSERVER_BASE_PATH;

        int lastSlash = pGomEntryPath.lastIndexOf("/");
        String lastSegment = pGomEntryPath.substring(lastSlash);

        if (lastSegment.contains(":")) {

            // it's an attribute
            int colon = pGomEntryPath.lastIndexOf(":");
            String parentNodePath = pGomEntryPath.substring(0, colon);
            String attrName = pGomEntryPath.substring(colon + 1);

            return base + parentNodePath + "/" + attrName;

        } else {

            // it's a node
            return base + pGomEntryPath;
        }
    }

    public static String getObserverUriFor(String pAttrPath) {

        return Constants.Gom.URI + getObserverPathFor(pAttrPath);
    }

    public static String createRegularExpression(String pPath) {

        return "^" + pPath + "([/:]([^/:])*)?$";
    }

    public static String getObserverId() {

        return encodeObserverId(DeviceConfiguration.load().getDevicePath());
    }

    static String encodeObserverId(String pDevicePath) {

        return "." + pDevicePath.replaceAll("/", "_").toLowerCase();

    }

}
