package com.artcom.y60;

import junit.framework.TestCase;

import org.apache.http.HttpResponse;

import com.artcom.y60.http.HttpClientException;

public class HttpHelperTest extends TestCase {

    private static final String LOG_TAG = "HttpHelperTest";

    public void test404() throws Exception {

        try {
            HttpResponse response = HttpHelper.get("http://artcom.de/dieseurlgibtsgarnicht");
            Logger.v(LOG_TAG, response.getStatusLine().getStatusCode(), " - ", response
                            .getStatusLine());
            fail("expected a 404 exception!");

        } catch (HttpClientException ex) {

            assertEquals("expected a 404 exception", 404, ex.getStatusCode());
        }
    }
}
