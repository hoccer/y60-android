package com.artcom.y60.hoccer;

public interface HocEventListener {

    public void onDataExchanged(HocEvent pHocEvent);

    public void onFeedback(String pMessage);

    public void onError(HocEventException e);

    public void onLinkEstablished();

}
