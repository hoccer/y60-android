package com.artcom.y60.trackpad;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.RemoteMousepointerClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class TiltController extends Activity {

    private static final String LOG_TAG = "TiltController";

    public static final String ADDRESS_EXTRA = "com.artcom.y60.trackpad.ADDRESS";
    public static final String PORT_EXTRA = "com.artcom.y60.trackpad.PORT";

    private float mOldX = -1;
    private float mOldY = -1;

    private InetAddress mAddress;
    private int mPort;

    private RemoteMousepointerClient mRemote = new RemoteMousepointerClient();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.trackpad);

        Logger.d(LOG_TAG, "TrackPad created");
    }

    @Override
    public boolean onTouchEvent(MotionEvent pEvent) {

        switch (pEvent.getAction()) {
        case MotionEvent.ACTION_DOWN:
            fingerDown(pEvent);
            return true;
        case MotionEvent.ACTION_UP:
            fingerUp(pEvent);
            return true;
        case MotionEvent.ACTION_MOVE:
            move(pEvent);
            return true;
        }

        return super.onTouchEvent(pEvent);
    }

    @Override
    public boolean onKeyDown(int pKeyCode, KeyEvent pEvent) {

        switch (pKeyCode) {
        case KeyEvent.KEYCODE_DPAD_UP:
            move(0, -1);
            return true;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            move(0, 1);
            return true;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            move(-1, 0);
            return true;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            move(1, 0);
            return true;
        }

        return super.onKeyDown(pKeyCode, pEvent);
    }

    // Protected Instance Methods ----------------------------------------

    @Override
    protected void onPause() {

        mRemote.disconnectFromDisplay();

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        Logger.v(LOG_TAG, "onResume");
        Intent i = getIntent();

        try {
            InetAddress addr = InetAddress.getByName(i.getStringExtra(ADDRESS_EXTRA));
            int port = Integer.valueOf(i.getStringExtra(PORT_EXTRA));

            Logger.d(LOG_TAG, "Old target:", mAddress, ":", mPort);
            Logger.d(LOG_TAG, "New target:", addr, ":", port);

            if (!addr.equals(mAddress) || port != mPort) {

                Logger.d(LOG_TAG, "reconnecting");
                mAddress = addr;
                mPort = port;
                mRemote.overrideTargetAndConnect(mAddress, mPort);
            }

        } catch (IOException x) {

            ErrorHandling.signalNetworkError(LOG_TAG, x, this);
        }

    }

    // Package Protected Instance Methods --------------------------------

    RemoteMousepointerClient getRemote() {

        return mRemote;
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

            Logger.v(LOG_TAG, "ignoring unexpected move event, x = ", pEvent.getX(), ", y = ",
                    pEvent.getY());
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
            mRemote.sendMoveEvent(pX, pY);

        } catch (SocketException x) {

            ErrorHandling.signalNetworkError(LOG_TAG, x, this);

        } catch (IOException iox) {

            ErrorHandling.signalIOError(LOG_TAG, iox, this);
        }
    }

}
