package com.artcom.y60;

import java.util.HashMap;

import android.net.Uri;

public class RciLauncher extends SlotLauncher {
    
    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = RciLauncher.class.getName();
    
    
    // Instance Variables ------------------------------------------------

    private Uri mResourceUri;
    
    private Uri mRciUri;
    
    private String mTarget;

    public RciLauncher(String pTarget, Uri pResourceUri, Uri pRciUri) {
        
        mResourceUri = pResourceUri;
        mRciUri = pRciUri;
        mTarget = pTarget;
    }

    public void launch() {
        
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("target", mTarget);
        args.put("arguments", "action=play&uri=" + mResourceUri.toString());

        try {
            
            Logger.d(LOG_TAG, "Send: ", HTTPHelper.urlEncode(args));
            HTTPHelper.postUrlEncoded(mRciUri.toString(), args);
            
        } catch (RuntimeException e) {
            
            ErrorHandling.signalNetworkError(LOG_TAG, e, getContext());
        }
    }
}
