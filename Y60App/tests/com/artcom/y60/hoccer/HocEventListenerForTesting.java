package com.artcom.y60.hoccer;

public class HocEventListenerForTesting implements HocEventListener {
    
    public boolean hadError;
    public boolean wasSuccessful;
    
    @Override
    public void onError(Throwable e) {
        hadError = true;
    }
    
    @Override
    public void onLinkEstablished() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onProgress(String pMessage) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onSuccess(HocEvent pHocEvent) {
        wasSuccessful = true;
    }
    
}
