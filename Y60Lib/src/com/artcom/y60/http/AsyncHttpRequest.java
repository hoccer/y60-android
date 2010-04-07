package com.artcom.y60.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.artcom.y60.Logger;
import com.artcom.y60.thread.ThreadedTask;

public abstract class AsyncHttpRequest extends ThreadedTask {
    
    private static final String   LOG_TAG                  = "AsyncHttpConnection";
    
    private DefaultHttpClient     mHttpClient;
    private final HttpRequestBase mRequest;
    
    private HttpResponse          mResponse                = null;
    private final OutputStream    mResultStream            = new ByteArrayOutputStream();
    
    private HttpResponseHandler   mResponseHandlerCallback = null;
    
    public AsyncHttpRequest(String pUrl) {
        mRequest = createRequest(pUrl);
        
        HttpParams httpParams = new BasicHttpParams();
        setHttpClient(new DefaultHttpClient(httpParams));
    }
    
    public AsyncHttpRequest(String pUrl, DefaultHttpClient pHttpClient) {
        mRequest = createRequest(pUrl);
        setHttpClient(pHttpClient);
    }
    
    // Only used internally to reuse code for both constructors
    private void setHttpClient(DefaultHttpClient pHttpClient) {
        mHttpClient = pHttpClient;
        
        // overwrite user-agent if it's not already customized
        Object userAgent = mHttpClient.getParams().getParameter("http.useragent");
        if (userAgent == null || userAgent.toString().contains("Apache-HttpClient")) {
            mHttpClient.getParams().setParameter("http.useragent", "Y60/1.0 Android");
        }
        
        // remember redirects
        mHttpClient.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public URI getLocationURI(HttpResponse response, HttpContext context)
                    throws ProtocolException {
                URI uri = super.getLocationURI(response, context);
                mRequest.setURI(uri);
                return uri;
            }
        });
    }
    
    public String getBodyAsString() {
        return mResultStream.toString();
    }
    
    public void setAcceptedMimeType(String pMimeType) {
        getRequest().addHeader("Accept", pMimeType);
    }
    
    public boolean isConnecting() {
        return getProgress() == 1;
    }
    
    @Override
    public void doInBackground() {
        
        setProgress(1);
        onConnecting();
        
        try {
            mResponse = mHttpClient.execute(mRequest);
        } catch (ClientProtocolException e) {
            onClientError(e);
            return;
        } catch (SocketException e) {
            onClientError(e);
            return;
        } catch (IOException e) {
            onIoError(e);
            return;
        }
        setProgress(2);
        
        if (mResponse == null) {
            onClientError(new NullPointerException("expected http response object is null"));
            return;
        }
        
        int status = mResponse.getStatusLine().getStatusCode();
        Logger.v(LOG_TAG, "response status code is ", status);
        
        try {
            InputStream is = mResponse.getEntity().getContent();
            long downloaded = 0;
            long size = mResponse.getEntity().getContentLength();
            byte[] buffer = new byte[0xFFFF];
            int len;
            while ((len = is.read(buffer)) != -1) {
                setProgress((int) (downloaded / size));
                mResultStream.write(buffer, 0, len);
                downloaded += len;
            }
        } catch (IOException e) {
            onIoError(e);
            return;
        }
        
    }
    
    public void registerResponseHandler(HttpResponseHandler responseHandler) {
        mResponseHandlerCallback = responseHandler;
    }
    
    /**
     * @return uri of the request (gets updated when redirected)
     */
    public String getUri() {
        return mRequest.getURI().toString();
    }
    
    public String getHeader(String pHeaderName) {
        return mResponse.getFirstHeader(pHeaderName).getValue();
    }
    
    abstract protected HttpRequestBase createRequest(String pUrl);
    
    protected HttpRequestBase getRequest() {
        return mRequest;
    }
    
    @Override
    protected void onPostExecute() {
        
        if (mResponse == null) {
            onClientError(new NullPointerException("response of request " + mRequest.getURI()
                    + " was null"));
            return;
        }
        
        int status = mResponse.getStatusLine().getStatusCode();
        if (status >= 400 && status < 500) {
            onClientError(status);
        } else if (status >= 500 && status < 600) {
            onServerError(status);
        } else {
            onSuccess(status);
        }
        
        super.onPostExecute();
    }
    
    protected void onIoError(IOException e) {
        Logger.e(LOG_TAG, e);
    }
    
    protected void onClientError(Exception e) {
        Logger.e(LOG_TAG, e);
    }
    
    protected void onConnecting() {
        if (mResponseHandlerCallback != null) {
            mResponseHandlerCallback.onConnecting();
        }
    }
    
    protected void onSuccess(int pStatusCode) {
        if (mResponseHandlerCallback != null) {
            mResponseHandlerCallback.onSuccess(pStatusCode, mResultStream);
        }
    }
    
    protected void onClientError(int pStatusCode) {
        Logger.e(LOG_TAG, "Error response code was ", pStatusCode);
        if (mResponseHandlerCallback != null) {
            mResponseHandlerCallback.onError(pStatusCode, mResultStream);
        }
    }
    
    protected void onServerError(int pStatusCode) {
        Logger.e(LOG_TAG, "Error response code was ", pStatusCode);
        if (mResponseHandlerCallback != null) {
            mResponseHandlerCallback.onError(pStatusCode, mResultStream);
        }
    }
}
