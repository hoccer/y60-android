package com.artcom.y60.http;

import java.io.IOException;

import org.apache.http.HttpResponse;

public class HttpServerException extends HttpException {

	private static final long serialVersionUID = -483569278630549194L;

	public HttpServerException(String pUrl, HttpResponse pResponse) throws IOException {
        super(pUrl, pResponse);
    }

    public HttpServerException(String pUrl, int pStatusCode) {
        super(pUrl, pStatusCode);
    }

}
