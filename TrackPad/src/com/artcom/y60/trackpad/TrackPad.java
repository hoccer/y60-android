package com.artcom.y60.trackpad;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;

public class TrackPad extends Activity {
    
    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "Trackpad";
    
    
    
    // Instance Variables ------------------------------------------------

    private float mOldX = -1;
    
    private float mOldY = -1;
    
    private InetAddress mAddress;
    
    private int mPort;
    
    private DatagramSocket mSocket;

    
    
    // Public Instance Methods -------------------------------------------
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        try {
            mAddress = InetAddress.getByName("192.168.9.229"); // TODO
            mPort    = 1999; // TODO
            
        } catch (UnknownHostException x) {
            
            ErrorHandling.signalNetworkError(LOG_TAG, x, this);
        }
        setContentView(R.layout.main);
        
        Logger.d(LOG_TAG, "TrackPad created");
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent pEvent) {

        switch (pEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: fingerDown(pEvent); return true;
            case MotionEvent.ACTION_UP:   fingerUp(pEvent);   return true;
            case MotionEvent.ACTION_MOVE: move(pEvent);       return true;
        }
        
        return super.onTouchEvent(pEvent);
    }

    @Override
    public boolean onKeyDown(int pKeyCode, KeyEvent pEvent) {
        
        switch (pKeyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:    move(0, -1); return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:  move(0,  1); return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:  move(-1, 0); return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT: move(1,  0); return true;
        }
        
        return super.onKeyDown(pKeyCode, pEvent);
    }

    
    
    // Protected Instance Methods ----------------------------------------

    @Override
    protected void onPause() {
        
        disconnectFromDisplay();
        
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }
    
    
    
    // Package Protected Instance Methods --------------------------------

    void overrideTargetAndConnect(InetAddress pAddr, int pPort) throws IOException {
        
        mAddress = pAddr;
        mPort    = pPort;
        disconnectFromDisplay();
        connectToDisplay();
    }
    


    // Private Instance Methods ------------------------------------------

    private void fingerDown(MotionEvent pEvent) {
        
        mOldX = pEvent.getX();
        mOldY = pEvent.getY();
    }
    
    private void fingerUp(MotionEvent pEvent) {
        
        mOldX = -1;
        mOldY = -1;
    }
    
    private void move(MotionEvent pEvent) {
        
        if (mOldX > -1 && mOldY > -1) {
            
            // origin is top left corner
            float dx = pEvent.getX() - mOldX;
            float dy = pEvent.getY() - mOldY;
            mOldX = pEvent.getX();
            mOldY = pEvent.getY();
            
            move(dx, dy);
            
        } else {
            
            Logger.v(LOG_TAG, "ignoring unexpected move event, x = ", pEvent.getX(), ", y = ", pEvent.getY());
        }
    }
    
    
    private void move(float pX, float pY) {
        
        Logger.v(LOG_TAG, "move, dx = ", pX, ", dy = ", pY);
        
        if (pX > 0) {
            Logger.v(LOG_TAG, "moving right");
        } else if (pX < 0) {
            Logger.v(LOG_TAG, "moving left");
        } else {
            Logger.v(LOG_TAG, "no horizontal movement");
        }
        
        if (pY > 0) {
            Logger.v(LOG_TAG, "moving down");
        } else if (pY < 0) {
            Logger.v(LOG_TAG, "moving up");
        } else {
            Logger.v(LOG_TAG, "no vertical movement");
        }
        
        try {
            sendMoveEvent(pX, pY);
            
        } catch (SocketException x) {
            
            ErrorHandling.signalNetworkError(LOG_TAG, x, this);
            
        } catch (IOException iox) {
            
            ErrorHandling.signalIOError(LOG_TAG, iox, this);
        }
    }
    
    
    private void connectToDisplay() throws SocketException {
        
        mSocket = new DatagramSocket();
    }
    
    
    private void disconnectFromDisplay() {
        
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket = null;
        }
    }
    
    
    private void sendMoveEvent(float pX, float pY) throws IOException, SocketException {
        
        if (mSocket == null) {
            
            connectToDisplay();
        }
        
        byte[]         payload = new byte[]{ deltaToByte(pX), deltaToByte(pY) };
        DatagramPacket packet  = new DatagramPacket(payload, payload.length, mAddress, mPort);
        
        Logger.d(LOG_TAG, "sending bytes ", payload[0], ", ", payload[1], " as UDP datagram");
        
        mSocket.send(packet);
    }
    
    
    private byte deltaToByte(float pDelta) {
        
        // should suffice
        return (byte)pDelta;
    }
    
    
    private boolean isConnected() {
        
        return (mSocket != null) && (mSocket.isConnected());
    }
}
