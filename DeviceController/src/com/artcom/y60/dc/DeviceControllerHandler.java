package com.artcom.y60.dc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.util.MultiMap;
import org.mortbay.util.UrlEncoded;

import com.artcom.y60.Constants;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.Y60Action;

import android.app.Service;
import android.content.Intent;

public class DeviceControllerHandler extends DefaultHandler {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG                 = "DeviceControllerHandler";

    /** path prefix for proc requests */
    public static final String  PROC_PATH_PREFIX        = "/proc";

    /** target for RCA HTTP requests */
    public static final String  RCA_TARGET              = "/commands";

    public static final String  LOG_COMMAND             = "/logcat";
    public static final String  CUSTOM_COMMAND          = "/exec";

    // Instance Variables ------------------------------------------------

    private final DeviceControllerService mService;

    // Constructors ------------------------------------------------------

    public DeviceControllerHandler(DeviceControllerService pService) {
        mService = pService;
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void handle(String pTarget, HttpServletRequest pRequest, HttpServletResponse pResponse,
            int pDispatch) {

        try {
            String method = pRequest.getMethod();
            //String path = pRequest.getPathInfo();

            Logger.v(LOG_TAG, "Incoming HTTP request________");
            
            if ("POST".equals(method) && RCA_TARGET.equals(pTarget)) {

                handleCommand(pRequest);

            } else if (Constants.Network.GNP_TARGET.equals(pTarget)) {

                handleGomNotification(pRequest);

            } else if ("GET".equals(method) && LOG_COMMAND.equals(pTarget)) {
                int     visible_characters = 0;
                if (pRequest.getQueryString() != null) {
                    String[]    parameterList = URLDecoder.decode(pRequest.getQueryString()).split("&");
                    String      sizeParamterIdentifier = "size=";
                    for(int i=0;i<parameterList.length;++i){
                        if(parameterList[i].startsWith(sizeParamterIdentifier)){
                           String numberString = parameterList[i].substring(sizeParamterIdentifier.length()); 
                           try {
                               visible_characters = Integer.parseInt(numberString);
                               break;
                           } catch(NumberFormatException e){
                           }
                        }
                    }
                }

                String commandBufferText = null;
                if( visible_characters != 0 ){
                    commandBufferText = mService.getLogcatCommandBuffer().getCommandBufferFromFile(visible_characters);
                } else {
                    commandBufferText = mService.getLogcatCommandBuffer().getCommandBufferFromFile();
                }
                if (commandBufferText != null ){
                    respondOKWithMessage(pResponse, commandBufferText);
                } else {
                    respondServerErrorWithMessage(pResponse,mService.getLogcatCommandBuffer().getExceptionMessage());
                }

            } else if ("GET".equals(method) && pTarget.startsWith(CUSTOM_COMMAND)) {
                if (pRequest.getQueryString() != null) {
                    String customCommand = URLDecoder.decode(pRequest.getQueryString());
                    Logger.v(LOG_TAG,"CUSTOM COMMAND: ", customCommand);

                    CommandBuffer commandBuffer = new CommandBuffer(); 
                    commandBuffer.executeReturningCommand(customCommand);
                    String commandBufferText = commandBuffer.getCommandBufferFromRam();
                    if (commandBufferText != null){
                        respondOKWithMessage(pResponse, commandBufferText);
                    } else {
                        respondServerErrorWithMessage(pResponse,commandBuffer.getExceptionMessage());
                    }
                } else {
                    respondOKWithMessage(pResponse, "no command was specified, usage: " + CUSTOM_COMMAND + "?shellcommand");
                }

            } else if ("HEAD".equals(method) || "GET".equals(method)) {
                Logger.v(LOG_TAG, "Not found");
                respondNotFound(pResponse);

            } else {
                Logger.v(LOG_TAG, "Not supported");
                respondNotImplemented(pResponse);
            }

        } catch (Exception ex) {

            ErrorHandling.signalUnspecifiedError(LOG_TAG, ex, mService);

        } finally {

            if (pRequest instanceof Request) {

                ((Request) pRequest).setHandled(true);

            } else {

                HttpConnection connection = HttpConnection.getCurrentConnection();

                // HACK: if this is called in a test, the request will be mocked
                // and
                // thus the connection will be null
                if (connection != null) {

                    connection.getRequest().setHandled(true);
                }
            }
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

        MultiMap argumentsMap = new MultiMap();
        String argumentsUrl = (String) parameters.get("arguments");
        UrlEncoded.decodeUtf8To(argumentsUrl.getBytes(), 0, argumentsUrl.length(), argumentsMap);
        String argumentsJson = new JSONObject(argumentsMap).toString();

        Logger.v(LOG_TAG, "target = " + (String) parameters.get("target") + ", sender = "
                + (String) parameters.get("sender") + ", receiver = "
                + (String) parameters.get("receiver") + ", arguments = " + argumentsJson);

        Intent broadcastIntent = null;

        String targetParam = (String) parameters.get("target");
        Logger.v(LOG_TAG, "target: ", targetParam);

        if ("search".equals(targetParam)) {
            broadcastIntent = new Intent(Y60Action.SEARCH_BC);
        } else if ("voice_control".equals(targetParam)) {
            broadcastIntent = new Intent(Y60Action.VOICE_CONTROL_BC);
        } else if ("movie_player".equals(targetParam) || "music_player".equals(targetParam)
                || "picture_viewer".equals(targetParam)) {
            broadcastIntent = new Intent(Y60Action.MOVIE_CONTROL_BC);
        } else if ("video_conf".equals(targetParam)) {
            final String ACCEPTANCE_DIALOG = "tgallery.intent.AcceptanceDialog";
            Intent intent = new Intent(ACCEPTANCE_DIALOG);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(IntentExtraKeys.RCI_SENDER, (String) parameters.get("sender"));
            mService.startActivity(intent);
            return;
        } else if ("user_feedback".equals(targetParam)) {
            Logger.v(LOG_TAG, "handle user_feedback rc event");
            broadcastIntent = new Intent(Y60Action.USER_FEEDBACK_BC);
        } else {
            Logger.e("illegal RCA target: " + targetParam + "from: " + parameters.get("sender"));
            return;
        }

        broadcastIntent.putExtra(IntentExtraKeys.RCI_TARGET, (String) parameters.get("target"));
        broadcastIntent.putExtra(IntentExtraKeys.RCI_SENDER, (String) parameters.get("sender"));
        broadcastIntent.putExtra(IntentExtraKeys.RCI_RECEIVER, (String) parameters.get("receiver"));
        broadcastIntent.putExtra(IntentExtraKeys.RCI_ARGUMENTS, argumentsJson);

        mService.sendBroadcast(broadcastIntent);
    }

    private void handleGomNotification(HttpServletRequest pRequest) throws IOException,
            HandlerException {

        String content = extractContent(pRequest);

        try {
            JSONObject notification = new JSONObject(content);

            Logger.v(LOG_TAG, "JSON path of gom notification: ", notification.get("uri"));

            Intent gnpIntent = new Intent(Y60Action.GOM_NOTIFICATION_BC);

            // wrong concept - uri is actually a path! see RFC 2396 for details
            gnpIntent.putExtra(IntentExtraKeys.NOTIFICATION_PATH, notification.getString("uri"));

            String operation = null;
            if (notification.has("create")) {

                operation = "create";

            } else if (notification.has("update")) {

                operation = "update";

            } else if (notification.has("delete")) {

                operation = "delete";
            }

            if (operation == null) {

                throw new HandlerException("GOM notification malformed:\n" + content);
            }

            gnpIntent.putExtra(IntentExtraKeys.NOTIFICATION_OPERATION, operation);

            String data = notification.getJSONObject(operation).toString();
            gnpIntent.putExtra(IntentExtraKeys.NOTIFICATION_DATA_STRING, data);

            Logger.v(LOG_TAG, "notification operation: ", operation.toUpperCase(), " and data: ",
                    data);
            mService.sendBroadcast(gnpIntent);

        } catch (JSONException jsx) {

            Logger.e(LOG_TAG, "ignoring notification - failed to parse json: \n'", content, "'\n",
                    jsx);
        }
    }

    // Private Instance Methods ------------------------------------------

    /**
     * @param pRequest
     * @return
     * @throws IOException
     */
    private String extractContent(HttpServletRequest pRequest) throws IOException {

        BufferedReader reader = pRequest.getReader();
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {

            builder.append(line + "\n");
        }
        return builder.toString();
    }

    private void respondNotImplemented(HttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        response.setContentLength(0);
    }

    private void respondNotFound(HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentLength(0);
    }

    private void respondOKWithMessage(HttpServletResponse response, String responseText) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.print(responseText); 
        out.flush();
    }

    private void respondServerErrorWithMessage(HttpServletResponse response, String responseText) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        PrintWriter out = response.getWriter();
        out.print(responseText); 
        out.flush();
    }
 
}
