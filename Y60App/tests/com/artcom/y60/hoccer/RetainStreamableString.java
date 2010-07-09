package com.artcom.y60.hoccer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.artcom.y60.data.StreamableString;

public class RetainStreamableString extends StreamableString {

    private boolean mReleaseInputStream = false;

    public RetainStreamableString(String text) throws IOException {
        super(text);
    }

    public void releaseInputStream() {
        mReleaseInputStream = true;
    }

    @Override
    public InputStream openInputStream() {
        while (!mReleaseInputStream) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        return new ByteArrayInputStream(mData.toByteArray());
    }

}
