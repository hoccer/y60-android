package com.artcom.y60.http;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.thread.StatusHandler;

public abstract class AsyncHttpRequestWithBody extends AsyncHttpRequest {

    @SuppressWarnings("unused")
	private static final String LOG_TAG = "AsyncHttpRequestWithBody";

    public AsyncHttpRequestWithBody(String pUrl) {
        super(pUrl);
    }

    public AsyncHttpRequestWithBody(String pUrl, DefaultHttpClient pHttpClient) {
        super(pUrl, pHttpClient);
    }

    public void setBody(String pStringData) {
        HttpHelper.insert(pStringData, "text/plain", getRequest());
    }

    public void setBody(StreamableContent pStreamableData) throws IOException {

        InputStreamEntity entity = new InputStreamEntity(pStreamableData.openInputStream(),
                pStreamableData.getStreamLength());

        entity.setContentType(pStreamableData.getContentType());
        getRequest().setEntity(entity);
        getRequest().addHeader("Content-Type", pStreamableData.getContentType());
    }

    public void setBody(HttpEntity pEntity) {
        getRequest().setEntity(pEntity);
    }

    public void setBody(MultipartHttpEntity pMultipart) {
        pMultipart.registerStatusHandler(new StatusHandler() {

            @Override
            public void onSuccess() {
                // if multipart is uploaded, the whole request is almost finished
                setProgress(95);
            }

            @Override
            public void onProgress(int progress) {
                setProgress(Math.max(0, progress - 5));
            }

            @Override
            public void onError(Throwable e) {
                // TODO Auto-generated method stub

            }
        });
        getRequest().setEntity(pMultipart);

    }

    public void setBody(Map<String, String> params) {
        HttpHelper.insert(HttpHelper.urlEncodeValues(params), "application/x-www-form-urlencoded",
                getRequest());
    }

    @Override
    protected HttpEntityEnclosingRequestBase getRequest() {
        return (HttpEntityEnclosingRequestBase) super.getRequest();
    }
}
