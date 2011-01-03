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
    private static final byte[]     SYNERGY_VERSION         = {0,1,0,3};

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

    private int                     mClientScreenPosLeft    = 0;
    private int                     mClientScreenPosTop     = 0;
    private int                     mClientScreenWarpZone   = 0;
    private int                     mClientScreenMousePosX  = 0;
    private int                     mClientScreenMousePosY  = 0;

    public int                     mClientScreenWidth      = 0;
    public int                     mClientScreenHeight     = 0;

    // -------------------------
    // |    Private Methods    |
    // -------------------------

    // ..........................
    // .    sending messages    .
    // ..........................
    private void sendMessage(String pMessageString, byte[] pDataArray) {
        Vector<Byte> messageVector = SynergyServerHelper.prepareMessageFromString(pMessageString, pDataArray.length);
        for(int i=0; i<pDataArray.length; ++i){
           messageVector.add((byte)pDataArray[i]); 
        }
        mSendQueue.add(messageVector);
    }

    private void sendMessage(String pMessageString) {
        Vector<Byte> messageVector = SynergyServerHelper.prepareMessageFromString(pMessageString, 0);
        mSendQueue.add(messageVector);
    }

    // .....................................
    // .    Hearbeat & Connection Timer    .
    // .....................................
    private void resetConnectionTimer() {
        mConnectionTimer = System.currentTimeMillis();
    }

    private void resetHeartBeatTimer() {
        mHeartBeatTimer = System.currentTimeMillis();
    }

    private boolean checkConnectionTimeout() {
        if ( (System.currentTimeMillis() - mConnectionTimer) > CONNECTION_TIMEOUT ) {
            Logger.v(LOG_TAG, "Timeout reached");
            return true;
        }
        return false;
    }

    private boolean checkHeartBeatTimeout() {
        if (mIsConnectedToClient) {
            if ( (System.currentTimeMillis() - mHeartBeatTimer) >= HEARTBEAT_TIMEOUT ) {
                resetHeartBeatTimer();
                return true;
            }
        }
        return false;
    }

    // ............................
    // .    message processing    .
    // ............................
    private void receiveMessagesToQueue() {
        try{
            if ( mClientSocket.getInputStream().available() > 0) {
                Vector<Byte> receivedMessage = new Vector<Byte>();
                while ( mClientSocket.getInputStream().available() > 0) {
                    receivedMessage.add( (byte) mClientSocket.getInputStream().read() );
                }
                if(SynergyServerHelper.addMessagetoQueue(receivedMessage,mReceiveQueue) >0 ){
                    resetConnectionTimer();
                }
            }
        } catch (IOException e) {
            Logger.v(LOG_TAG, "IO exception at socket input stream reading: ", e);
            // try to reconnect
            closeClientConnection();
        } catch (InterruptedException e) {
            Logger.v(LOG_TAG, "interrupted exception at adding to message queue: ", e);
        }
    }

    private void parseReceivedMessages() {
        Vector<Byte> message;
        while(mReceiveQueue.peek()!= null){
            try{
                message = mReceiveQueue.poll(0,TimeUnit.SECONDS);

                if ( (SynergyServerHelper.messageSearchString(message,"CALV")!= true) &&
                     (SynergyServerHelper.messageSearchString(message,"CNOP")!= true) 
                        ) {
                    Logger.v(LOG_TAG, "reading message: ", SynergyServerHelper.messageToString(message) );
                }

                if ( SynergyServerHelper.messageSearchString(message,"Synergy") ) {
                    Logger.v(LOG_TAG,"got hello message");
                    sendMessage("QINF");
                    resetConnectionTimer();

                } else if ( SynergyServerHelper.messageSearchString(message,"CALV") ) {
                    //Logger.v(LOG_TAG,"got keep alive message");
                    resetConnectionTimer();

                } else if ( SynergyServerHelper.messageSearchString(message,"DINF") ) {
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

                } else if ( SynergyServerHelper.messageSearchString(message,"CNOP") ) {
                    //Logger.v(LOG_TAG,"got nop message");

                } else if ( SynergyServerHelper.messageSearchString(message,"CCLP") ) {
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
                    if (socketWriter == null ) {
                        Logger.v(LOG_TAG, " socket writer is null ?????");
                        break;
                    }
                    socketWriter.write( (int) message.get(i) );
                }
                if ( (SynergyServerHelper.messageSearchString(message,"CALV")!= true) 
                     && (SynergyServerHelper.messageSearchString(message,"DMMV")!= true)
                     && (SynergyServerHelper.messageSearchString(message,"DMRM")!= true)
                ) {
                    Logger.v(LOG_TAG, "writing message: ", SynergyServerHelper.messageToString(message) );
                }
                if (socketWriter != null ) {
                    socketWriter.flush();
                }
            } catch (InterruptedException e){
                Logger.v(LOG_TAG, "InterruptedException at message polling: ", e);
            } catch (IOException e){
                Logger.v(LOG_TAG, "IOexception at message writing: ", e);
                // try to reconnect
                closeClientConnection();
            }
        }
    }

    // ............................
    // .    connection closing    .
    // ............................
    private void closeClientConnection() {
        Logger.v(LOG_TAG, "closing Client connection");
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

    private void closeServerConnection() {
        Logger.v(LOG_TAG, "closing Server connection");
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
                        Logger.v(LOG_TAG, "thread sleep exception: ", e);
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
                            // try to reconnect
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
                            closeClientConnection();
                            // try to reconnect
                            continue; 
                        }
                        receiveMessagesToQueue();
                        parseReceivedMessages(); 
                        if (checkHeartBeatTimeout()) {
                            sendMessage("CALV");
                        }
                        if (socketWriter != null ) {
                            sendMessageQueue();
                        }
                    }
                }
                closeClientConnection();
                closeServerConnection();
                Logger.v(LOG_TAG,"stopping socket thread");
            }
        });
        mSocketThread.start();
    }

    public void stop() {
        Logger.v(LOG_TAG, "stopping socket thread");
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
                    (byte) ((x>>8) & SynergyServerHelper.BITMASK),
                    (byte) ( x & SynergyServerHelper.BITMASK),
                    (byte) ((y>>8) & SynergyServerHelper.BITMASK),
                    (byte) ( y & SynergyServerHelper.BITMASK) } );
        }
    }

    public void relativeMousePosition(int x, int y){
        if (mIsConnectedToClient) {
            sendMessage("DMRM",new byte[]{  
                    (byte) ((x>>8) & SynergyServerHelper.BITMASK),
                    (byte) ( x & SynergyServerHelper.BITMASK),
                    (byte) ((y>>8) & SynergyServerHelper.BITMASK),
                    (byte) ( y & SynergyServerHelper.BITMASK) } );
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
        if (mIsConnectedToClient) {
            sendMessage("DKDN",new byte[]{0x00,0x00,0x00, 0x00,0x00,0x00});
            
            /*
            sendMessage("DKDN",new byte[]{0xef,0x51,0x00, 0x00,0x00,0x7c}); // left arrow down
            sendMessage("DKDN",new byte[]{0x00,0x61,0x00, 0x00,0x00,0x01}); // a down
            sendMessage("DKDN",new byte[]{0x00,0x62,0x00, 0x00,0x00,0x0c}); // b down
            sendMessage("DKDN",new byte[]{0x00,0x20,0x00, 0x00,0x00,0x32}); // space down
            sendMessage("DKDN",new byte[]{0xef,0x0d,0x00, 0x00,0x00,0x25}); // CR down
            sendMessage("DKDN",new byte[]{0xef,0xc2,0x00, 0x00,0x00,0x61}); // f5 down
            sendMessage("DKDN",new byte[]{0x00,0x31,0x00, 0x00,0x00,0x13}); // 1 down
            sendMessage("DKDN",new byte[]{0xef,0x53,0x00, 0x00,0x00,0x7d}); // right arrow down
            */
        }
    }

    public void keyUp(int key){
        if (mIsConnectedToClient) {
            sendMessage("DKUP",new byte[]{0x00,0x00,0x00, 0x00,0x00,0x00});

            /*
            sendMessage("DKUP",new byte[]{0x00,0x00,0x00, 0x00,0x00,0x7c}); // left arrow up
            sendMessage("DKUP",new byte[]{0x00,0x00,0x00, 0x00,0x00,0x01}); // a up
            */
        }
    }


    public void keyDownArrowLeft() {
        if (mIsConnectedToClient) {
            sendMessage("DKDN",new byte[]{(byte)0xef,(byte)0x51,(byte)0x00, (byte)0x00,(byte)0x00,(byte)0x7c});
        }
    }
    public void keyDownArrowRight() {
        if (mIsConnectedToClient) {
            sendMessage("DKDN",new byte[]{(byte)0xef,(byte)0x53,(byte)0x00, (byte)0x00,(byte)0x00,(byte)0x7d});
        }
    }
    public void keyDownArrowUp() {
        if (mIsConnectedToClient) {
            sendMessage("DKDN",new byte[]{(byte)0xef,(byte)0x52,(byte)0x00, (byte)0x00,(byte)0x00,(byte)0x7f});
        }
    }
    public void keyDownArrowDown() {
        if (mIsConnectedToClient) {
            sendMessage("DKDN",new byte[]{(byte)0xef,(byte)0x54,(byte)0x00, (byte)0x00,(byte)0x00,(byte)0x7e});
        }
    }
    public void keyUpArrowLeft() {
        if (mIsConnectedToClient) {
            sendMessage("DKUP",new byte[]{(byte)0x00,(byte)0x00,(byte)0x00, (byte)0x00,(byte)0x00,(byte)0x7c});
        }
    }
    public void keyUpArrowRight() {
        if (mIsConnectedToClient) {
            sendMessage("DKUP",new byte[]{(byte)0x00,(byte)0x00,(byte)0x00, (byte)0x00,(byte)0x00,(byte)0x7d});
        }
    }
    public void keyUpArrowUp() {
        if (mIsConnectedToClient) {
            sendMessage("DKUP",new byte[]{(byte)0x00,(byte)0x00,(byte)0x00, (byte)0x00,(byte)0x00,(byte)0x7f});
        }
    }
    public void keyUpArrowDown() {
        if (mIsConnectedToClient) {
            sendMessage("DKUP",new byte[]{(byte)0x00,(byte)0x00,(byte)0x00, (byte)0x00,(byte)0x00,(byte)0x7e});
        }
    }
}
