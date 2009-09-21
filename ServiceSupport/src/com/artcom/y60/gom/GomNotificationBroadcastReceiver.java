package com.artcom.y60.gom;

import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Constants;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;

public class GomNotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "GomNotificationBroadcastReceiver";

    private String              mPath;

    private boolean             mBubbleUp;

    private GomObserver         mGomObserver;

    private String              mRegEx;

    private GomProxyHelper      mGomProxy;

    // Constructors ------------------------------------------------------

    public GomNotificationBroadcastReceiver(String pPath, GomObserver pObserver, boolean pBubbleUp,
            GomProxyHelper pGomProxy) {

        mPath = pPath;
        mGomObserver = pObserver;
        mBubbleUp = pBubbleUp;
        mGomProxy = pGomProxy;

        // reg ex for paths of entries in which we are interested
        // i.e. the path we observe or one level below
        mRegEx = GomNotificationHelper.createRegularExpression(mPath);
        Logger.v(LOG_TAG, "created GomNotificationBroadcastReceiver for ", mPath);
    }

    @Override
    public void onReceive(Context pContext, Intent pIntent) {

        // Logger.d(LOG_TAG, "BroadcastReceiver ", this, " for ", mPath,
        // " onReceive with intent: ",
        // pIntent.toString(), " - with bubble up? ", mBubbleUp);
        // Logger.v(LOG_TAG, "BroadcastReceiver for ", mPath,
        // " - getting path: ", pIntent
        // .getStringExtra(IntentExtraKeys.NOTIFICATION_PATH));

        String notificationPath = pIntent.getStringExtra(IntentExtraKeys.NOTIFICATION_PATH);
        if (notificationPathIsObservedByMe(notificationPath)) {

            Logger.d(LOG_TAG, "BroadcastReceiver ", mPath, " , ok, the path is relevant to me");

            String jsnStr = pIntent.getStringExtra(IntentExtraKeys.NOTIFICATION_DATA_STRING);
            JSONObject data;
            try {
                data = new JSONObject(jsnStr);

                String operation = pIntent.getStringExtra(IntentExtraKeys.NOTIFICATION_OPERATION);
                String path = getAffectedEntryPath(data);

                if ("create".equals(operation)) {
                    // Logger.v(LOG_TAG, "BroadcastReceiver ", mPath,
                    // " , it's a CREATE notification");
                    // Logger.v(LOG_TAG, "DER PFFFFFFFFFFFFFFFFFFFFFFFFFAD: ",
                    // path);
                    // Logger.v(LOG_TAG, "DER DAAAAAAAAAAAAAAAAAAAAAAATEN: ",
                    // data);
                    mGomProxy.createEntry(path, data.toString());
                    // Logger.v(LOG_TAG, "dannnnaaaach DER : ");
                    mGomObserver.onEntryCreated(path, data);

                    // update proxy
                } else if ("update".equals(operation)) {
                    // Logger
                    // .v(LOG_TAG, "BroadcastReceiver ", mPath,
                    // " , it's an UPDATE notification");
                    // Logger.v(LOG_TAG, "DER PFFFFFFFFFFFFFFFFFFFFFFFFFAD: ",
                    // path);
                    // Logger.v(LOG_TAG, "DER DAAAAAAAAAAAAAAAAAAAAAAATEN: ",
                    // data);
                    mGomProxy.updateEntry(path, data.toString());
                    mGomObserver.onEntryUpdated(path, data);

                    // update proxy
                } else if ("delete".equals(operation)) {
                    Logger.v(LOG_TAG, "BroadcastReceiver ", mPath, " , it's a DELETE notification");
                    Logger.v(LOG_TAG, "DER PFFFFFFFFFFFFFFFFFFFFFFFFFAD: ", path);
                    Logger.v(LOG_TAG, "DER DAAAAAAAAAAAAAAAAAAAAAAATEN: ", data);

                    mGomProxy.deleteEntry(path);
                    mGomObserver.onEntryDeleted(path, data);
                    // update proxy
                } else {

                    Logger.w(LOG_TAG, "BroadcastReceiver ", mPath,
                            " , GOM notification with unknown operation: ", operation);
                }

            } catch (JSONException e) {

                throw new RuntimeException(e);
            }
        } else {

            Logger.d(LOG_TAG, "BroadcastReceiver ", mPath, " , path is not relevant to me");
        }
    }

    private String getAffectedEntryPath(JSONObject pData) throws JSONException {

        if (pData.has(Constants.Gom.Keywords.ATTRIBUTE)) {

            JSONObject attrJson = pData.getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
            return attrJson.getString(Constants.Gom.Keywords.NODE) + ":"
                    + attrJson.getString(Constants.Gom.Keywords.NAME);

        } else if (pData.has(Constants.Gom.Keywords.NODE)) {

            JSONObject nodeJson = pData.getJSONObject(Constants.Gom.Keywords.NODE);
            return nodeJson.getString(Constants.Gom.Keywords.URI);
        }

        return null;
    }

    public boolean notificationPathIsObservedByMe(String pNotificationPath) {

        return (mBubbleUp && GomReference.isSelfOrAncestorOf(mPath, pNotificationPath))
                || (Pattern.matches(mRegEx, pNotificationPath));
    }

}
