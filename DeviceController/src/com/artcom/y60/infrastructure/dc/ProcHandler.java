package com.artcom.y60.infrastructure.dc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.ContextHandler;

import com.artcom.y60.infrastructure.dc.StatusCollector.ScreenState;

import android.util.Log;

// TODO make this class do something useful - supply battery status, device status or something

public class ProcHandler extends ContextHandler {
	
	private static final String LOG_TAG = "ProcHandler";
	
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		
		Log.v( LOG_TAG, "Handling incoming request" );
		StatusCollector status = StatusCollector.getInstance();
		
		response.setStatus( HttpServletResponse.SC_OK );
        response.setContentType("text/plain");
        
        ScreenState screenState = status.getScreenState();
        ServletOutputStream out = response.getOutputStream();
        switch (screenState) {
        case UNKNOWN:
        	out.println( "Screen state is unknown (Possible reason: No state change events received yet)" );
        	break;
        case ON:
        	out.println( "Screen is on" );
        	break;
        case OFF:
        	out.println( "Screen is off" );
        	break;
        }
        
        out.flush();
		
		Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
		base_request.setHandled(true);
	}

}
