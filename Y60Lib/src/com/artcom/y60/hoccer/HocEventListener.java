package com.artcom.y60.hoccer;

import java.util.UUID;

public interface HocEventListener {

    public void onSuccess(UUID uuid);

    public void onProgress(String pMessage);

    public void onError(Throwable e, UUID uuid);

    public void onLinkEstablished();

}
