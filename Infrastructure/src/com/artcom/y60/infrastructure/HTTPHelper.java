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
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class HTTPHelper {

    // Constants ---------------------------------------------------------

    private static final String TAG = "HTTPHelper";
    
    
    
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
		return extractBody(result.getEntity());
	}

	public static String get(String uri) {

        Log.v(TAG, "get('"+uri+"')");
		HttpGet get = new HttpGet(uri);
		HttpEntity entity = executeHTTPMethod(get).getEntity();
		String result = extractBody(entity);
		Log.v(TAG, "got: " + result);
		return result;
	}
	
	
	public static JSONObject getJson(String pUrl) throws JSONException {
	    
	    String url    = toJsonUrl(pUrl);
	    String result = get(url);
	    return new JSONObject(result);
	}
	

    public static JSONObject postJson(String pUrl, JSONObject pData) throws JSONException {
        
        String url = toJsonUrl(pUrl);
        
        Log.v(TAG, "POST to "+url+" JSON data "+pData.toString());
        
        HttpPost post = new HttpPost(url);
        insertJson(pData.toString(), post);
        HttpResponse result = executeHTTPMethod(post);
        String body = extractBody(result.getEntity());
        return new JSONObject(body);
    }

    
    public static String putJson(String pUrl, JSONObject pData) throws JSONException {
        
        Log.v(TAG, "PUT to "+pUrl+" JSON data "+pData.toString());
        
        HttpPut put = new HttpPut(pUrl);
        insertJson(pData.toString(), put);
        StatusLine statusLine = executeHTTPMethod(put).getStatusLine();
        return statusLine.getStatusCode() + " " + statusLine.getReasonPhrase();
    }


	private static String extractBody(HttpEntity entity) {
		try {
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			entity.writeTo(ostream);
			Log.v(TAG, ostream.toString());
			return ostream.toString();
		} catch (IllegalStateException e) {
			Log.e(TAG, "illegal state: " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		} catch (IOException e) {
			Log.e(TAG, "io: " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
	}

	
	private static void insertXML(String body, HttpEntityEnclosingRequestBase method) {
	    
        insert(body, "text/xml", "text/xml", method);
	}
	
	
    private static void insertJson(String body, HttpEntityEnclosingRequestBase method) {
        
        insert(body, "application/json", "application/json", method);
    }
    
    
	private static void insert(String pBody, String pContentType, String pAccept, HttpEntityEnclosingRequestBase pMethod) {
        StringEntity entity;
        try {
            entity = new StringEntity(pBody);
            pMethod.setEntity(entity);
            pMethod.addHeader("Content-Type", pContentType);
            pMethod.addHeader("Accept", pAccept);         
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "unsupported encoding: " + e.getMessage());
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
                Log.v(TAG, response.getStatusLine().getStatusCode() + " redirect to: " + uri);
                return uri;
            }
        });

		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_0,
				500, "Client Error");
		try {
			response = httpclient.execute(method);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "protocol exception: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "io exception: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(TAG, "unknown exception:" + e.getMessage());
			e.printStackTrace();
		}
		return response;
	}
	
	
	private static String toJsonUrl(String pUrl) {
	    
        if (pUrl.endsWith(".xml")) {
            // an xml uri means that someone is using this method in a wrong way
            // --> fail fast
            throw new IllegalArgumentException(
                    "HttpHelper was passed a URI which explicitly "+
                    "asked for a different format: '"+pUrl+"'!");
        }
        
        // remove trailing slashes
        if (pUrl.endsWith("/")) {
            
            pUrl = pUrl.substring(0, pUrl.length()-1);
        }
        
        // gracefully accept format-agnostic URIs
        if (!pUrl.endsWith(".json")) {
            pUrl = pUrl + ".json";
        }
        
        return pUrl;
	}

}
