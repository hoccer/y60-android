package com.artcom.y60.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class HTTPHelper {

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
		return extractBody(result.getEntity());
	}

	public static String get(String uri) {

		HttpGet get = new HttpGet(uri);
		HttpEntity entity = executeHTTPMethod(get).getEntity();
		String result = extractBody(entity);
		Log.v("HTTPHelper", "got: " + result);
		return result;
	}

	private static String extractBody(HttpEntity entity) {
		try {
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			entity.writeTo(ostream);
			Log.v("HTTPHelper", ostream.toString());
			return ostream.toString();
		} catch (IllegalStateException e) {
			Log.e("HTTPHelper", "illegal state: " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			Log.e("HTTPHelper", "io: " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
	}

	private static void insertXML(String body, HttpEntityEnclosingRequestBase method) {
		StringEntity entity;
		try {
			entity = new StringEntity(body);
			method.setEntity(entity);
			method.addHeader("Content-Type", "text/xml");
			method.addHeader("Accept", "text/xml");			
		} catch (UnsupportedEncodingException e) {
			Log.e("HTTPHelper", "unsupported encoding: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static HttpResponse executeHTTPMethod(HttpRequestBase method) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		// Log redirects
		httpclient.setRedirectHandler(new DefaultRedirectHandler(){
            @Override
            public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
                URI uri = super.getLocationURI(response, context);
                Log.v("HTTPHelper", response.getStatusLine().getStatusCode() + " redirect to: " + uri);
                return uri;
            }
        });

		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_0,
				500, "Client Error");
		try {
			response = httpclient.execute(method);
		} catch (ClientProtocolException e) {
			Log.e("HTTPHelper", "protocol exception: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("HTTPHelper", "io exception: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e("HTTPHelper", "unknown exception:" + e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

}
