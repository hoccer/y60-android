package com.artcom.y60.infrastructure;

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
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

public class HTTPHelper {

	// Constants ---------------------------------------------------------

	private static final String LOG_TAG = "HTTPHelper";

	// Static Methods ----------------------------------------------------

	public static String putXML(String uri, String body) {
		HttpPut put = new HttpPut(uri);
		insertXML(body, put);
		StatusLine statusLine = executeHTTPMethod(put).getStatusLine();
		return statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
	}

	public static String postXML(String uri, String body) {
		HttpPost post = new HttpPost(uri);
		insertXML(body, post);
		HttpResponse result = executeHTTPMethod(post);

		if (result.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Execution of HTTP Method returend "
					+ result.getStatusLine());
		}

		return extractBodyAsString(result.getEntity());
	}

	public static InputStream getAsInStream(String uri_string)
			throws IOException {

		HttpGet get = new HttpGet(uri_string);
		HttpEntity entity = executeHTTPMethod(get).getEntity();

		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		entity.writeTo(ostream);

		ByteArrayInputStream istream = new ByteArrayInputStream(ostream
				.toByteArray());
		return istream;
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

		Log.v(LOG_TAG, "Storing content under filename " + filename);

		try {
			FileOutputStream fstream = new FileOutputStream(filename);
			entity.writeTo(fstream);
		} catch (IllegalStateException e) {
			Log.e(LOG_TAG, "illegal state: " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (IOException e) {
			Log.e(LOG_TAG, "io: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	public static JSONObject getJson(String pUrl) throws JSONException {

		String url = toJsonUrl(pUrl);
		String result = get(url);

		Log.v(LOG_TAG, "JSON result: " + result);

		return new JSONObject(result);
	}

	public static Uri getLocationHeader(String url) {

		HttpHead head = new HttpHead(url);
		HttpResponse response = executeHTTPMethod(head);
		if (response.containsHeader("Location")) {
			Header locationHeader = response.getFirstHeader("Location");
			Log.v(LOG_TAG, "Location: " + locationHeader.getValue());
			return Uri.parse(locationHeader.getValue());
		} else {
			Log.v(LOG_TAG, "HTTP response does not contain a Location header");
		}
		throw new RuntimeException("Could not retrive location header.");
	}

	public static StatusLine putUrlEncoded(String pUrl,
			Map<String, String> pData) {

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

		Log.v(LOG_TAG, "PUT " + pUrl + " with body " + body);

		insertUrlEncoded(body, put);
		return executeHTTPMethod(put).getStatusLine();
	}

	public static StatusLine putUrlEncoded(String pUrl, String pData) {

		HttpPut put = new HttpPut(pUrl);
		insertUrlEncoded(URLEncoder.encode(pData), put);
		return executeHTTPMethod(put).getStatusLine();
	}

	
	
	// Private Instance Methods ------------------------------------------
	
	private static HttpEntity getAsHttpEntity(Uri uri) {
	    
        Log.v(LOG_TAG, "get('" + uri + "')");
        HttpGet get = new HttpGet(uri.toString());
        HttpResponse response = executeHTTPMethod(get);
        // Check for some common errors. Consider throwing an exception here?
        // TODO
        StatusLine status = response.getStatusLine();
        int statusCode = status.getStatusCode();
        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            Log.w(LOG_TAG, "Request came back with 501 Internal Server Error");
            throw new RuntimeException("Request came back with 501 Internal Server Error");
        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
            Log.w(LOG_TAG, "Request came back with 404 Not Found");
            throw new RuntimeException("Request came back with 404 Not Found");
        }
        
        return response.getEntity();
	}

	private static String extractBodyAsString(HttpEntity entity) {
	    
		try {
		    return extractBody(entity).toString();
		} catch (IllegalStateException e) {
			Log.e(LOG_TAG, "illegal state: " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			Log.e(LOG_TAG, "io: " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	private static byte[] extractBodyAsByteArray(HttpEntity entity) throws IllegalStateException, IOException {
	    
	    return extractBody(entity).toByteArray();
	}
	
	private static ByteArrayOutputStream extractBody(HttpEntity entity) throws IOException {
	    
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        entity.writeTo(ostream);
        Log.v(LOG_TAG, ostream.toString());
        return ostream;
	}

	private static void insertXML(String body,
			HttpEntityEnclosingRequestBase method) {

		insert(body, "text/xml", "text/xml", method);
	}

	private static void insertUrlEncoded(String pEncodedBody,
			HttpEntityEnclosingRequestBase method) {

		insert(pEncodedBody, "application/x-www-form-urlencoded",
				"application/json", method);
	}

	private static void insert(String pBody, String pContentType,
			String pAccept, HttpEntityEnclosingRequestBase pMethod) {

		Log.v(LOG_TAG, "inserting for content type " + pContentType
				+ ", body is " + pBody);

		StringEntity entity;
		try {

			entity = new StringEntity(pBody);
			pMethod.setEntity(entity);
			pMethod.addHeader("Content-Type", pContentType);
			pMethod.addHeader("Accept", pAccept);

		} catch (UnsupportedEncodingException e) {

			Log.e(LOG_TAG, "unsupported encoding: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static HttpResponse executeHTTPMethod(HttpRequestBase method) {
		DefaultHttpClient httpclient = new DefaultHttpClient();

		// Log redirects
		httpclient.setRedirectHandler(new DefaultRedirectHandler() {
			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context)
					throws ProtocolException {
				URI uri = super.getLocationURI(response, context);
				Log.v(LOG_TAG, response.getStatusLine().getStatusCode()
						+ " redirect to: " + uri);
				return uri;
			}
		});

		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				500, "Client Error");
		try {

			int retryCount = 3;
			while (retryCount > 0) {
				try {
					response = httpclient.execute(method);
					retryCount = 0;
				} catch (IOException e) {
					Log.e(LOG_TAG, "io exception: " + e.getMessage());
					retryCount--;
				}
			}

		} catch (Exception e) {
			Log.e(LOG_TAG, "unknown exception:" + e.getMessage());
			throw new RuntimeException("Error while executing HTTP method: "
					+ e.getMessage());
		}

		return response;
	}

	private static String toJsonUrl(String pUrl) {

		if (pUrl.endsWith(".xml")) {
			// an xml uri means that someone is using this method in a wrong way
			// --> fail fast
			throw new IllegalArgumentException(
					"HttpHelper was passed a URI which explicitly "
							+ "asked for a different format: '" + pUrl + "'!");
		}

		// remove trailing slashes
		if (pUrl.endsWith("/")) {

			pUrl = pUrl.substring(0, pUrl.length() - 1);
		}

		// gracefully accept format-agnostic URIs
		if (!pUrl.endsWith(".json")) {
			pUrl = pUrl + ".json";
		}

		return pUrl;
	}

	public static StatusLine postUrlEncoded(String pUrl,
			Map<String, String> pData) {
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

		HttpPost post = new HttpPost(pUrl);
		String body = tmp.toString();

		Log.v(LOG_TAG, "POST " + pUrl + " with body " + body);

		insertUrlEncoded(body, post);
		return executeHTTPMethod(post).getStatusLine();
	}

}
