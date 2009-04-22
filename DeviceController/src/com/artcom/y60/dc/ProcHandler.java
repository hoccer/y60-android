package com.artcom.y60.dc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.ContextHandler;

import com.artcom.y60.Logger;

// TODO The functionality in this class is currently buggy. Fix and write tests.

public class ProcHandler extends ContextHandler {
	
	private static final String REQ_SCREEN = "/proc/screen";
	private static final String REQ_RECENT = "/proc/recent";
	
	private static final String LOG_TAG = "ProcHandler";
	
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		
		Logger.v( LOG_TAG, "Handling incoming request for target ", target );
		
		if (target.equals( REQ_RECENT )) {
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

}
