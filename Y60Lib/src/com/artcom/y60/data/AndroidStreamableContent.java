package com.artcom.y60.data;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.net.Uri;

public abstract class AndroidStreamableContent implements StreamableContent {

    ContentResolver      mContentResolver;
    private Uri          mContentResolverUri;
    String               mContentType;
    private OutputStream mOutputStream;

    public AndroidStreamableContent(ContentResolver pContentResolver) {
        mContentResolver = pContentResolver;
    }

    public Uri getContentResolverUri() {
        return mContentResolverUri;
    }

    protected void setCotentResolverUri(Uri dataLocation) throws FileNotFoundException {
        mContentResolverUri = dataLocation;
        mOutputStream = mContentResolver.openOutputStream(getContentResolverUri());
    }

    abstract public String getContentType();

    @Override
    public long getStreamLength() {
        return 0;
    }

    public InputStream getInputStream() throws IOException {
        BufferedInputStream stream = new BufferedInputStream(mContentResolver
                .openInputStream(getContentResolverUri()));
        return stream;

    }

    public OutputStream getOutputStream() throws FileNotFoundException {
        if (mOutputStream == null) {
            throw new FileNotFoundException(
                    "Outputstream is null, since content resolver uri is not yet set.");
        }
        return mOutputStream;
    }

    public void write(byte[] buffer, int offset, int count) throws IOException {
        getOutputStream().write(buffer, offset, count);
    }
}
