package com.artcom.y60.http;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

public class AsyncHttpPost extends AsyncHttpRequest {
    
    @Override
    protected HttpRequestBase createRequest(String pUrl) {
        HttpPost request = new HttpPost(pUrl);
        insertData((HttpPost) request);
        return request;
    }
}
