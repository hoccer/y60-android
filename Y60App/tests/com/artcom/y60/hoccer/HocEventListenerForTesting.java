package com.artcom.y60.hoccer;

import com.artcom.y60.Logger;

public class HocEventListenerForTesting implements HocEventListener {

    private static final String LOG_TAG = "HocEventListenerForTesting";
    public boolean              hadError;
    public boolean              wasSuccessful;

    @Override
    public void onError(HocEventException e) {
        Logger.v(LOG_TAG, "error in test callback");
        hadError = true;
    }

    @Override
    public void onLinkEstablished() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFeedback(String pMessage) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDataExchanged(HocEvent pHocEvent) {
        wasSuccessful = true;
    }

    @Override
    public void onTransferProgress(double progress) {

    }

    @Override
    public void onAbort(HocEvent hoc) {
        // TODO Auto-generated method stub

    }

}
