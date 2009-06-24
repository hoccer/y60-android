package com.artcom.y60;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

public class HttpHelper {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG           = "HttpHelper";
    private static final String SCRIPT_RUNNER_Uri = "http://t-gom.service.t-gallery.act/gom/script-runner";

    // Static Methods ----------------------------------------------------

    public static String putXML(String uri, String body) {
        HttpPut put = new HttpPut(uri);
        insertXML(body, put);
        StatusLine statusLine = executeHTTPMethod(put).getStatusLine();
        return statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
    }

    public static HttpResponse putUrlEncoded(String pUrl, Map<String, String> pData) {

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
        return executeHTTPMethod(put);
    }

    public static String putText(String pUri, String pData) {
        HttpPut put = new HttpPut(pUri);
        insert(pData, "text/xml", "text/xml", put);
        StatusLine statusLine = executeHTTPMethod(put).getStatusLine();
        if (statusLine.getStatusCode() != 200) {
            throw new RuntimeException("Execution of HTTP Method PUT '" + pUri + "' returned "
                    + statusLine);
        }
        return statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
    }

    public static String postXML(String uri, String body) {
        // Logger.v(LOG_TAG, "post('" + uri + "'): " + body);
        HttpPost post = new HttpPost(uri);
        insertXML(body, post);
        HttpResponse result = executeHTTPMethod(post);

        if (result.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Execution of HTTP Method POST '" + uri + "' returned "
                    + result.getStatusLine() + " " + extractBodyAsString(result.getEntity()));
        }

        return extractBodyAsString(result.getEntity());
    }

    public static String post(String uri, String body, String pContentType, String pAccept) {

        HttpPost post = new HttpPost(uri);
        insert(body, pContentType, pAccept, post);
        HttpResponse result = executeHTTPMethod(post);

        if (result.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Execution of HTTP Method POST '" + uri + "' returned "
                    + result.getStatusLine());
        }

        return extractBodyAsString(result.getEntity());
    }

    public static InputStream getAsInStream(String uri_string) throws IOException {

        HttpGet get = new HttpGet(uri_string);
        HttpEntity entity = executeHTTPMethod(get).getEntity();

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        entity.writeTo(ostream);

        ByteArrayInputStream istream = new ByteArrayInputStream(ostream.toByteArray());
        return istream;
    }

    public static HttpResponse delete(Uri uri) {

        HttpDelete del = new HttpDelete(uri.toString());
        HttpResponse result = executeHTTPMethod(del);

        if (result.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Execution of HTTP Method DELETE '" + uri + "' returned "
                    + result.getStatusLine());
        }

        return result;
    }

    public static String get(Uri uri) {

        HttpEntity result = getAsHttpEntity(uri);
        return extractBodyAsString(result);
    }

    public static byte[] getAsByteArray(Uri uri) throws IllegalStateException, IOException {

        HttpEntity result = getAsHttpEntity(uri);
        return extractBodyAsByteArray(result);
    }

    public static String get(String uri) {
        return get(Uri.parse(uri));
    }

    public static void fetchUriToFile(String uriString, String filename) {
        HttpGet get = new HttpGet(uriString);
        HttpEntity entity = executeHTTPMethod(get).getEntity();

        // Generate a random filename to store the data under

        // Logger.v(LOG_TAG, "Storing content under filename " + filename);

        try {
            FileOutputStream fstream = new FileOutputStream(filename);
            entity.writeTo(fstream);
            fstream.flush();
            fstream.close();
        } catch (IllegalStateException e) {
            throw new RuntimeException("illegal state: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("io error: " + e.getMessage());
        }
    }

    public static JSONObject getJson(String pUrl) throws JSONException {

        String url = toJsonUrl(pUrl);
        String result = get(url);

        // Log.v(LOG_TAG, "JSON result: " + result);

        return new JSONObject(result);
    }

    public static Uri getLocationHeader(String url) {

        HttpHead head = new HttpHead(url);
        HttpResponse response = executeHTTPMethod(head);
        if (response.containsHeader("Location")) {
            Header locationHeader = response.getFirstHeader("Location");
            // Logger.v(LOG_TAG, "Location: " + locationHeader.getValue());
            return Uri.parse(locationHeader.getValue());
        } else {
            Logger.e(LOG_TAG, "HTTP response does not contain a Location header");
        }
        throw new RuntimeException("Could not retrive location header.");
    }

    public static long getSize(String url) {

        HttpHead head = new HttpHead(url);
        HttpResponse response = executeHTTPMethod(head);
        if (response.containsHeader("Content-Length")) {
            return new Long(response.getFirstHeader("Content-Length").getValue());
        } else {
            Logger.e(LOG_TAG, "HTTP response does not contain a Content-Length header");
        }
        throw new RuntimeException("Could not retrive content-length header.");
    }

    public static int getStatusCode(String url) {

        HttpHead head = new HttpHead(url);
        HttpResponse response = executeHTTPMethod(head);
        return response.getStatusLine().getStatusCode();
    }

    public static HttpResponse postUrlEncoded(String pUrl, Map<String, String> pData) {

        String body = urlEncode(pData);
        HttpPost post = new HttpPost(pUrl);

        // Logger.v(LOG_TAG, "POST " + pUrl + " with body " + body);

        insertUrlEncoded(body, post);
        return executeHTTPMethod(post);
    }

    public static JSONObject executeServerScript(String pJsStr, Map<String, String> pParams)
            throws JSONException {

        String params = urlEncode(pParams);
        String uri = SCRIPT_RUNNER_Uri + "?" + params;
        String jsonStr = post(uri, pJsStr, "text/javascript", "text/json");

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

    public static String extractBodyAsString(HttpEntity entity) {

        try {
            return extractBody(entity).toString();
        } catch (IllegalStateException e) {
            Logger.e(LOG_TAG, "illegal state: " + e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        } catch (IOException e) {
            Logger.e(LOG_TAG, "io: " + e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static byte[] extractBodyAsByteArray(HttpEntity entity) throws IllegalStateException,
            IOException {

        return extractBody(entity).toByteArray();
    }

    // Private Instance Methods ------------------------------------------

    private static HttpEntity getAsHttpEntity(Uri uri) {

        // Logger.v(LOG_TAG, "get('" + uri + "')");
        HttpGet get = new HttpGet(uri.toString());
        HttpResponse response = executeHTTPMethod(get);
        // Check for some common errors. Consider throwing an exception here?
        // TODO
        StatusLine status = response.getStatusLine();
        int statusCode = status.getStatusCode();
        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Logger.w(LOG_TAG, "Request '", uri, "' came back with 501 Internal Server Error");
            throw new RuntimeException("Request '" + uri
                    + "' came back with 501 Internal Server Error");
        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
            Logger.w(LOG_TAG, "Request '", uri, "'came back with 404 Not Found");
            throw new RuntimeException("Request '" + uri + "' came back with 404 Not Found");
        }

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

            Logger.e(LOG_TAG, "unsupported encoding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static HttpResponse executeHTTPMethod(HttpRequestBase pMethod) {
        int timeout = 1000;
        return executeHTTPMethod(pMethod, timeout);
    }

    private static HttpResponse executeHTTPMethod(HttpRequestBase pMethod, int pConnectionTimeout) {

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

        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 500, "Client Error");
        try {

            try {
                response = httpclient.execute(pMethod);
            } catch (IOException e) {
                throw new RuntimeException("Error while executing HTTP method: " + e.getMessage());
            }

        } catch (Exception e) {
            Logger.e(LOG_TAG, "unknown exception:", e.getMessage());
            throw new RuntimeException("Error while executing HTTP method: " + e.getMessage());
        }

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
