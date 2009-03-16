package com.artcom.y60.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

import android.net.Uri;
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

	public static String get(Uri uri) {

		Log.v(TAG, "get('"+uri+"')");
		HttpGet get = new HttpGet(uri.toString());
		HttpEntity entity = executeHTTPMethod(get).getEntity();
		String result = extractBody(entity);
		//Log.v(TAG, "got: " + result);
		return result;

	}
	
	public static String get(String uri) {
		return get(Uri.parse(uri));
	}
	
	public static void fetchUriToFile( String uriString, String filename ) {
		HttpGet get = new HttpGet(uriString);
		HttpEntity entity = executeHTTPMethod(get).getEntity();
		
		// Generate a random filename to store the data under
		
		Log.v( TAG, "Storing content under filename " +  filename );

		try {
			FileOutputStream fstream = new FileOutputStream( filename );
			entity.writeTo(fstream);
		} catch (IllegalStateException e) {
			Log.e(TAG, "illegal state: " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (IOException e) {
			Log.e(TAG, "io: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	
	public static JSONObject getJson(String pUrl) throws JSONException {
	    
	    String url    = toJsonUrl(pUrl);
	    String result = get(url);
	    return new JSONObject(result);
	}
	
	public static StatusLine getStatusLine(String url) {
	    
		HttpGet get = new HttpGet(url);
		return executeHTTPMethod(get).getStatusLine();
	}
	

    public static StatusLine putUrlEncoded(String pUrl, Map<String, String> pData) {
        
        StringBuffer tmp = new StringBuffer();
        Set<String> keys = pData.keySet();
        int idx = 0;
        for (String key: keys) {
            
            tmp.append(URLEncoder.encode(key));
            tmp.append("=");
            tmp.append(URLEncoder.encode(pData.get(key)));
            
            idx += 1;
            
            if (idx < keys.size()) {
                
                tmp.append("&");
            }
        }
        
        HttpPut put  = new HttpPut(pUrl);
        String  body = tmp.toString();
        
        Log.v(TAG, "PUT "+pUrl+" with body "+body);
        
        insertUrlEncoded(body, put);
        return executeHTTPMethod(put).getStatusLine();
    }
    
    
    public static StatusLine putUrlEncoded(String pUrl, String pData) {
        
        HttpPut put = new HttpPut(pUrl);
        insertUrlEncoded(URLEncoder.encode(pData), put);
        return executeHTTPMethod(put).getStatusLine();
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
	
	
    private static void insertUrlEncoded(String pEncodedBody, HttpEntityEnclosingRequestBase method) {
        
        insert(pEncodedBody, "application/x-www-form-urlencoded", "application/json", method);
    }
    
    
	private static void insert(String pBody, String pContentType, String pAccept, HttpEntityEnclosingRequestBase pMethod) {
	    
	    Log.v(TAG, "inserting for content type "+pContentType+", body is "+pBody);
	    
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

    public static StatusLine postUrlEncoded( String pUrl, Map<String, String> pData)
    {    
        StringBuffer tmp = new StringBuffer();
        Set<String> keys = pData.keySet();
        int idx = 0;
        for (String key: keys) {
            
            tmp.append(URLEncoder.encode(key));
            tmp.append("=");
            tmp.append(URLEncoder.encode(pData.get(key)));
            
            idx += 1;
            
            if (idx < keys.size()) {
                
                tmp.append("&");
            }
        }
        
        HttpPost post  = new HttpPost(pUrl);
        String  body = tmp.toString();
        
        Log.v(TAG, "POST "+pUrl+" with body "+body);
        
        insertUrlEncoded(body, post);
        return executeHTTPMethod(post).getStatusLine();
    }


}
