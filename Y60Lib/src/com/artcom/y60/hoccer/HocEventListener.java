package com.artcom.y60.hoccer;

public interface HocEventListener {
    
    public void onSuccess(HocEvent pHocEvent);
    
    public void onProgress(String pMessage);
    
    public void onError(HocEventException e);
    
    public void onLinkEstablished();
    
}
