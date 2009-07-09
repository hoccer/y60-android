package com.artcom.y60.gom;

import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
    }

    @Override
    public void onReceive(Context pContext, Intent pIntent) {

        Logger.d(LOG_TAG, "BroadcastReceiver for ", mPath, " onReceive with intent: ", pIntent
                .toString(), " - with bubble up? ", mBubbleUp);
        Logger.v(LOG_TAG, "BroadcastReceiver for ", mPath, " - getting path: ", pIntent
                .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH));

        String notificationPath = pIntent.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH);
        if (notificationPathIsObservedByMe(notificationPath)) {

            Logger.d(LOG_TAG, "BroadcastReceiver ", mPath, " , ok, the path is relevant to me");
            Logger.v(LOG_TAG, "BroadcastReceiver ", mPath, "  - data: ", pIntent
                    .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING));

            String jsnStr = pIntent.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING);
            JSONObject data;
            try {
                data = new JSONObject(jsnStr);

            } catch (JSONException e) {

                throw new RuntimeException(e);
            }

            String operation = pIntent.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION);
            if ("create".equals(operation)) {

                Logger.v(LOG_TAG, "BroadcastReceiver ", mPath, " , it's a CREATE notification");
                mGomObserver.onEntryCreated(mPath, data);

            } else if ("update".equals(operation)) {

                Logger.v(LOG_TAG, "BroadcastReceiver ", mPath, " , it's an UPDATE notification");
                mGomObserver.onEntryUpdated(mPath, data);

            } else if ("delete".equals(operation)) {

                Logger.v(LOG_TAG, "BroadcastReceiver ", mPath, " , it's a DELETE notification");
                mGomObserver.onEntryDeleted(mPath, data);

            } else {

                Logger.w(LOG_TAG, "BroadcastReceiver ", mPath,
                        " , GOM notification with unknown operation: ", operation);
            }
        } else {

            Logger.d(LOG_TAG, "BroadcastReceiver ", mPath, " , path is not relevant to me");
        }
    }

    public boolean notificationPathIsObservedByMe(String pNotificationPath) {

        return (mBubbleUp && GomReference.isSelfOrAncestorOf(mPath, pNotificationPath))
                || (Pattern.matches(mRegEx, pNotificationPath));
    }

}
