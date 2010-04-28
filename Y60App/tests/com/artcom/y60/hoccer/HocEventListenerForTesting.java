package com.artcom.y60.hoccer;

public class HocEventListenerForTesting implements HocEventListener {
    
    public boolean hadError;
    public boolean wasSuccessful;
    
    @Override
    public void onError(HocEventException e) {
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
    
}
