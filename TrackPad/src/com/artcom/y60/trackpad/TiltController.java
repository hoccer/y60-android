package com.artcom.y60.trackpad;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;
import com.artcom.y60.RemoteMousepointerClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
//import android.view.MotionEvent;

public class TiltController extends Activity {

    private static final String LOG_TAG = "TiltController";

    public static final String ADDRESS_EXTRA = "com.artcom.y60.trackpad.ADDRESS";
    public static final String PORT_EXTRA = "com.artcom.y60.trackpad.PORT";

    //private float mOldX = -1;
    //private float mOldY = -1;

    private InetAddress mAddress;
    private int mPort;

    private RemoteMousepointerClient mRemote = new RemoteMousepointerClient();

    private AccelerometerSensorListener mAccelListener;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.trackpad);
        mAccelListener = new AccelerometerSensorListener();
    }

    @Override
    protected void onPause() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(mAccelListener);
        mRemote.disconnectFromDisplay();
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(mAccelListener, SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_FASTEST);
        
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

    RemoteMousepointerClient getRemote() {

        return mRemote;
    }

    /*private void move(MotionEvent pEvent) {

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
    }*/

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

    class AccelerometerSensorListener implements SensorListener {

        @Override
        public void onAccuracyChanged(int pSensor, int pAccuracy) {
            // ignore these events for now
        }

        @Override
        public void onSensorChanged(int pSensor, float[] pValues) {

            float xAccel = pValues[SensorManager.DATA_X];
            float yAccel = pValues[SensorManager.DATA_Y];
            float zAccel = pValues[SensorManager.DATA_Z];      

            zAccel += SensorManager.STANDARD_GRAVITY;

            TiltController.this.move(xAccel, (-1) * yAccel);
        }
    }

}
