package com.artcom.y60.trackpad;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.RemoteMousepointerClient;

public class TrackPad extends Activity {

   // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "Trackpad";

    public static final String ADDRESS_EXTRA = "com.artcom.y60.trackpad.ADDRESS";
    public static final String PORT_EXTRA = "com.artcom.y60.trackpad.PORT";

    // after receiving a store event, ignore further store events for this many
    // milliseconds
    private static final long IGNORE_PERIOD = 1 * 500;
    private long mTimestamp = 0; // Time of last store event
    private static final float SIGNIFICANCE_THRESHOLD = 10; // in pixels
    private static final long VIBE_TIME = 300; // in milliseconds
    private boolean mInterpretDownEvents = true;

    // Instance Variables ------------------------------------------------

    private float mOldX = -1;
    private float mOldY = -1;

    private InetAddress mAddress;
    private int mPort;

    private RemoteMousepointerClient mRemote = new RemoteMousepointerClient();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.trackpad);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
            InetAddress addr = InetAddress
                    .getByName((i.getStringExtra(PORT_EXTRA) == null) ? "localhost" : i
                            .getStringExtra(ADDRESS_EXTRA));
            int port = (i.getStringExtra(PORT_EXTRA) == null) ? 1999 : Integer.valueOf(i
                    .getStringExtra(PORT_EXTRA));

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

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // heul nich
        }
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        layout.setBackgroundColor(Color.BLACK);
    }

    private void move(MotionEvent pEvent) {
        boolean signalDown = false;

        if (!mInterpretDownEvents) {
            long now = System.currentTimeMillis();
            if (Math.abs(mTimestamp - now) > IGNORE_PERIOD) {
                mInterpretDownEvents = true;
            }
        }

        if (mOldX > -1 && mOldY > -1) {

            // origin is top left corner
            float dx = pEvent.getX() - mOldX;
            float dy = pEvent.getY() - mOldY;

            if (mInterpretDownEvents && Math.abs(dy) > SIGNIFICANCE_THRESHOLD && Math.abs(dx) < 1) {
                if (dy > 0) { // this is a downward movement, signaling a
                    // "store" event
                    signalDown = true;
                    mInterpretDownEvents = false;
                    mTimestamp = System.currentTimeMillis();
                }
            }

            mOldX = pEvent.getX();
            mOldY = pEvent.getY();

            move(dx, dy);

            if (signalDown) {
                feedbackStore();
            }

        } else {

            Logger.v(LOG_TAG, "ignoring unexpected move event, x = ", pEvent.getX(), ", y = ",
                    pEvent.getY());
        }
    }

    private void feedbackStore() {

        // Flash the screen and vibe to give the user feedback that a "store"
        // event has happened.
        // To make this be somewhat simultaneous, the viber is run in its own
        // thread.
        // Note that we cannot run the flasher code in its own thread as well,
        // since it needs to modify the UI/view, and in Android, only the thread
        // that created a view can modify it.

        Runnable goodVibes = new Runnable() {
            public void run() {

                // vibrate
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(VIBE_TIME);
            }
        };

        Thread viberThread = new Thread(goodVibes);
        viberThread.start();

        // flash the screen

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        //Drawable oldBg = layout.getBackground();

        layout.setBackgroundColor(Color.WHITE);

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
