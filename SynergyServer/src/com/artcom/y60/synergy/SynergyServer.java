package com.artcom.y60.synergy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
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

import com.artcom.y60.Logger;

public class SynergyServer {

    // ------------------------
    // |    Static Members    |
    // ------------------------
    private static final String     LOG_TAG                 = "SynergyServer";
    private static final int        SYNERGY_PORT            = 24800;
    private static final int        BITMASK                 = 0x000000FF;
    private static final byte[]     SYNERGY_VERSION         = {0,1,0,3};
    private static final int        MESSAGE_SIZE_OFFSET     = 4;

    private static final int        SYNERGY_SOCKET_TIMEOUT  = 1000;
    private static final int        CONNECTION_TIMEOUT      = 3000;
    private static final int        HEARTBEAT_TIMEOUT       = 500;
    private static final int        MAIN_LOOP_TIMEOUT       = 10;

    // -------------------------
    // |    Private Members    |
    // -------------------------
    private BlockingQueue<Vector<Byte>> mSendQueue          = new LinkedBlockingQueue<Vector<Byte>>();
    private BlockingQueue<Vector<Byte>> mReceiveQueue       = new LinkedBlockingQueue<Vector<Byte>>();

    private Thread                  mSocketThread           = null;
    private boolean                 mSocketThreadRun        = false;
    private ServerSocket            mServerSocket           = null;
    private Socket                  mClientSocket           = null;
    private OutputStream            socketWriter            = null;
    private boolean                 mIsConnectedToClient    = false;

    private long                    mConnectionTimer;
    private long                    mHeartBeatTimer;

    // ------------------------
    // |    Public Members    |
    // ------------------------
    public int                      mClientScreenPosLeft;
    public int                      mClientScreenPosTop;
    public int                      mClientScreenWidth = 0;
    public int                      mClientScreenHeight = 0;
    public int                      mClientScreenWarpZone;
    public int                      mClientScreenMousePosX;
    public int                      mClientScreenMousePosY;

    // -------------------------
    // |    Private Methods    |
    // -------------------------
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

    /*
    private void sendMessage(String pMessageString, Vector<Byte> messageData) {
        Vector<Byte> messageVector = prepareMessageFromString(pMessageString, messageData.size());
        messageVector.addAll(messageData);
        mSendQueue.add(messageVector);
    }
    */

    private void sendMessage(String pMessageString, byte[] pDataArray) {
    //public void sendMessage(String pMessageString, byte[] pDataArray) {
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

    private void resetConnectionTimer() {
        mConnectionTimer = System.currentTimeMillis();
    }

    private void resetHeartBeatTimer() {
        mHeartBeatTimer = System.currentTimeMillis();
    }

    private boolean checkConnectionTimeout() {
        if ( (System.currentTimeMillis() - mConnectionTimer) > CONNECTION_TIMEOUT ) {
            Logger.v(LOG_TAG, "Timeout reached");
            closeClientConnection();
            return true;
        }
        return false;
    }

    private void checkHeartBeatTimeout() {
        if (mIsConnectedToClient) {
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
            if ( (i+MESSAGE_SIZE_OFFSET) >= message.size() ) {
                messageContainsString = false;
                break;
            }
            if ( string.charAt(i) != message.get(i+MESSAGE_SIZE_OFFSET) ) {
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

                if (receivedMessage.size() == (messageLength+MESSAGE_SIZE_OFFSET) ){
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
                        if ( (i+MESSAGE_SIZE_OFFSET+messageLength) <= receivedMessage.size() ) {
                            Vector<Byte> message = new Vector<Byte>();
                            for(j=i;j<(i+MESSAGE_SIZE_OFFSET+messageLength);++j) {
                                message.add( receivedMessage.get(j) );
                            }
                            mReceiveQueue.put(message);
                            resetConnectionTimer();
                            i += MESSAGE_SIZE_OFFSET;
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

                if ( (messageSearchString(message,"CALV")!= true) &&
                     (messageSearchString(message,"CNOP")!= true) 
                        ) {
                    Logger.v(LOG_TAG, "reading message: ", messageToString(message) );
                }

                if ( messageSearchString(message,"Synergy") ) {
                    Logger.v(LOG_TAG,"got hello message");
                    sendMessage("QINF");
                    resetConnectionTimer();

                } else if ( messageSearchString(message,"CALV") ) {
                    //Logger.v(LOG_TAG,"got keep alive message");
                    resetConnectionTimer();

                } else if ( messageSearchString(message,"DINF") ) {
                    Logger.v(LOG_TAG,"got Dinfo message");

                    mClientScreenPosLeft =  (message.get(8) << 8) + message.get(9);
                    mClientScreenPosTop =  (message.get(10) << 8) + message.get(11);
                    mClientScreenWidth =  (message.get(12) << 8) + message.get(13);
                    mClientScreenHeight =  (message.get(14) << 8) + message.get(15);
                    mClientScreenWarpZone =  (message.get(16) << 8) + message.get(17);
                    mClientScreenMousePosX =  (message.get(18) << 8) + message.get(19);
                    mClientScreenMousePosY =  (message.get(20) << 8) + message.get(21);

                    sendMessage("CIAK");
                    if ( mIsConnectedToClient == false ) {
                        sendMessage("CROP");
                        sendMessage("DSOP",new byte[]{0,0,0,0});
                        sendMessage("CINN",new byte[]{0,0,0,0,0, 0,0,0,0,0});
                        sendMessage("CALV");
                        mIsConnectedToClient = true;
                    }
                    resetConnectionTimer();

                } else if ( messageSearchString(message,"CNOP") ) {
                    //Logger.v(LOG_TAG,"got nop message");

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
                if ( (messageSearchString(message,"CALV")!= true) 
                     && (messageSearchString(message,"DMMV")!= true)
                     && (messageSearchString(message,"DMRM")!= true)
                ) {
                    Logger.v(LOG_TAG, "writing message: ", messageToString(message) );
                }
                socketWriter.flush();
            } catch (InterruptedException e){
                Logger.v(LOG_TAG, "InterruptedException at message polling: ", e);
            } catch (IOException e){
                Logger.v(LOG_TAG, "IOexception at message writing: ", e);
                closeClientConnection();
            }
        }
    }
    private void closeClientConnection() {
        Logger.v(LOG_TAG, "closing connection");
        try {
            if (socketWriter != null) {
                socketWriter.close();
                socketWriter = null;
            }
        } catch(IOException e) {
            Logger.v(LOG_TAG, "IO exception at socket Writer closing ", e);
        }
        mIsConnectedToClient = false;
        try {
            if (mClientSocket != null) {
                mClientSocket.close();
                mClientSocket = null;
            }
        } catch(IOException e) {
            Logger.v(LOG_TAG, "IO exception at Socket closing ", e);
        }
    }

    private void closeConnection() {
        closeClientConnection();
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch(IOException e) {
            Logger.v(LOG_TAG, "IO exception at Socket closing ", e);
        }
    }

    // ------------------------
    // |    Public Methods    |
    // ------------------------
    public void start() {
        if (mSocketThread != null ) {
            stop();
        }

        mSendQueue.clear();
        mReceiveQueue.clear();
        try {
            mServerSocket = new ServerSocket(SYNERGY_PORT);
            mServerSocket.setSoTimeout(SYNERGY_SOCKET_TIMEOUT);
        } catch (SocketException e) {
            Logger.v(LOG_TAG,"could not set socket timeout to: ", SYNERGY_SOCKET_TIMEOUT, ": ", e);
        } catch (IOException e) {
            Logger.v(LOG_TAG,"could listen to port: ", SYNERGY_PORT, ": ", e);
        }

        mSocketThreadRun = true;
        mSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.v(LOG_TAG,"starting socket thread");
                while(true) {
                    if (mSocketThreadRun != true) {
                        if (mIsConnectedToClient) {
                            if (mSendQueue.peek() == null){
                                break;
                            }
                            Logger.v(LOG_TAG,"waiting for all messages to be send");
                        } else {
                            break;
                        }
                    }
                    try {
                        Thread.sleep(MAIN_LOOP_TIMEOUT);
                    } catch (InterruptedException e ) {
                        Logger.v(LOG_TAG, "sleep exception: ", e);
                    }
                    if (mClientSocket == null) {
                        try {
                            Logger.v(LOG_TAG, "waiting for socket accept");
                            mClientSocket = mServerSocket.accept();
                            Logger.v(LOG_TAG, "socket connection: " , mClientSocket.toString() );
                            socketWriter = mClientSocket.getOutputStream();
                            sendMessage("Synergy", SYNERGY_VERSION);
                            resetConnectionTimer();
                            resetHeartBeatTimer();
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
                            Logger.v(LOG_TAG,"accept failed: ",e);
                            break;
                        }
                    } else {
                        if (checkConnectionTimeout()) {
                            continue;
                        }
                        receiveMessagesToQueue();
                        parseReceivedMesseges(); 
                        checkHeartBeatTimeout();
                        sendMessageQueue();
                    }
                }
                closeConnection();
                Logger.v(LOG_TAG,"stopping socket thread");
            }
        });
        mSocketThread.start();
    }

    public void stop() {
        Logger.v(LOG_TAG, "trying to stop socket thread");
        sendMessage("CBYE");
        mSocketThreadRun = false;
        try {
            if (mSocketThread != null) {
                mSocketThread.join();
                mSocketThread = null;
            }
        } catch (InterruptedException e ) {
            Logger.v(LOG_TAG, "could not stop socket listening thread, with exception: ", e);
        }
        Logger.v(LOG_TAG, "socket thread stopped");
    }

    public boolean isConnected() {
        return mIsConnectedToClient;
    }

    public void absoluteMousePosition(int x, int y){
        if (mIsConnectedToClient) {
            sendMessage("DMMV",new byte[]{  
                    (byte) ((x>>8) & BITMASK),
                    (byte) ( x & BITMASK),
                    (byte) ((y>>8) & BITMASK),
                    (byte) ( y & BITMASK) } );
        }
    }

    public void relativeMousePosition(int x, int y){
        if (mIsConnectedToClient) {
            sendMessage("DMRM",new byte[]{  
                    (byte) ((x>>8) & BITMASK),
                    (byte) ( x & BITMASK),
                    (byte) ((y>>8) & BITMASK),
                    (byte) ( y & BITMASK) } );
        }
    }

    public void mouseButtonLeftDown(){
        if (mIsConnectedToClient) {
            sendMessage("DMDN",new byte[]{1});
        }
    }

    public void mouseButtonMiddleDown(){
        if (mIsConnectedToClient) {
            sendMessage("DMDN",new byte[]{2});
        }
    }

    public void mouseButtonRightDown(){
        if (mIsConnectedToClient) {
            sendMessage("DMDN",new byte[]{3});
        }
    }

    public void mouseButtonLeftUp(){
        if (mIsConnectedToClient) {
            sendMessage("DMUP",new byte[]{1});
        }
    }

    public void mouseButtonMiddleUp(){
        if (mIsConnectedToClient) {
            sendMessage("DMUP",new byte[]{2});
        }
    }

    public void mouseButtonRightUp(){
        if (mIsConnectedToClient) {
            sendMessage("DMUP",new byte[]{3});
        }
    }

    public void keyDown(int key){
    }

    public void keyUp(int key){
    }

}
