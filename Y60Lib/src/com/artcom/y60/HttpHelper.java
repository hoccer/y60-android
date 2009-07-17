package com.artcom.y60;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpException;
import com.artcom.y60.http.HttpServerException;

public class HttpHelper {

    // Constants ---------------------------------------------------------

    private static final int    PUT_TIMEOUT       = 15 * 1000;
    private static final int    POST_TIMEOUT      = 40 * 1000;
    private static final int    GET_TIMEOUT       = 5 * 1000;
    private static final String LOG_TAG           = "HttpHelper";
    private static final String SCRIPT_RUNNER_Uri = "http://t-gom.service.t-gallery.act/gom/script-runner";

    // Static Methods ----------------------------------------------------

    public static HttpResponse putXML(String uri, String body) throws IOException,
            HttpClientException, HttpServerException {
        HttpPut put = new HttpPut(uri);
        insertXML(body, put);
        return executeHTTPMethod(put, PUT_TIMEOUT);
    }

    public static HttpResponse putUrlEncoded(String pUrl, Map<String, String> pData)
            throws IOException, HttpClientException, HttpServerException {

        StringBuffer tmp = new StringBuffer();
        Set<String> keys = pData.keySet();
        int idx = 0;
        for (String key : keys) {

            tmp.append(URLEncoder.encode(key));
            tmp.append("=");
            tmp.append(URLEncoder.encode(pData.get(key)));

            idx += 1;

            if (idx < keys.size()) {

                tmp.append("&");
            }
        }

        HttpPut put = new HttpPut(pUrl);
        String body = tmp.toString();

        Logger.v(LOG_TAG, "PUT " + pUrl + " with body " + body);

        insertUrlEncoded(body, put);
        return executeHTTPMethod(put, PUT_TIMEOUT);
    }

    public static String putText(String pUri, String pData) throws IOException,
            HttpClientException, HttpServerException {
        HttpPut put = new HttpPut(pUri);
        insert(pData, "text/xml", "text/xml", put);
        StatusLine statusLine = executeHTTPMethod(put, PUT_TIMEOUT).getStatusLine();
        return statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
    }

    public static HttpResponse putFile(String pUri, File pFile, String pContentType, String pAccept)
            throws IOException, HttpClientException, HttpServerException {
        HttpPut put = new HttpPut(pUri);
        FileEntity entity = new FileEntity(pFile, pContentType);
        put.setEntity(entity);
        put.addHeader("Content-Type", pContentType);
        put.addHeader("Accept", pAccept);
        return executeHTTPMethod(put, PUT_TIMEOUT);
    }

    public static String postXML(String uri, String body) throws IOException, HttpClientException,
            HttpServerException {
        HttpPost post = new HttpPost(uri);
        insertXML(body, post);
        HttpResponse result = executeHTTPMethod(post, POST_TIMEOUT);
        return extractBodyAsString(result.getEntity());
    }

    public static String post(String uri, String body, String pContentType, String pAccept)
            throws IOException, HttpClientException, HttpServerException {

        HttpResponse response = post(uri, body, pContentType, pAccept, POST_TIMEOUT);
        return extractBodyAsString(response.getEntity());
    }

    public static HttpResponse post(String uri, String body, String pContentType, String pAccept,
            int pTimeout) throws IOException, HttpClientException, HttpServerException {

        HttpPost post = new HttpPost(uri);
        insert(body, pContentType, pAccept, post);
        return executeHTTPMethod(post, pTimeout);
    }

    public static InputStream getAsInStream(String uri_string) throws IOException,
            HttpClientException, HttpServerException {

        HttpGet get = new HttpGet(uri_string);
        HttpEntity entity = executeHTTPMethod(get).getEntity();

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        entity.writeTo(ostream);

        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        return istream;
    }

    public static Drawable getAsDrawable(String pUri) throws IOException, HttpClientException,
            HttpServerException {

        return new BitmapDrawable(getAsInStream(pUri));
    }

    public static HttpResponse delete(Uri uri) throws IOException, HttpClientException,
            HttpServerException {

        HttpDelete del = new HttpDelete(uri.toString());
        return executeHTTPMethod(del);
    }

    public static String getAsString(Uri uri) throws IOException, HttpClientException,
            HttpServerException {

        HttpEntity result = getAsHttpEntity(uri);
        return extractBodyAsString(result);
    }

    public static byte[] getAsByteArray(Uri uri) throws IllegalStateException, IOException,
            HttpClientException, HttpServerException {

        HttpEntity result = getAsHttpEntity(uri);
        return extractBodyAsByteArray(result);
    }

    public static HttpResponse get(String pUri) throws IOException, HttpClientException,
            HttpServerException {

        HttpGet get = new HttpGet(pUri);
        return executeHTTPMethod(get);
    }

    public static String getAsString(String uri) throws IOException, HttpClientException,
            HttpServerException {
        return getAsString(Uri.parse(uri));
    }

    public static HttpResponse fetchUriToFile(String uriString, String filename)
            throws IOException, HttpClientException, HttpServerException {

        HttpGet get = new HttpGet(uriString);
        HttpResponse response = executeHTTPMethod(get);
        HttpEntity entity = response.getEntity();

        // Generate a random filename to store the data
        // Logger.v(LOG_TAG, "Storing content under filename " + filename);

        FileOutputStream fstream = new FileOutputStream(filename);
        entity.writeTo(fstream);
        fstream.flush();
        fstream.close();
        return response;
    }

    public static JSONObject getJson(String pUrl) throws JSONException, IOException,
            HttpClientException, HttpServerException {

        String url = toJsonUrl(pUrl);
        String result = getAsString(url);

        // Log.v(LOG_TAG, "JSON result: " + result);

        return new JSONObject(result);
    }

    public static Uri getLocationHeader(String url) throws IOException, HttpClientException,
            HttpServerException {

        HttpHead head = new HttpHead(url);
        HttpResponse response = executeHTTPMethod(head);

        if (!response.containsHeader("Location")) {

            Logger.e(LOG_TAG, "HTTP response does not contain a Location header");
            throw new RuntimeException("Could not retrieve location header.");
        }

        Header locationHeader = response.getFirstHeader("Location");
        // Logger.v(LOG_TAG, "Location: " + locationHeader.getValue());
        return Uri.parse(locationHeader.getValue());
    }

    public static long getSize(String url) throws IOException, HttpClientException,
            HttpServerException {

        HttpHead head = new HttpHead(url);
        HttpResponse response = executeHTTPMethod(head);

        if (!response.containsHeader("Content-Length")) {
            Logger.e(LOG_TAG, "HTTP response does not contain a Content-Length header");
            throw new RuntimeException("Could not retrieve content-length header.");
        }

        return new Long(response.getFirstHeader("Content-Length").getValue());
    }

    public static int getStatusCode(String url) throws IOException {

        Logger.v(LOG_TAG, "getStatusCode for ", url);
        HttpHead head = new HttpHead(url);
        try {

            HttpResponse response = executeHTTPMethod(head);
            return response.getStatusLine().getStatusCode();

        } catch (HttpException ex) {

            Logger
                    .v(LOG_TAG, "getStatusCode caught HTTP exception (which might be expected): ",
                            ex);
            return ex.getStatusCode();
        }
    }

    public static HttpResponse postUrlEncoded(String pUrl, Map<String, String> pData)
            throws IOException, HttpClientException, HttpServerException {

        String body = urlEncode(pData);
        HttpPost post = new HttpPost(pUrl);

        // Logger.v(LOG_TAG, "POST " + pUrl + " with body " + body);

        insertUrlEncoded(body, post);
        return executeHTTPMethod(post);
    }

    public static JSONObject executeServerScript(String pJsStr, Map<String, String> pParams)
            throws JSONException, IOException, HttpClientException, HttpServerException {

        String params = urlEncode(pParams);
        String uri = SCRIPT_RUNNER_Uri + "?" + params;
        HttpResponse response = post(uri, pJsStr, "text/javascript", "text/json", 30 * 1000);
        String jsonStr = extractBodyAsString(response.getEntity());

        return new JSONObject(jsonStr);
    }

    public static String urlEncode(Map pData) {

        StringBuffer tmp = new StringBuffer();
        Set keys = pData.keySet();
        int idx = 0;
        for (Object key : keys) {

            tmp.append(URLEncoder.encode(String.valueOf(key)));
            tmp.append("=");
            tmp.append(URLEncoder.encode(String.valueOf(pData.get(key))));

            idx += 1;

            if (idx < keys.size()) {

                tmp.append("&");
            }
        }

        return tmp.toString();
    }

    public static String extractBodyAsString(HttpEntity entity) throws IOException {

        return extractBody(entity).toString();
    }

    public static byte[] extractBodyAsByteArray(HttpEntity entity) throws IllegalStateException,
            IOException {

        return extractBody(entity).toByteArray();
    }

    // Private Instance Methods ------------------------------------------

    private static HttpEntity getAsHttpEntity(Uri uri) throws IOException, HttpClientException,
            HttpServerException {

        HttpGet get = new HttpGet(uri.toString());
        HttpResponse response = executeHTTPMethod(get);
        return response.getEntity();
    }

    private static ByteArrayOutputStream extractBody(HttpEntity entity) throws IOException {

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        entity.writeTo(ostream);
        // Logger.v(LOG_TAG, ostream.toString());
        return ostream;
    }

    private static void insertXML(String body, HttpEntityEnclosingRequestBase method) {

        insert(body, "text/xml", "text/xml", method);
    }

    private static void insertUrlEncoded(String pEncodedBody, HttpEntityEnclosingRequestBase method) {

        insert(pEncodedBody, "application/x-www-form-urlencoded", "application/json", method);
    }

    private static void insert(String pBody, String pContentType, String pAccept,
            HttpEntityEnclosingRequestBase pMethod) {

        // Logger.v(LOG_TAG, "inserting for content type " + pContentType
        // + ", body is " + pBody);

        StringEntity entity;
        try {

            entity = new StringEntity(pBody);
            pMethod.setEntity(entity);
            pMethod.addHeader("Content-Type", pContentType);
            pMethod.addHeader("Accept", pAccept);

        } catch (UnsupportedEncodingException e) {

            Logger.e(LOG_TAG, "unsupported encoding: ", e);
            throw new RuntimeException(e);
        }
    }

    private static HttpResponse executeHTTPMethod(HttpRequestBase pMethod) throws IOException,
            HttpClientException, HttpServerException {
        return executeHTTPMethod(pMethod, GET_TIMEOUT);
    }

    private static HttpResponse executeHTTPMethod(HttpRequestBase pMethod, int pConnectionTimeout)
            throws IOException, HttpClientException, HttpServerException {

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, pConnectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, pConnectionTimeout);
        DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);

        // Log redirects
        httpclient.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public URI getLocationURI(HttpResponse response, HttpContext context)
                    throws ProtocolException {
                URI uri = super.getLocationURI(response, context);
                Logger
                        .v(LOG_TAG, response.getStatusLine().getStatusCode() + " redirect to: "
                                + uri);
                return uri;
            }
        });

        HttpResponse response = httpclient.execute(pMethod);
        HttpException.throwIfError(response);
        return response;
    }

    private static String toJsonUrl(String pUrl) {

        if (pUrl.endsWith(".xml")) {
            // an xml uri means that someone is using this method in a wrong way
            // --> fail fast
            throw new IllegalArgumentException("HttpHelper was passed a Uri which explicitly "
                    + "asked for a different format: '" + pUrl + "'!");
        }

        // remove trailing slashes
        if (pUrl.endsWith("/")) {

            pUrl = pUrl.substring(0, pUrl.length() - 1);
        }

        // gracefully accept format-agnostic Uris
        if (!pUrl.endsWith(".json")) {
            pUrl = pUrl + ".json";
        }

        return pUrl;
    }
}
