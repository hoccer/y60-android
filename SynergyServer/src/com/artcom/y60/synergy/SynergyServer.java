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

    private ServerSocket            mServerSocket = null;
    private Socket                  mClientSocket = null;

    private static final int        SYNERGY_PORT = 24800;
    private static final int        SYNERGY_TIMEOUT = 1000;

    private Button                  mButton1;
    private Button                  mButton2;





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
            }
        });
        mButton2 = (Button) findViewById(R.id.button_2);
        mButton2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.v(LOG_TAG,"button 2 pressed");
            }
        });

        try {
            mServerSocket = new ServerSocket(SYNERGY_PORT);
        } catch (IOException e) {
            Logger.v(LOG_TAG,"could listen to port: ", SYNERGY_PORT, " with exception: ", e);
        }

        try {
            mServerSocket.setSoTimeout(SYNERGY_TIMEOUT);
        } catch (SocketException e) {
            Logger.v(LOG_TAG,"could not set socket timeout to: ", SYNERGY_TIMEOUT, " milliseconds, with exception: ", e);
        }

        if (mServerSocket != null) {
            mSocketThread = new Thread(new Runnable() {
                private long                mTimer;
                private static final int    TIMEOUT = 10000;

                //private BlockingQueue<Vector<Character>> mSendQueue;
                //private BlockingQueue<Vector<Character>> mReceiveQueue;

                private BlockingQueue<Vector<Byte>> mSendQueue = new LinkedBlockingQueue<Vector<Byte>>();
                private BlockingQueue<Vector<Byte>> mReceiveQueue = new LinkedBlockingQueue<Vector<Byte>>();

                //private BlockingQueue<char[]> mSendQueue = new BlockingQueue<char[]>();
                //private BlockingQueue<char[]> mReceiveQueue = new BlockingQueue<char[]>();

                private static final int    BITMASK = 0x000000FF;


                private OutputStreamWriter  socketWriter = null;


                private BufferedReader clientInput = null;


                private void sendMessage(String messageText, Vector<Byte> messageData) {
                    int messageLength = messageText.length() + messageData.size();
                    Vector<Byte>    messageVector = new Vector<Byte>();
                    messageVector.add( (byte)(messageLength >> 24 & BITMASK) );
                    messageVector.add( (byte)(messageLength >> 16 & BITMASK) );
                    messageVector.add( (byte)(messageLength >> 8 & BITMASK) );
                    messageVector.add( (byte)(messageLength & BITMASK) );
                    for(int i=0;i<messageText.length();++i){
                       messageVector.add( (byte)messageText.charAt(i) ); 
                    }
                    messageVector.addAll(messageData);
                    mSendQueue.add(messageVector);
                }
                private void sendMessage(String messageText, byte[] byteArray) {
                    int messageLength = messageText.length() + byteArray.length;
                    Vector<Byte>    messageVector = new Vector<Byte>();
                    messageVector.add( (byte)(messageLength >> 24 & BITMASK) );
                    messageVector.add( (byte)(messageLength >> 16 & BITMASK) );
                    messageVector.add( (byte)(messageLength >> 8 & BITMASK) );
                    messageVector.add( (byte)(messageLength & BITMASK) );
                    int i;
                    for(i=0;i<messageText.length();++i){
                       messageVector.add( (byte)messageText.charAt(i) ); 
                    }
                    for(i=0;i<byteArray.length;++i){
                       messageVector.add( (byte) byteArray[i] ); 
                    }
                    mSendQueue.add(messageVector);
                }


                @Override
                public void run() {
                    Logger.v(LOG_TAG,"starting socket thread");
                    resetTimer();
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
                                //Logger.v(LOG_TAG, "input length: ", mClientSocket.getInputStream().available() );
                                resetTimer();

                                clientInput = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
                                socketWriter = new OutputStreamWriter( mClientSocket.getOutputStream());


                                /*
                                Logger.v(LOG_TAG, "about to write message");
                                //socketWriter = new OutputStreamWriter( mClientSocket.getOutputStream());
                                String messageText = "Synergy";
                                int messageLength = messageText.length() + 4;
                                socketWriter.write( messageLength >> 24 & BITMASK );
                                socketWriter.write( messageLength >> 16 & BITMASK );
                                socketWriter.write( messageLength >> 8 & BITMASK );
                                socketWriter.write( messageLength & BITMASK );
                                socketWriter.write( messageText );
                                socketWriter.write( 0 );
                                socketWriter.write( 1 );
                                socketWriter.write( 0 );
                                socketWriter.write( 3 );
                                socketWriter.flush();
                                //socketWriter.close();
                                Logger.v(LOG_TAG, "wrote message");
                                */

                                sendMessage("Synergy", new byte[]{0,1,0,3});


                            } catch (SocketTimeoutException e) {
                                //Logger.v(LOG_TAG,"Timeout exception: ",e);
                                //break;
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
                        }

                        /*
                        if ( (System.currentTimeMillis() - mTimer) > TIMEOUT ) {
                            Logger.v(LOG_TAG, "Timeout reached");
                            try {
                                if (mClientSocket != null) {
                                    mClientSocket.close();
                                }
                            } catch(IOException e){
                                Logger.v(LOG_TAG, "IO exception during socket close: ", e);
                            }
                            break;
                        }
                        */

                        /*
                        if (mClientSocket != null) {
                            try{
                                //Logger.v(LOG_TAG,"about to read a line from input");
                                String inputLine = clientInput.readLine();
                                if ( inputLine != null ) {
                                    Logger.v(LOG_TAG,"read line: ", inputLine);
                                }
                            } catch (Exception e) {

                                Logger.v(LOG_TAG, "exception: ", e);
                            }
                        }
                        */

                        if (mClientSocket != null) {
                            try{
                                if ( mClientSocket.getInputStream().available() > 0) {
                                    Vector<Byte> receiveMessage = new Vector<Byte>();
                                    String receivedString = "";
                                    while ( mClientSocket.getInputStream().available() > 0) {
                                        int readByte = mClientSocket.getInputStream().read();
                                        receivedString += (byte) readByte;
                                        if ( Character.isISOControl( (char) (byte) readByte ) == false ) {
                                            receivedString += "(" + (char) (byte) readByte + ")";
                                        }
                                        receivedString += ",";

                                        receiveMessage.add( (byte) readByte );
                                    }

                                    int messageLength = (receiveMessage.get(0) << 24) +
                                                        (receiveMessage.get(1) << 16) +
                                                        (receiveMessage.get(2) <<  8) +
                                                        (receiveMessage.get(3) );

                                    mReceiveQueue.put(receiveMessage);
                                    Logger.v(LOG_TAG, "received message length: ", messageLength);
                                    Logger.v(LOG_TAG, "received message length: ", receiveMessage.size());
                                    Logger.v(LOG_TAG, "received message: ", receivedString);
                                }

                            } catch (Exception e) {
                                Logger.v(LOG_TAG, "exception: ", e);
                            }
                        }



                        Vector<Byte> message;
                        String       messageString;
                        while(mSendQueue.peek()!= null){
                            try{
                                message = mSendQueue.poll(0,TimeUnit.SECONDS);
                                messageString = "";
                                for(int i=0;i<message.size();++i){
                                    socketWriter.write( (int) message.get(i) );
                                    messageString += (byte) message.get(i);
                                    if ( Character.isISOControl( (char) (byte) message.get(i) ) == false ) {
                                        messageString += "(" + (char) (byte) message.get(i) + ")";
                                    }
                                    messageString += ",";
                                }
                                socketWriter.flush();
                                Logger.v(LOG_TAG,"wrote message: " + messageString);
                            } catch (InterruptedException e){
                                Logger.v(LOG_TAG, "InterruptedException at message polling: ", e);
                            } catch (IOException e){
                                Logger.v(LOG_TAG, "IOexception at message writing: ", e);
                            }
                        }




                    }
                    Logger.v(LOG_TAG,"stopping socket thread");
                }
                private void resetTimer() {
                    mTimer = System.currentTimeMillis();
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
