package com.artcom.y60.synergy;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.util.ByteArrayISO8859Writer;

import com.artcom.y60.Logger;

public class SynergyServerHandler extends DefaultHandler {
    private static final String LOG_TAG          = "SynergyServerHandler";

    public SynergyServerHandler() {
    }

    @Override
    public void handle(String pTarget, HttpServletRequest pRequest, 
            HttpServletResponse pResponse, int pDispatch) {
        try {
            String method = pRequest.getMethod();
            Logger.v(LOG_TAG, "request: ", pRequest);
            Logger.v(LOG_TAG, "Incoming HTTP request________",method);
            if ("HEAD".equals(method) || "GET".equals(method)) {
                responseWithHtml(pResponse);
            } else {
                Logger.v(LOG_TAG, "Not supported");
                respondNotImplemented(pResponse);
            }

        } catch (Exception ex) {
            //ErrorHandling.signalUnspecifiedError(LOG_TAG, ex, this);

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

    private void responseWithHtml(HttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType(MimeTypes.TEXT_HTML);
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);

        ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);
        writer.write("<HTML>\n<HEAD>\n<TITLE>Error 404 - Not Found");
        writer.write("</TITLE>\n<BODY>\n<H2>Error 404 - Not Found.</H2>\n");
        writer.write("\n</BODY>\n</HTML>\n");
        writer.flush();
        response.setContentLength(writer.size());
        OutputStream out=response.getOutputStream();
        writer.writeTo(out);
        out.close();
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

}






