package com.artcom.y60;

import java.util.HashMap;

import android.net.Uri;

public class RciLauncher extends SlotLauncher {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "RciLauncher";

    // Instance Variables ------------------------------------------------

    private Uri                 mResourceUri;

    private Uri                 mRciUri;

    private String              mTarget;

    private String              mAction;

    public RciLauncher(String pTarget, String pAction, Uri pResourceUri, Uri pRciUri) {

        mResourceUri = pResourceUri;
        mRciUri = pRciUri;
        mTarget = pTarget;
        mAction = pAction;
    }

    public void launch() {

        Logger.v(LOG_TAG, "shifting - resource uri: ", mResourceUri, ", rci uri: ", mRciUri,
                ", target: ", mTarget, ", action: ", mAction);

        HashMap<String, String> args = new HashMap<String, String>();
        args.put("target", mTarget);
        args.put("arguments", "action=" + mAction + "&uri=" + mResourceUri.toString());

        try {

            Logger.d(LOG_TAG, "Send: ", HttpHelper.urlEncode(args));
            HttpHelper.postUrlEncoded(mRciUri.toString(), args);

        } catch (RuntimeException e) {

            ErrorHandling.signalNetworkError(LOG_TAG, e, getContext());
        }
    }
}
