package com.artcom.y60.synergy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.artcom.y60.Logger;

public class SynergyServer extends Activity {

    private static final String     LOG_TAG          = "SynergyServer";

    private Thread                  mSocketThread = null;
    private boolean                 mSocketThreadRun = false;

    private static final int        SYNERGY_PORT = 24800;
    private static final int        SYNERGY_SOCKET_TIMEOUT = 1000;
    private static final int        BITMASK = 0x000000FF;
    private static final int        CONNECTION_TIMEOUT = 3000;
    private static final int        HEARTBEAT_TIMEOUT = 500;
    private static final byte[]     SYNERGY_VERSION = {0,1,0,3};

    private Button                  mButton1;
    private Button                  mButton2;

    private BlockingQueue<Vector<Byte>> mSendQueue = new LinkedBlockingQueue<Vector<Byte>>();
    private BlockingQueue<Vector<Byte>> mReceiveQueue = new LinkedBlockingQueue<Vector<Byte>>();

    private ServerSocket            mServerSocket = null;
    private Socket                  mClientSocket = null;

    private long                    mConnectionTimer;
    private long                    mHeartBeatTimer;

    private OutputStreamWriter      socketWriter = null;

    private boolean                 mIsConnectedToClient = false;


    private Vector<Byte> prepareMessageFromString(String pMessageString, int pDataLength) {
        int messageLength = pMessageString.length() + pDataLength;
        Vector<Byte> messageVector = new Vector<Byte>();
        messageVector.add((byte)(messageLength >> 24 & BITMASK));
        messageVector.add((byte)(messageLength >> 16 & BITMASK));
        messageVector.add((byte)(messageLength >> 8 & BITMASK));
        messageVector.add((byte)(messageLength & BITMASK));
        for(int i=0; i<pMessageString.length(); ++i){
           messageVector.add((byte)pMessageString.charAt(i)); 
        }
        return messageVector;
    }
    private void sendMessage(String pMessageString, Vector<Byte> messageData) {
        Vector<Byte> messageVector = prepareMessageFromString(pMessageString, messageData.size());
        messageVector.addAll(messageData);
        mSendQueue.add(messageVector);
    }
    private void sendMessage(String pMessageString, byte[] pDataArray) {
        Vector<Byte> messageVector = prepareMessageFromString(pMessageString, pDataArray.length);
        for(int i=0; i<pDataArray.length; ++i){
           messageVector.add((byte)pDataArray[i]); 
        }
        mSendQueue.add(messageVector);
    }
    private void sendMessage(String pMessageString) {
        Vector<Byte> messageVector = prepareMessageFromString(pMessageString, 0);
        mSendQueue.add(messageVector);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.v(LOG_TAG, ">>> onCreate() ", this);
        setContentView(R.layout.main);

        mButton1 = (Button) findViewById(R.id.button_1);
        mButton1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.v(LOG_TAG,"button 1 pressed");

                if (mIsConnectedToClient) {
                    int mousePositionX = 0;
                    int mousePositionY = 0;
                    sendMessage("DMMV",new byte[]{  
                            (byte) ((mousePositionX>>8) & BITMASK),
                            (byte) (mousePositionX & BITMASK),
                            (byte) ((mousePositionY>>8) & BITMASK),
                            (byte) (mousePositionY & BITMASK) } );
                }
            }
        });
        mButton2 = (Button) findViewById(R.id.button_2);
        mButton2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.v(LOG_TAG,"button 2 pressed");
                if (mIsConnectedToClient) {
                    int mousePositionX = 100;
                    int mousePositionY = 100;
                    sendMessage("DMMV",new byte[]{  
                            (byte) ((mousePositionX>>8) & BITMASK),
                            (byte) (mousePositionX & BITMASK),
                            (byte) ((mousePositionY>>8) & BITMASK),
                            (byte) (mousePositionY & BITMASK) } );
                }
            }
        });

        try {
            mServerSocket = new ServerSocket(SYNERGY_PORT);
        } catch (IOException e) {
            Logger.v(LOG_TAG,"could listen to port: ", SYNERGY_PORT, " with exception: ", e);
        }

        try {
            mServerSocket.setSoTimeout(SYNERGY_SOCKET_TIMEOUT);
        } catch (SocketException e) {
            Logger.v(LOG_TAG,"could not set socket timeout to: ", SYNERGY_SOCKET_TIMEOUT, " milliseconds, with exception: ", e);
        }

        if (mServerSocket != null) {
            mSocketThread = new Thread(new Runnable() {

                private void resetConnectionTimer() {
                    mConnectionTimer = System.currentTimeMillis();
                }
                private void resetHeartBeatTimer() {
                    mHeartBeatTimer = System.currentTimeMillis();
                }

                private void closeConnection() {
                    Logger.v(LOG_TAG, "closing connection");
                    try {
                        socketWriter.close();
                    } catch(IOException e) {
                        Logger.v(LOG_TAG, "IO exception at socket Writer closing ", e);
                    }
                    try {
                        if (mClientSocket != null) {
                            mClientSocket.close();
                        }
                    } catch(IOException e) {
                        Logger.v(LOG_TAG, "IO exception at Socket closing ", e);
                    }
                    mIsConnectedToClient = false;
                }

                private void checkConnectionTimeout() {
                    if ( (System.currentTimeMillis() - mConnectionTimer) > CONNECTION_TIMEOUT ) {
                        Logger.v(LOG_TAG, "Timeout reached");
                        closeConnection();
                    }
                }
                private void checkHeartBeatTimeout() {
                    if (mIsConnectedToClient && (mClientSocket != null)) {
                        if ( (System.currentTimeMillis() - mHeartBeatTimer) >= HEARTBEAT_TIMEOUT ) {
                            resetHeartBeatTimer();
                            sendMessage("CALV");
                        }
                    }
                }

                private String messageToString(Vector<Byte> message) {
                    String       messageString = "";
                    for(int i=0;i<message.size();++i){
                        if ( i!=0 ){
                            messageString += ",";
                        }
                        messageString += (byte) message.get(i);
                        if ( Character.isISOControl( (char) (byte) message.get(i) ) == false ) {
                            messageString += "(" + (char) (byte) message.get(i) + ")";
                        }
                    }
                    return messageString;
                }

                private boolean messageSearchString(Vector<Byte> message, String string) {
                    boolean messageContainsString = true;
                    for(int i=0;i<string.length();++i) {
                        if ( (i+4) >= message.size() ) {
                            messageContainsString = false;
                            break;
                        }
                        if ( string.charAt(i) != message.get(i+4) ) {
                            messageContainsString = false;
                            break;
                        }
                    }
                    return messageContainsString;
                }

                private void receiveMessagesToQueue() {
                    try{
                        if ( mClientSocket.getInputStream().available() > 0) {
                            Vector<Byte> receivedMessage = new Vector<Byte>();
                            while ( mClientSocket.getInputStream().available() > 0) {
                                receivedMessage.add( (byte) mClientSocket.getInputStream().read() );
                            }
                            int messageLength = (receivedMessage.get(0) << 24) +
                                                (receivedMessage.get(1) << 16) +
                                                (receivedMessage.get(2) <<  8) +
                                                (receivedMessage.get(3) );

                            if (receivedMessage.size() == (messageLength+4) ){
                                mReceiveQueue.put(receivedMessage);
                                resetConnectionTimer();

                            } else {
                                int i = 0;
                                int j;
                                while(true){
                                    if ( i >= receivedMessage.size() ) {
                                        break;
                                    }
                                    messageLength = (receivedMessage.get(i+0) << 24) +
                                                        (receivedMessage.get(i+1) << 16) +
                                                        (receivedMessage.get(i+2) <<  8) +
                                                        (receivedMessage.get(i+3) );
                                    if ( (i+4+messageLength) <= receivedMessage.size() ) {
                                        Vector<Byte> message = new Vector<Byte>();
                                        for(j=i;j<(i+4+messageLength);++j) {
                                            message.add( receivedMessage.get(j) );
                                        }
                                        mReceiveQueue.put(message);
                                        resetConnectionTimer();
                                        i += 4;
                                        i += messageLength;
                                    }
                                }
                            }
                        }

                    } catch (IOException e) {
                        Logger.v(LOG_TAG, "IO exception at socket input stream reading: ", e);
                    } catch (InterruptedException e) {
                        Logger.v(LOG_TAG, "interrupted exception at adding to message queue: ", e);
                    }
                }


                private void parseReceivedMesseges() {
                    Vector<Byte> message;
                    while(mReceiveQueue.peek()!= null){
                        try{
                            message = mReceiveQueue.poll(0,TimeUnit.SECONDS);

                            if ( messageSearchString(message,"CALV")!= true ) {
                                Logger.v(LOG_TAG, "reading message: ", messageToString(message) );
                            }

                            if ( messageSearchString(message,"Synergy") ) {
                                Logger.v(LOG_TAG,"got hello message");
                                sendMessage("QINF");
                                resetConnectionTimer();

                            } else if ( messageSearchString(message,"CALV") ) {
                                resetConnectionTimer();

                            } else if ( messageSearchString(message,"DINF") ) {
                                Logger.v(LOG_TAG,"got Dinfo message");
                                sendMessage("CIAK");
                                if ( mIsConnectedToClient == false ) {
                                    sendMessage("CROP");
                                    sendMessage("DSOP",new byte[]{0,0,0,0});
                                    sendMessage("CINN",new byte[]{0,0,0,0,0, 0,0,0,0,0});
                                    sendMessage("CALV");
                                }
                                resetConnectionTimer();
                                mIsConnectedToClient = true;

                            } else if ( messageSearchString(message,"CNOP") ) {
                                Logger.v(LOG_TAG,"got nop message");

                            } else if ( messageSearchString(message,"CCLP") ) {
                                Logger.v(LOG_TAG,"got clipboard message");
                            }
                        } catch (InterruptedException e){
                            Logger.v(LOG_TAG, "InterruptedException at message polling: ", e);
                        }
                    }
                }

                private void sendMessageQueue() {
                    Vector<Byte> message;
                    while(mSendQueue.peek()!= null){
                        try{
                            message = mSendQueue.poll(0,TimeUnit.SECONDS);
                            for(int i=0;i<message.size();++i){
                                socketWriter.write( (int) message.get(i) );
                            }
                            if ( messageSearchString(message,"CALV")!= true ) {
                                Logger.v(LOG_TAG, "writing message: ", messageToString(message) );
                            }
                            socketWriter.flush();
                        } catch (InterruptedException e){
                            Logger.v(LOG_TAG, "InterruptedException at message polling: ", e);
                        } catch (IOException e){
                            Logger.v(LOG_TAG, "IOexception at message writing: ", e);
                            closeConnection();
                        }
                    }
                }

                @Override
                public void run() {
                    Logger.v(LOG_TAG,"starting socket thread");
                    while(mSocketThreadRun) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e ) {
                            Logger.v(LOG_TAG, "sleep exception: ", e);
                        }
                        if (mClientSocket == null) {
                            try {
                                Logger.v(LOG_TAG, "waiting for socket accept");
                                mClientSocket = mServerSocket.accept();
                                Logger.v(LOG_TAG, "got socket accept");
                                Logger.v(LOG_TAG, "socket: " , mClientSocket.toString() );
                                resetConnectionTimer();
                                resetHeartBeatTimer();
                                socketWriter = new OutputStreamWriter( mClientSocket.getOutputStream());
                                sendMessage("Synergy", SYNERGY_VERSION);
                                mIsConnectedToClient = false;

                            } catch (SocketTimeoutException e) {
                                continue;
                            } catch (IOException e) {
                                Logger.v(LOG_TAG,"I/O error: ",e);
                                break;
                            } catch (SecurityException e) {
                                Logger.v(LOG_TAG,"security error: ",e);
                                break;
                            } catch (IllegalBlockingModeException e) {
                                Logger.v(LOG_TAG,"Illegal Blocking Mode Exception: ",e);
                                break;
                            } catch (Exception e) {
                                Logger.v(LOG_TAG,"accept failed",e);
                                break;
                            }
                        } else {
                            checkConnectionTimeout();
                            receiveMessagesToQueue();
                            parseReceivedMesseges(); 
                            checkHeartBeatTimeout();
                            sendMessageQueue();
                        }
                    }
                    Logger.v(LOG_TAG,"stopping socket thread");
                }
            });
        }

        Logger.v(LOG_TAG, "<<< onCreate() ", this);
    }

    @Override
    protected void onStart() {
        Logger.v(LOG_TAG, ">>> onStart() ", this);
        super.onStart();

        if (mSocketThread != null) {
            mSocketThreadRun = true;
            mSocketThread.start();
        }

        Logger.v(LOG_TAG, "<<< onStart() ", this);
    }

    @Override
    protected void onDestroy() {
        Logger.v(LOG_TAG, ">>> onDestroy() ", this);

        if (mSocketThread != null) {
            Logger.v(LOG_TAG, "about to stop socket thread");
            mSocketThreadRun = false;
            try {
                mSocketThread.stop();
                mSocketThread.join();
            } catch (InterruptedException e ) {
                Logger.v(LOG_TAG, "could not stop socket listening thread, with exception: ", e);
            }
            Logger.v(LOG_TAG, "socket thread stopped");
        }
        try {
            if (mClientSocket != null) {
                mClientSocket.close();
            }
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch(IOException e){
            Logger.v(LOG_TAG, "IO exception during socket close: ", e);
        }
  
        super.onDestroy();
        Logger.v(LOG_TAG, "<<< onDestroy() ", this);
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
    protected void onNewIntent(Intent pIntent) {
        Logger.v(LOG_TAG, ">>> onNewIntent() ", this);
        super.onNewIntent(pIntent);
        Logger.v(LOG_TAG, "<<< onNewIntent() ", this);
    }

}
