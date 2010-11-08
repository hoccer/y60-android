package com.artcom.y60.http;

import java.io.IOException;

import org.apache.http.HttpResponse;

public class HttpClientException extends HttpException {

	private static final long serialVersionUID = 8333060373751327614L;

	public HttpClientException(String pUrl, HttpResponse pResponse) throws IOException {
        super(pUrl, pResponse);
    }

    public HttpClientException(String pUrl, int pStatusCode) {
        super(pUrl, pStatusCode);
    }
}