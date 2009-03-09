package com.artcom.y60.infrastructure.dc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.DefaultHandler;

import com.artcom.y60.infrastructure.PreferencesActivity;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;


public class DeviceControllerHandler extends DefaultHandler
{
	private static final String LOG_TAG = "DeviceControllerHandler";
    
	// name of the preferences file. android stores this under
	// /data/data/com.artcom.y60.infrastructure.dc/shared_prefs/device_controller.xml
    private static final String prefFile = "device_controller";
    
    private Service m_Service;
    
    public DeviceControllerHandler( Service p_Service ) {
    	m_Service = p_Service;
    }
    
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {

		
	    SharedPreferences prefs = m_Service.getSharedPreferences( prefFile, Context.MODE_WORLD_READABLE );
	    String gom_location = prefs.getString( PreferencesActivity.KEY_GOM_LOCATION, "" );
	    String device_id = prefs.getString( PreferencesActivity.KEY_DEVICE_ID, "" );
	    String device_path = prefs.getString( PreferencesActivity.KEY_DEVICES_PATH, "" );

		Log.v(LOG_TAG , "Target: " + target );
		String method = request.getMethod();
		Log.v(LOG_TAG, "Method: " + method );

		String path = request.getPathInfo();
		String location = "";

		String self = device_path + "/" + device_id;

		if (path.startsWith( "/proc" )) {
			// Requests directed at the DC's own resources.
			Log.v( LOG_TAG, "Handling /proc request" );
			ProcHandler procHandler = new ProcHandler();
			procHandler.handle( target, request, response, dispatch );
			return;
		}
		
		// All other GET requests are redirected to the GOM
		if (method.equals( "GET" )) {
			
			if (path == null) {
				path = "";
			}
        
			if (path.equals( "/self" )) {
				location = gom_location + self; 
			} else {
				location = gom_location + path;
			}	

			response.setStatus( HttpServletResponse.SC_SEE_OTHER );
			response.addHeader( "Location", location );
            response.setContentType("text/plain");
            response.setContentLength(0);
		} else {
			respond_not_supported( request, response );
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