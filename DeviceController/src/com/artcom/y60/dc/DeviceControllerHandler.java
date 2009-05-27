package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;

import android.app.Service;
import android.content.Intent;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

public class DeviceControllerHandler extends DefaultHandler {
    
    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "DeviceControllerHandler";

    /** path prefix for proc requests */
    public static final String PROC_PATH_PREFIX = "/proc";
    
    /** target for RCA HTTP requests */
    public static final String RCA_TARGET = "/commands";
    
    /** target for GNP HTTP requests */
    public static final String GNP_TARGET = "/notifications";
    
    // Instance Variables ------------------------------------------------
    
    private Service mService;
    
    // Constructors ------------------------------------------------------

    public DeviceControllerHandler(Service pService) {
        mService = pService;
    }
    
    // Public Instance Methods -------------------------------------------

    public void handle(String pTarget,
                       HttpServletRequest pRequest,
                       HttpServletResponse pResponse,
                       int pDispatch) {
        
        try {

            String method = pRequest.getMethod();
            String path   = pRequest.getPathInfo();
            
            Logger.v(LOG_TAG, "Incoming HTTP request________");
            Logger.v(LOG_TAG, "Method...", method);
            Logger.v(LOG_TAG, "Path.....", path);
            Logger.v(LOG_TAG, "Target...", pTarget);
    
    
            if (path.startsWith(PROC_PATH_PREFIX)) {
                
                // Requests directed at the DC's own resources.
                Logger.v(LOG_TAG, "Handling /proc request");
                ProcHandler procHandler = new ProcHandler();
                procHandler.handle(pTarget, pRequest, pResponse, pDispatch);
                
            } else if ("POST".equals(method) && RCA_TARGET.equals(pTarget)) {
    
                handleCommand(pRequest);
    
            } else if (GNP_TARGET.equals(pTarget)) {
                
                handleGomNotification(pRequest);
                
            } else {
                
                respondNotSupported(pResponse);
            }
    
        } catch (Exception ex) {
            
            ErrorHandling.signalUnspecifiedError(LOG_TAG, ex, mService);
            
        } finally {
        
            Request baseRequest = (pRequest instanceof Request) ? (Request) pRequest : HttpConnection
                            .getCurrentConnection().getRequest();
            baseRequest.setHandled(true);
        }
    }
    
    // Private Instance Methods ------------------------------------------
    
    private void handleCommand(HttpServletRequest pRequest) throws IOException, HandlerException {
        
        Logger.d(LOG_TAG, "handling RCA command");
        
        String paramsStr = "";
        if (pRequest.getContentLength() > 0) { // arguments are supplied in
            // request body
            BufferedReader reader = pRequest.getReader();
            paramsStr = reader.readLine();
        } else { // arguments are supplied in request string
            paramsStr = pRequest.getQueryString();
        }

        MultiMap parameters = new MultiMap();
        UrlEncoded.decodeUtf8To(paramsStr.getBytes(), 0, paramsStr.length(), parameters);

        Logger.v(LOG_TAG, "target = " + (String) parameters.get("target") + ", sender = "
                + (String) parameters.get("sender") + ", receiver = "
                + (String) parameters.get("receiver") + ", arguments = "
                + (String) parameters.get("arguments"));

        Intent intent;
        Intent broadcastIntent;
        
        Object targetParam = parameters.get("target"); 
        
        if ("search".equals(targetParam)) {
            intent = new Intent(Y60Action.SEARCH);
            broadcastIntent = new Intent(Y60Action.SEARCH_BC);
            
        } else if ("voice_control".equals(targetParam)) {
            intent = new Intent(Y60Action.VOICE_CONTROL);
            broadcastIntent = new Intent(Y60Action.VOICE_CONTROL_BC);
            
        } else {
            throw new HandlerException("illegal RCA target: " + targetParam + "from: "
                            + parameters.get("sender"));
        }

        broadcastIntent.putExtra(IntentExtraKeys.KEY_SEARCH_TARGET, (String) parameters.get("target"));
        broadcastIntent.putExtra(IntentExtraKeys.KEY_SEARCH_SENDER, (String) parameters.get("sender"));
        broadcastIntent.putExtra(IntentExtraKeys.KEY_SEARCH_RECEIVER, (String) parameters.get("receiver"));
        broadcastIntent.putExtra(IntentExtraKeys.KEY_SEARCH_ARGUMENTS, (String) parameters.get("arguments"));

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mService.startActivity(intent);
        
        mService.sendBroadcast(broadcastIntent);
    }
    
    private void handleGomNotification(HttpServletRequest pRequest)
                            throws IOException, JSONException, HandlerException {
        
        Logger.d(LOG_TAG, "handling GOM notification");
        
        String     content      = extractContent(pRequest);
        JSONObject notification = new JSONObject(content);
        Intent     gnpIntent    = new Intent(Y60Action.GOM_NOTIFICATION_BC);
        
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_URI, notification.getString("uri"));
        
        String operation = null;
        if (notification.has("create")) {
            
            operation = "create";
            
        } else if (notification.has("update")) {
            
            operation = "update";
            
        } else if (notification.has("delete")) {
            
            operation = "delete";
        }
        
        if (operation == null) {
            
            throw new HandlerException("GOM notification malformed:\n"+content);
        }

        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION, operation);
        
        String data = notification.getJSONObject(operation).toString();
        gnpIntent.putExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING, data);
        
        mService.sendBroadcast(gnpIntent);
    }

    /**
     * @param pRequest
     * @return
     * @throws IOException
     */
    private String extractContent(HttpServletRequest pRequest) throws IOException {
        
        BufferedReader reader  = pRequest.getReader();
        StringBuilder  builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            
            builder.append(line+"\n");
        }
        return builder.toString();
    }

    private void respondNotSupported(HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        response.setContentLength(0);
    }

}