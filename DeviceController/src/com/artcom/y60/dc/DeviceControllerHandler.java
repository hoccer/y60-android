package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.test.FlakyTest;

import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.PreferencesActivity;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Y60Action;

public class DeviceControllerHandler extends DefaultHandler {
    private static final String LOG_TAG = "DeviceControllerHandler";

    // name of the preferences file. android stores this under
    // /data/data/com.artcom.y60.dc/shared_prefs/device_controller.xml
    private static final String prefFile = "device_controller";

    private Service mService;

    public DeviceControllerHandler(Service p_Service) {
        mService = p_Service;
    }

    public void handle(String target, HttpServletRequest request, HttpServletResponse response,
            int dispatch) throws IOException, ServletException {

        SharedPreferences prefs = mService.getSharedPreferences(prefFile,
                Context.MODE_WORLD_READABLE);
        String gom_location = prefs.getString(PreferencesActivity.KEY_GOM_LOCATION, "");
        String device_id = prefs.getString(PreferencesActivity.KEY_DEVICE_ID, "");
        String device_path = prefs.getString(PreferencesActivity.KEY_DEVICES_PATH, "");

        Logger.v(LOG_TAG, "Target: ", target);
        String method = request.getMethod();
        Logger.v(LOG_TAG, "Method: ", method);

        String path = request.getPathInfo();
        String location = "";

        String self = device_path + "/" + device_id;

        if (path.startsWith("/proc")) {
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

            String parameters = "";
            if (request.getContentLength() > 0) { // arguments are supplied in
                // request body
                BufferedReader reader = request.getReader();
                parameters = reader.readLine();
            } else { // arguments are supplied in request string
                parameters = request.getQueryString();
            }

            MultiMap kvMap = new MultiMap();
            UrlEncoded.decodeUtf8To(parameters.getBytes(), 0, parameters.length(), kvMap);

            // String[] keysnvalues = parameters.split("[=&]");
            // Map<String,String> kvMap = new HashMap<String,String>();
            // Logger.v(LOG_TAG, keysnvalues.length, " elements");
            // for (int i = 0; i*2+1 < keysnvalues.length; i++) {
            // UrlEncoded encodedKey = new UrlEncoded(keysnvalues[i*2]).de;
            // kvMap.put(keysnvalues[i*2], keysnvalues[i*2+1]);
            // Logger.v(LOG_TAG, "\t", keysnvalues[i*2], " = ",
            // keysnvalues[i*2+1]);
            // }
            // if (keysnvalues.length % 2 != 0 && keysnvalues.length > 0) {
            // Logger.v(LOG_TAG, "\tLonely key: ",
            // keysnvalues[keysnvalues.length-1]);
            // }

            Logger.v(LOG_TAG, "target = " + (String) kvMap.get("target") + ", sender = "
                    + (String) kvMap.get("sender") + ", receiver = "
                    + (String) kvMap.get("receiver") + ", arguments = "
                    + (String) kvMap.get("arguments"));

            Intent intent;
            Intent broadcastIntent;
            
            if ((kvMap.get("target")).equals("search")) {
                intent = new Intent(Y60Action.SEARCH);
                broadcastIntent = new Intent(Y60Action.SEARCH_BC);
                
            } else if ((kvMap.get("target")).equals("voice_control")) {
                intent = new Intent(Y60Action.VOICE_CONTROL);
                broadcastIntent = new Intent(Y60Action.VOICE_CONTROL_BC);
                
            } else {
                ErrorHandling.signalComponentNotFoundError(LOG_TAG, new Exception(
                        "illegal RCA target: " + (kvMap.get("target")) + "from: "
                                + (kvMap.get("sender"))), mService);
                return;
            }

            broadcastIntent.putExtra(IntentExtraKeys.KEY_SEARCH_TARGET, (String) kvMap.get("target"));
            broadcastIntent.putExtra(IntentExtraKeys.KEY_SEARCH_SENDER, (String) kvMap.get("sender"));
            broadcastIntent.putExtra(IntentExtraKeys.KEY_SEARCH_RECEIVER, (String) kvMap.get("receiver"));
            broadcastIntent.putExtra(IntentExtraKeys.KEY_SEARCH_ARGUMENTS, (String) kvMap.get("arguments"));

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mService.startActivity(intent);
            
            mService.sendBroadcast(broadcastIntent);

        } else {
            respond_not_supported(request, response);
        }

        Request base_request = (request instanceof Request) ? (Request) request : HttpConnection
                .getCurrentConnection().getRequest();
        base_request.setHandled(true);
    }

    private void respond_not_supported(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        response.setContentLength(0);
    }

}