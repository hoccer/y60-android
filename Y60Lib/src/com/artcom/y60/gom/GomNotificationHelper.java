package com.artcom.y60.gom;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Constants;
import com.artcom.y60.HTTPHelper;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;

public class GomNotificationHelper {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "GomNotificationHelper";

    // Static Methods ----------------------------------------------------

    /**
     * Register a GOM observer for a given path. Filtering options are currently
     * not supported.
     */
    public static BroadcastReceiver registerObserver(String pPath, GomObserver pGomObserver)
            throws IOException {

        postObserverToGom(pPath);

        return createBroadcastReceiver(pPath, pGomObserver);
    }

    /**
     * @param pPath
     * @throws IOException
     */
    public static void postObserverToGom(String pPath) throws IOException {
        
        postObserverToGom(pPath, false);
    }
    
    public static void postObserverToGom(String pPath, boolean pWithBubbleUp) throws IOException {
        
        Map<String, String> formData = new HashMap<String, String>();
        
        InetAddress myIp        = NetworkHelper.getStagingIp();
        Logger.v(LOG_TAG, "myIp: ", myIp.toString());
        String      ip          = myIp.getHostAddress();
        Logger.v(LOG_TAG, "ip: ", ip);
        String      callbackUrl = "http://"+ip+":"+Constants.Network.DEFAULT_PORT+Constants.Network.GNP_TARGET;
        Logger.v(LOG_TAG, "callbackUrl: ", callbackUrl);
        formData.put("callback_url", callbackUrl);
        formData.put("accept", "application/json");
        
        if (!pWithBubbleUp) {
            
            // constrain gom notifications using a regexp, so that for each entry we get only
            // events on that entry and one level below, i.e. for nodes events on immediate subnodes
            // or attributes (no bubbling up from way below)
            // the structure of the regexp is
            // base path + optional segment consisting of separator (/, :) + node/attribute name
            // (no separator allowed in names)
            formData.put("uri_regexp", createRegularExpression(pPath) );
        }

        String      observerPath = getObserverPathFor(pPath);
        String      observerUri  = Constants.Gom.URI+observerPath;
        
        Logger.d(LOG_TAG, "posting observer for GOM entry "+pPath+" to "+observerUri+" for callback "+callbackUrl + " for reg exp: " + createRegularExpression(pPath) );
        
        HttpResponse response = HTTPHelper.postUrlEncoded(observerUri, formData);
        StatusLine   status   = response.getStatusLine();
        if (status.getStatusCode() >= 300) {
            
            throw new IOException("Unexpected HTTP status code: "+status.getStatusCode());
        }
        
        String result = HTTPHelper.extractBodyAsString(response.getEntity());
        Logger.v(LOG_TAG, "result of post to observer: ", result);
    }

    private static BroadcastReceiver createBroadcastReceiver(final String pPath,
            final GomObserver pGomObserver) {
        // TODO Auto-generated method stub

        if (pPath == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        BroadcastReceiver br = new BroadcastReceiver() {

            @Override
            public void onReceive(Context pArg0, Intent pArg1) {

                Logger.d(LOG_TAG, "onReceive with intent: ", pArg1.toString());
                Logger.v(LOG_TAG, " - path: ", pArg1
                        .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH));

                Logger.d(LOG_TAG, "ok, it's my path");
                Logger.v(LOG_TAG, " - operation: ", pArg1
                        .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION));
                Logger.v(LOG_TAG, " - data: ", pArg1
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

                    Logger.v(LOG_TAG, "it's a CREATE notification");
                    pGomObserver.onEntryCreated(pPath, data);

                } else if ("update".equals(operation)) {

                    Logger.v(LOG_TAG, "it's an UPDATE notification");
                    pGomObserver.onEntryUpdated(pPath, data);

                } else if ("delete".equals(operation)) {

                    Logger.v(LOG_TAG, "it's a DELETE notification");
                    pGomObserver.onEntryDeleted(pPath, data);

                } else {

                    Logger.w(LOG_TAG, "GOM notification with unknown operation: ", operation);
                }
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

    public static String createRegularExpression(String pPath) {
    
        return "^"+pPath+"([/:]([^/:])*)?$";
    }
}
