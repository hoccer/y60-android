package com.artcom.y60.hoccer;

public interface HocEventListener {

    public void onDataExchanged(HocEvent hoc);

    public void onFeedback(String message);

    public void onTransferProgress(double progress);

    public void onError(HocEventException e);

    public void onLinkEstablished();

    public void onAbort(HocEvent hoc);

}
