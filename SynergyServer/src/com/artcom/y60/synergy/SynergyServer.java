package com.artcom.y60.synergy;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.thread.QueuedThreadPool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;


public class SynergyServer extends Activity {

    private static final String     LOG_TAG          = "SynergyServer";

    private Server                  mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.v(LOG_TAG, ">>> onCreate() ", this);
        setContentView(R.layout.main);
        try {
            if (mServer == null) {
                mServer = startServer(4043);
            }
        } catch (Exception ex) {
            ErrorHandling.signalUnspecifiedError(LOG_TAG, ex, this);
        }
        Logger.v(LOG_TAG, "<<< onCreate() ", this);
    }

    @Override
    protected void onStart() {
        Logger.v(LOG_TAG, ">>> onStart() ", this);
        super.onStart();
        Logger.v(LOG_TAG, "<<< onStart() ", this);
    }

    @Override
    protected void onRestart() {
        Logger.v(LOG_TAG, ">>> onRestart() ", this);
        super.onRestart();
        Logger.v(LOG_TAG, "<<< onRestart() ", this);
    }

    @Override
    protected void onResume() {
        Logger.v(LOG_TAG, ">>> onResume() ", this);
        super.onResume();
        Logger.v(LOG_TAG, "<<< onResume() ", this);
    }

    @Override
    protected void onPostResume() {
        Logger.v(LOG_TAG, ">>> onPostResume() ", this);
        super.onPostResume();
        Logger.v(LOG_TAG, "<<< onPostResume() ", this);
    }

    @Override
    public void onPause() {
        Logger.v(LOG_TAG, ">>> onPause() ", this);
        super.onPause();
        Logger.v(LOG_TAG, "<<< onPause() ", this);
    }

    @Override
    protected void onStop() {
        Logger.v(LOG_TAG, ">>> onStop() ", this);
        super.onStop();
        Logger.v(LOG_TAG, "<<< onStop() ", this);
    }

    @Override
    protected void onDestroy() {
        Logger.v(LOG_TAG, ">>> onDestroy() ", this);
        if (mServer != null) {
            try {
                Logger.v(LOG_TAG, "stopping Jetty");
                stopServer();
            } catch (Exception e) {
                ErrorHandling.signalServiceError(LOG_TAG, e, this);
            }
        }
        super.onDestroy();
        Logger.v(LOG_TAG, "<<< onDestroy() ", this);
    }

    @Override
    protected void onNewIntent(Intent pIntent) {
        Logger.v(LOG_TAG, ">>> onNewIntent() ", this);
        super.onNewIntent(pIntent);
        Logger.v(LOG_TAG, "<<< onNewIntent() ", this);
    }

    private void stopServer() throws Exception {
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopping");
        mServer.stop();
        mServer.join();
        Logger.i(LOG_TAG, "stopServer(): Jetty Server stopped. done.");
        mServer = null;
    }

    Server startServer(int pPort) throws Exception {
        SocketConnector connector = new SocketConnector();
        connector.setPort(pPort);
        connector.setHost("0.0.0.0"); // listen on all interfaces

        Server server = new Server();
        server.setConnectors(new Connector[] { connector });

        // Bridge Jetty logging to Android logging
        // System.setProperty("org.mortbay.log.class",
        // "org.mortbay.log.AndroidLog");
        // org.mortbay.log.Log.setLog(new AndroidLog());

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { new SynergyServerHandler() });
        server.setHandler(handlers);

        server.start();
        QueuedThreadPool threadpool = (QueuedThreadPool) server.getThreadPool();
        threadpool.setMaxStopTimeMs(10);

        return server;
    }





}
