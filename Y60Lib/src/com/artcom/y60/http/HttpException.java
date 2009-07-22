package com.artcom.y60.http;

import java.io.IOException;

import org.apache.http.HttpResponse;

import com.artcom.y60.HttpHelper;

public abstract class HttpException extends Exception {

    // Static Methods ----------------------------------------------------

    /**
     * 
     */
    private static final long serialVersionUID = -7901299732283491664L;

    public static void throwIfError(String pUrl, HttpResponse pResponse)
                    throws HttpServerException, HttpClientException, IOException {

        int status = pResponse.getStatusLine().getStatusCode();
        if (status >= 400 && status < 500) {
            throw new HttpClientException(pUrl, pResponse);
        }
        if (status >= 500 && status < 600) {
            throw new HttpServerException(pUrl, pResponse);
        }
    }

    private static String createMessage(HttpResponse pResponse) throws IOException {

        if (pResponse.getEntity() == null) {
            return "<empty>";
        } else {
            String msg = HttpHelper.extractBodyAsString(pResponse.getEntity());
            return msg.substring(0, Math.min(500, msg.length() - 1));
        }
    }

    // Instance Variables ------------------------------------------------

    private int mStatusCode;

    private String mUrl;

    /**
     * HttpResponse objects are not serializable, thus the response is transient
     */
    private transient HttpResponse mResponse;

    public HttpException(String pUrl, int pStatusCode) {

        super("HTTP request for '" + pUrl + "'failed with status code " + pStatusCode
                        + " (response is not available).");
        mStatusCode = pStatusCode;
        mUrl = pUrl;
    }

    public HttpException(String pUrl, HttpResponse pResponse) throws IOException {

        super("HTTP request for '" + pUrl + "' failed with status code "
                        + pResponse.getStatusLine().getStatusCode() + " -- response is: \n"
                        + createMessage(pResponse));
        mStatusCode = pResponse.getStatusLine().getStatusCode();
        mResponse = pResponse;
        mUrl = pUrl;
    }

    // Public Instance Methods -------------------------------------------

    public int getStatusCode() {

        return mStatusCode;
    }

    /**
     * @return the HTTP response, if available -- null otherwise
     */
    public HttpResponse getHttpResponse() {

        return mResponse;
    }

    public boolean hasHttpResponse() {

        return mResponse != null;
    }
}
