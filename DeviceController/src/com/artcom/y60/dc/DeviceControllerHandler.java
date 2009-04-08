package com.artcom.y60.dc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.DefaultHandler;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.artcom.y60.Actions;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.PreferencesActivity;


public class DeviceControllerHandler extends DefaultHandler
{
	private static final String LOG_TAG = "DeviceControllerHandler";
    
	// name of the preferences file. android stores this under
	// /data/data/com.artcom.y60.dc/shared_prefs/device_controller.xml
    private static final String prefFile = "device_controller";
    
    private Service mService;
    
    public DeviceControllerHandler( Service p_Service ) {
    	mService = p_Service;
    }
    
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {

		
	    SharedPreferences prefs = mService.getSharedPreferences( prefFile, Context.MODE_WORLD_READABLE );
	    String gom_location = prefs.getString( PreferencesActivity.KEY_GOM_LOCATION, "" );
	    String device_id = prefs.getString( PreferencesActivity.KEY_DEVICE_ID, "" );
	    String device_path = prefs.getString( PreferencesActivity.KEY_DEVICES_PATH, "" );

		Logger.v(LOG_TAG , "Target: ", target);
		String method = request.getMethod();
		Logger.v(LOG_TAG, "Method: ", method);

		String path = request.getPathInfo();
		String location = "";

		String self = device_path + "/" + device_id;

		if (path.startsWith( "/proc" )) {
			// Requests directed at the DC's own resources.
		    Logger.v(LOG_TAG, "Handling /proc request");
			ProcHandler procHandler = new ProcHandler();
			procHandler.handle(target, request, response, dispatch);
			return;
		}
		
		// All other GET requests are redirected to the GOM
		if (method.equals("GET") || method.equals("HEAD")) {
			
			if (path == null) {
				path = "";
			}
        
			if (path.startsWith("/self")) {
				location = gom_location + self + path.replaceFirst("/self", ""); 
			} else {
				location = gom_location + path;
			}	

			response.setStatus(HttpServletResponse.SC_SEE_OTHER);
			response.addHeader("Location", location);
            response.setContentType("text/plain");
            response.setContentLength(0);
		} else if (method.equals("POST")) {
			String queryString = request.getQueryString();
			String requestString = request.getRequestURL().toString();
			Uri uri = Uri.parse(requestString + "?" + queryString);
			String query = uri.getQuery();
			String[] keysnvalues = query.split("[=&]");
			Map<String,String> kvMap = new HashMap<String,String>();
			Logger.v(LOG_TAG, keysnvalues.length, " elements");
			for (int i = 0; i*2+1 < keysnvalues.length; i++) {
				kvMap.put(keysnvalues[i*2], keysnvalues[i*2+1]);
				Logger.v(LOG_TAG, "\t", keysnvalues[i*2], " = ", keysnvalues[i*2+1]);
			}
			if (keysnvalues.length % 2 != 0 && keysnvalues.length > 0) {
				Logger.v(LOG_TAG, "\tLonely key: ", keysnvalues[keysnvalues.length-1]);
			}
			
			Intent intent = new Intent(Actions.SEARCH);
			intent.putExtra(IntentExtraKeys.KEY_SEARCH_SENDER, kvMap.get("sender"));
			intent.putExtra(IntentExtraKeys.KEY_SEARCH_RECEIVER, kvMap.get("receiver"));
			intent.putExtra(IntentExtraKeys.KEY_SEARCH_ARGUMENTS, kvMap.get("arguments"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mService.startActivity(intent);
		} else {
			respond_not_supported(request, response);
		}
		
		Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
		base_request.setHandled(true);
	}
	
    private void respond_not_supported( HttpServletRequest request,
            HttpServletResponse response )
        throws ServletException, IOException
    {
        response.setContentType("text/plain");
        response.setStatus( HttpServletResponse.SC_NOT_IMPLEMENTED );
        response.setContentLength(0);
    }
	
}