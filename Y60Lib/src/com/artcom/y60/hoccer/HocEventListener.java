package com.artcom.y60.hoccer;

import java.util.UUID;

public interface HocEventListener {

    public void onUploadFinished(UUID uuid);

    public void onUploadFeedback(String pMessage);

    public void onUploadError(Throwable e, UUID uuid);

    public void onContentUploadStart();

}
