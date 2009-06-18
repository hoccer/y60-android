package com.artcom.y60.dc;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;

import com.artcom.y60.HttpHelper;

public class DeviceControllerUnitTests extends TestCase {

    public void testStartingAndStoppingJetty() throws Exception {

        Server jetty = new Server();
        assertFalse(jetty.isStarted());
        assertFalse(jetty.isRunning());
        assertTrue(jetty.isStopped());

        jetty.start();
        assertTrue(jetty.isStarted());
        assertTrue(jetty.isRunning());
        assertFalse(jetty.isStopped());

        jetty.stop();
        jetty.join();
        assertFalse(jetty.isStarted());
        assertFalse(jetty.isRunning());
        assertTrue(jetty.isStopped());
    }

    public void testSendingRequestToJetty() throws Exception {
        Server jetty = new Server();

        SocketConnector connector = new SocketConnector();
        connector.setPort(8082);
        connector.setHost("0.0.0.0"); // listen on all interfaces
        Connector[] connectors = { connector };
        jetty.setConnectors(connectors);

        jetty.start();
        assertTrue(jetty.isStarted());
        assertTrue(jetty.isRunning());
        assertFalse(jetty.isStopped());

        assertEquals("jetty should return a status code", 404, HttpHelper
                .getStatusCode("http://localhost:8082/"));

        jetty.stop();
        jetty.join();
        assertFalse(jetty.isStarted());
        assertFalse(jetty.isRunning());
        assertTrue(jetty.isStopped());
    }

    public void testJettyResponse() throws Exception {
        Server jetty = new Server();

        SocketConnector connector = new SocketConnector();
        connector.setPort(8082);
        connector.setHost("0.0.0.0"); // listen on all interfaces
        Connector[] connectors = { connector };
        jetty.setConnectors(connectors);

        Handler handler = new AbstractHandler() {
            @Override
            public void handle(String target, HttpServletRequest request,
                    HttpServletResponse response, int dispatch) {

                response.setStatus(Response.SC_OK);
                response.setContentType("text/plain");

                ServletOutputStream out;
                try {
                    out = response.getOutputStream();
                    out.print("hello world");
                    out.flush();
                } catch (IOException e) {
                    throw new AssertionError(e);
                }

                ((Request) request).setHandled(true);

            }
        };
        jetty.setHandler(handler);

        jetty.start();
        assertTrue(jetty.isStarted());
        assertTrue(jetty.isRunning());
        assertFalse(jetty.isStopped());

        assertEquals("jetty should return succssful status code", 200, HttpHelper
                .getStatusCode("http://localhost:8082/"));
        assertEquals("jetty should respond", "hello world", HttpHelper
                .get("http://localhost:8082/hello"));

        jetty.stop();
        jetty.join();
        assertFalse(jetty.isStarted());
        assertFalse(jetty.isRunning());
        assertTrue(jetty.isStopped());
        assertFalse(jetty.isFailed());
    }
}
