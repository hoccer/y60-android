package com.artcom.y60.infrastructure.dc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.ContextHandler;

import com.artcom.y60.infrastructure.dc.StatusCollector.ScreenState;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.util.Log;

// TODO make this class do something useful - supply battery status, device status or something

public class ProcHandler extends ContextHandler {
	
	private static final String REQ_SCREEN = "/proc/screen";
	private static final String REQ_RECENT = "/proc/recent";
	
	private static final String LOG_TAG = "ProcHandler";
	
	private static Activity m_Parent;
	
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		
		Log.v( LOG_TAG, "Handling incoming request for target " + target );
		
		if (target.equals( REQ_SCREEN )) {
			handleScreenRequest( target, request, response, dispatch );
		} else if (target.equals( REQ_RECENT )) {
			handleRecentRequest( target, request, response, dispatch );
		} else {
			handleUnknownRequest( target, request, response, dispatch );
		}
		
		Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
		base_request.setHandled(true);
	}
	
	private void handleUnknownRequest(String target,
			HttpServletRequest request, HttpServletResponse response,
			int dispatch) {

        response.setContentType("text/plain");
        response.setStatus( HttpServletResponse.SC_NOT_IMPLEMENTED );
        response.setContentLength(0);
	}

	private void handleRecentRequest(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException {
		
		response.setStatus( HttpServletResponse.SC_OK );
        response.setContentType("text/plain");
        
//        RecentTaskInfo taskInfo = new ActivityManager.RecentTaskInfo();
//        List<ActivityManager.RecentTaskInfo> recentTasks =
//        	ActivityManager.getRecentTasks( 5, ActivityManager.RECENT_WITH_EXCLUDED );
//        ActivityManager mgr = (ActivityManager)getSystemService( Context.ACTIVITY_SERVICE );
        ServletOutputStream out = response.getOutputStream();

        
        out.println( "Placeholder" );
        out.flush();
	}

	private void handleScreenRequest( String target,
									  HttpServletRequest request,
									  HttpServletResponse response,
									  int dispatch)
		throws IOException, ServletException{
		
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
	}

}
