package com.artcom.y60.gom;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONObject;

import com.artcom.y60.BindingException;
import com.artcom.y60.Constants;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandler;
import com.artcom.y60.IpAddressNotFoundException;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;
import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpHelper;
import com.artcom.y60.http.HttpServerException;

import android.content.BroadcastReceiver;

public class GomNotificationHelper {

    private static final String LOG_TAG = "GomNotificationHelper";

    // REGISTER AND NOTIFY
    public static BroadcastReceiver createObserverAndNotify(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom) throws IOException{

        return createObserverAndNotify(pPath, pGomObserver, pGom, new ErrorHandler() {
            @Override
            public void handle(Exception pE) {
                Logger.e(LOG_TAG, pE);
            }
        });
    }

    public static BroadcastReceiver createObserverAndNotify(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom, boolean pBubbleUp)
            throws IOException {

        return createObserverAndNotify(pPath, pGomObserver, pGom, pBubbleUp, new ErrorHandler() {
            @Override
            public void handle(Exception pE) {
                Logger.e(LOG_TAG, pE);
            }
        });
    }

    // JUST REGISTER, DO NOT GET NOTIFIED IMMEDIATELY NOTIFY
    public static BroadcastReceiver createObserver(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom) throws IOException{

        return registerObserver(pPath, pGomObserver, pGom, new ErrorHandler() {
            @Override
            public void handle(Exception pE) {
                Logger.e(LOG_TAG, pE);
            }
        });
    }

    public static BroadcastReceiver createObserverAndNotify(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom,
            final ErrorHandler pErrorHandler) throws IOException {
        return createObserverAndNotify(pPath, pGomObserver, pGom, true, pErrorHandler);
    }

    /**
     * Register a GOM observer for a given path. Filtering options are currently not supported.
     * 
     * @param pPath
     *            Gom path to observe
     * @param pGomObserver
     *            the observer??? Don't know!
     * @param pErrorHandler
     *            pass your own error handling code to the registration thread
     */
    public static BroadcastReceiver createObserverAndNotify(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom, final boolean pBubbleUp,
            final ErrorHandler pErrorHandler) throws IOException {
        BroadcastReceiver receiver = createBroadcastReceiver(pPath, pGomObserver, pBubbleUp, pGom);
        if (!pGom.isBound() || pGom == null) {
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
                } catch (BindingException be) {
                    Logger.w(LOG_TAG, "GomProxy was unbound while processing asynchronous thread");
                } catch (Exception ex) {
                    pErrorHandler.handle(ex);
                } catch (Throwable t) {
                    Logger.v(LOG_TAG, t.toString());
                }
            }
        }).start();
        return receiver;
    }

    /**
     * Register a GOM observer for a given path. Filtering options are currently not supported.
     * 
     * @param pPath
     *            Gom path to observe
     * @param pGomObserver
     *            the observer??? Don't know!
     * @param pErrorHandler
     *            pass your own error handling code to the registration thread
     */
    public static BroadcastReceiver createObserverAndNotifyLazy(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom, final boolean pBubbleUp,
            final ErrorHandler pErrorHandler) throws IOException {
        BroadcastReceiver receiver = createBroadcastReceiver(pPath, pGomObserver, pBubbleUp, pGom);
        if (!pGom.isBound() || pGom == null) {
            throw new IllegalStateException("GomProxyHelper " + pGom.toString() + " is not bound!");
        }

        new Thread(new Runnable() {
            public void run() {
                try {
                    putObserverToGom(pPath, pBubbleUp);
                    boolean hasInCache = pGom.hasInCache(pPath);
                    if (!hasInCache) {
                        pGom.refreshEntry(pPath);
                    }
                    GomEntry entry = pGom.getEntry(pPath);
                    if (entry instanceof GomNode) {
                        ((GomNode) entry).entries();
                    }
                    Logger.v(LOG_TAG, "in lazy gnp: , update: ", entry.toJson(), " ppath: ", pPath,
                            " was in cache?: ", hasInCache);
                    pGomObserver.onEntryUpdated(pPath, entry.toJson());
                } catch (GomEntryNotFoundException gx) {
                    pGom.deleteEntry(pPath);
                    Logger.v(LOG_TAG, "pGomObserver.onEntryDeleted( ", pPath, " )");
                    pGomObserver.onEntryDeleted(pPath, null);
                } catch (BindingException be) {
                    Logger.w(LOG_TAG, "GomProxy was unbound while processing asynchronous thread");
                } catch (Exception ex) {
                    pErrorHandler.handle(ex);
                } catch (Throwable t) {
                    Logger.v(LOG_TAG, t.toString());
                }
            }
        }).start();
        return receiver;
    }

    /**
     * Register a GOM observer for a given path. Filtering options are currently not supported.
     * 
     * @param path
     *            Gom path to observe
     * @param pGomObserver
     *            the observer??? Don't know!
     * @param pErrorHandler
     *            pass your own error handling code to the registration thread
     */
    public static BroadcastReceiver createObserverAndNotifyWithoutCache(final String path,
            final GomObserver pGomObserver, final GomProxyHelper pGom, final boolean pBubbleUp,
            final ErrorHandler pErrorHandler) throws IOException {
        BroadcastReceiver receiver = createBroadcastReceiver(path, pGomObserver, pBubbleUp, pGom);
        if (!pGom.isBound() || pGom == null) {
            throw new IllegalStateException("GomProxyHelper " + pGom.toString() + " is not bound!");
        }

        new Thread(new Runnable() {
            public void run() {

                try {
                    putObserverToGom(path, pBubbleUp);
                } catch (Exception e) {
                    pErrorHandler.handle(e);
                }

                JSONObject response;
                try {
                    response = HttpHelper.getJson(Constants.Gom.URI + path);
                    Logger.v(LOG_TAG, "without cache: immediate response: ", response);
                    pGomObserver.onEntryUpdated(path, response);
                } catch (Exception e) {
                    pGomObserver.onEntryDeleted(path, null);
                    Logger.e(LOG_TAG, e);
                }

            }
        }).start();
        return receiver;
    }

    public static BroadcastReceiver registerObserver(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom,
            final ErrorHandler pErrorHandler) throws IOException {
        return registerObserver(pPath, pGomObserver, pGom, true, pErrorHandler);
    }

    /**
     * Use this with care, the callback will not immediately return with the latest data
     * 
     * @param pPath
     *            Gom path to observe
     * @param pGomObserver
     *            the observer??? Don't know!
     * @param pErrorHandler
     *            pass your own error handling code to the registration thread
     */
    public static BroadcastReceiver registerObserver(final String pPath,
            final GomObserver pGomObserver, final GomProxyHelper pGom, final boolean pBubbleUp,
            final ErrorHandler pErrorHandler) throws IOException {

        BroadcastReceiver rec = createBroadcastReceiver(pPath, pGomObserver, pBubbleUp, pGom);

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
            HttpClientException, IpAddressNotFoundException, HttpServerException {
        return putObserverToGom(pPath, false);
    }

    public static HttpResponse putObserverToGom(String pPath, boolean pWithBubbleUp)
            throws IOException, HttpClientException, IpAddressNotFoundException,
            HttpServerException {
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

    private static BroadcastReceiver createBroadcastReceiver(String pPath,
            GomObserver pGomObserver, boolean pBubbleUp, GomProxyHelper pGom) {
        if (pPath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        return new GomNotificationBroadcastReceiver(pPath, pGomObserver, pBubbleUp, pGom);
    }

    /**
     * Convenience Method which returns the path to the node in which all the observers of the given
     * path reside.
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
