package com.artcom.y60.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

import com.artcom.y60.HttpHelper;
import com.artcom.y60.Logger;

public abstract class AsyncHttpRequest extends AsyncTask<String, Float, HttpResponse> {
    
    private static final String LOG_TAG       = "AsyncHttpConnection";
    
    private static String       USER_AGENT    = "Y60/1.0 Android";
    
    private HttpEntity          mData;
    
    private String              mContentType  = "application/x-www-form-urlencoded";
    private final String        mAccept       = "text/html";
    
    private HttpResponseHandler mCallbackOnUiThread;
    private HttpResponseHandler mCallbackInBackground;
    
    private final OutputStream  mResultStream = new ByteArrayOutputStream();
    
    private HttpRequestBase     mRequest;
    private HttpResponse        mResponse;
    private boolean             mHasFinished;
    
    public static void setUserAgent(String pAgent) {
        USER_AGENT = pAgent;
    }
    
    private void setData(String data) throws UnsupportedEncodingException {
        mData = new StringEntity(data);
    }
    
    private void setData(HttpEntity entity) {
        mData = entity;
    }
    
    private void setData(InputStream iStream, String pContentType) {
        mData = new InputStreamEntity(iStream, 1000);
        mContentType = pContentType;
    }
    
    public void setContentType(String pContentType) {
        mContentType = pContentType;
    }
    
    public String getData() {
        return mData.toString();
    }
    
    public void registerUiThreadedResponseHandler(HttpResponseHandler callback) {
        mCallbackOnUiThread = callback;
    }
    
    public void registerAsyncResponseHandler(HttpResponseHandler callback) {
        mCallbackInBackground = callback;
    }
    
    @Override
    protected HttpResponse doInBackground(String... params) {
        try {
            return connect(params);
        } catch (IOException e) {
            Logger.e(LOG_TAG, e);
        }
        return null;
    }
    
    public boolean hasFinished() {
        return mHasFinished;
    }
    
    public HttpResponse connect(String... params) throws IOException {
        
        String url = params[0];
        Logger.v(LOG_TAG, "connecing to ", url);
        
        mResponse = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        mRequest = createRequest(params[0]);
        mRequest.addHeader("User-Agent", USER_AGENT);
        mResponse = httpClient.execute(mRequest);
        
        int status = mResponse.getStatusLine().getStatusCode();
        Logger.v(LOG_TAG, "response is ", HttpHelper.extractBodyAsString(mResponse.getEntity()));
        
        if (status == 220) {
            // publishProgress(new Float(-1));
            
            InputStream is = mResponse.getEntity().getContent();
            long downloaded = 0;
            long size = mResponse.getEntity().getContentLength();
            byte[] buffer = new byte[0xFFFF];
            int len;
            while ((len = is.read(buffer)) != -1) {
                // publishProgress(new Float(downloaded / (float) size));
                mResultStream.write(buffer, 0, len);
                downloaded += len;
            }
        }
        
        return mResponse;
    }
    
    abstract protected HttpRequestBase createRequest(String pUrl);
    
    protected void insertData(HttpEntityEnclosingRequestBase post) {
        post.setEntity(mData);
        if (mContentType != null)
            post.addHeader("Content-Type", mContentType);
        post.addHeader("Accept", mAccept);
    }
    
    @Override
    protected void onProgressUpdate(Float... progress) {
        Logger.v("LOG_TAG", "progress ", progress[0]);
        
        if (progress[0].floatValue() == -1) {
            mCallbackOnUiThread.onSuccess(mResponse);
        }
        
        mCallbackOnUiThread.onReceiving(progress[0]);
        
        super.onProgressUpdate(progress);
    }
    
    @Override
    protected void onPostExecute(HttpResponse pResponse) {
        mHasFinished = true;
        Logger.v(LOG_TAG, "on PostExecute");
        
        if (mCallbackOnUiThread == null) {
            return;
        }
        
        int status = pResponse.getStatusLine().getStatusCode();
        if (status >= 400 && status < 500) {
            onClientError(pResponse, mCallbackOnUiThread);
        } else if (status >= 500 && status < 600) {
            onServerError(pResponse, mCallbackOnUiThread);
        } else if (pResponse != null) {
            onSuccess(pResponse, mCallbackOnUiThread);
        }
        
        super.onPostExecute(pResponse);
    }
    
    protected void onSuccess(HttpResponse pResponse, HttpResponseHandler pCallback) {
        pCallback.onError(pResponse);
    }
    
    protected void onClientError(HttpResponse pResponse, HttpResponseHandler pCallback) {
        pCallback.onError(pResponse);
    }
    
    protected void onServerError(HttpResponse pResponse, HttpResponseHandler pCallback) {
        pCallback.onError(pResponse);
    }
    
    @Override
    public void onCancelled() {
        super.onCancelled();
        try {
            mRequest.abort();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }
}
