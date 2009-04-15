package com.artcom.y60.trackpad;

import java.io.IOException;
import java.net.InetSocketAddress;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;

import de.sciss.net.OSCBundle;
import de.sciss.net.OSCClient;
import de.sciss.net.OSCMessage;

public class TrackPad extends Activity {
    
    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "Trackpad";
    
    
    
    // Instance Variables ------------------------------------------------

    private float mOldX = -1;
    
    private float mOldY = -1;
    
    private OSCClient mOscClient;

    
    
    // Public Instance Methods -------------------------------------------
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        try {
            mOscClient = OSCClient.newUsing(OSCClient.UDP);
            mOscClient.setTarget(new InetSocketAddress("127.0.0.1", 4711));
            
        } catch (IOException iox) {
        
            ErrorHandling.signalIOError(LOG_TAG, iox, this);
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
        
        try {
            disconnectFromDisplay();
            
        } catch (IOException iox) {
            
            ErrorHandling.signalIOError(LOG_TAG, iox, this);
        }
        
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        setContentView(R.layout.main);
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
            
        } catch (IOException iox) {
            
            ErrorHandling.signalIOError(LOG_TAG, iox, this);
        }
    }
    
    
    private void connectToDisplay() throws IOException {
        
        if (isConnected()) {
            Logger.w(LOG_TAG, "already connected - ignoring");
            return;
        }
        
        mOscClient.start();
    }
    
    
    private void disconnectFromDisplay() throws IOException {
        
        if (!isConnected()) {
            Logger.w(LOG_TAG, "not connected - ignoring");
            return;
        }
        
        mOscClient.stop();
    }
    
    
    private void sendMoveEvent(float pX, float pY) throws IOException {
        
        if (!isConnected()) {
            
            connectToDisplay();
        }
                 
        OSCBundle bndl = new OSCBundle();
        bndl.addPacket(new OSCMessage("x", new Object[]{ deltaToInt(pX) }));
        bndl.addPacket(new OSCMessage("y", new Object[]{ deltaToInt(pY) }));
        
        mOscClient.send(bndl);
        
//        OSCMessage msg = new OSCMessage("/move", new Object[]{ deltaToByte(pX), deltaToByte(pY)});
//        mOscClient.send(msg);
    }
    
    
    private int deltaToInt(float pDelta) {
        
        // should suffice
        return Math.round(pDelta);
    }
    
    
    private boolean isConnected() {
        
        return (mOscClient != null) && (mOscClient.isConnected());
    }
}