package com.artcom.y60;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpResponse;

import com.artcom.y60.http.HttpException;

public class RciLauncher extends SlotLauncher {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "RciLauncher";

    // Instance Variables ------------------------------------------------

    private String              mResourceUri;

    private String              mRciUri;

    private String              mTarget;

    private String              mAction;

    private String              mOwnerPath;

    public RciLauncher(String pTarget, String pAction, String pResourceUri, String pRciUri,
            String pOwnerPath) {

        mResourceUri = pResourceUri;
        mRciUri = pRciUri;
        mTarget = pTarget;
        mAction = pAction;
        mOwnerPath = pOwnerPath;
    }

    @Override
    public void launch() {

        Logger.v(LOG_TAG, "shifting - resource uri: ", mResourceUri, ", rci uri: ", mRciUri,
                ", target: ", mTarget, ", action: ", mAction, " ownerPath: ", mOwnerPath);

        HashMap<String, String> args = new HashMap<String, String>();
        args.put("target", mTarget);
        args.put("arguments", "action=" + mAction + "&file_uri=" + mResourceUri + "&sender="
                + mOwnerPath);

        try {

            Logger.d(LOG_TAG, "Send: ", HttpHelper.urlEncode(args));
            HttpResponse res = HttpHelper.postUrlEncoded(mRciUri, args);
            Logger.v(LOG_TAG, res.getStatusLine());

        } catch (IOException e) {

            ErrorHandling.signalNetworkError(LOG_TAG, e, getContext());
        } catch (HttpException e) {

            ErrorHandling.signalHttpError(LOG_TAG, e, getContext());
        }
    }
}
