package com.artcom.y60.synergy;

import java.lang.Math;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

import com.artcom.y60.Logger;

public class GameControl extends Activity implements SensorEventListener{

    private static final String     LOG_TAG          = "GameControl";

	private SensorManager           mSensorManager;
    private Sensor                  mSensor;
    private Button                  mButton1;
    private Button                  mButton2;
    
    private SynergyServer           synergyServer = new SynergyServer();


    private float                   mAccelerationX = 0;
    private float                   mAccelerationY = 0;
    private float                   mAccelerationZ = 0;


    private double                   mouseCenteredPosX = 0;
    private double                   mouseCenteredPosY = 0;

    private double                   mouseCenteredPosXOld = 0;
    private double                   mouseCenteredPosYOld = 0;
    

    public void onSensorChanged(SensorEvent event) {
        if ( (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) && (event.values.length == 3)) {

            mAccelerationX = event.values[1];
            mAccelerationY = event.values[0];

            //mAccelerationZ = event.values[2];

            //Logger.v(LOG_TAG, ">> ", Math.round(event.values[0]) , " ",Math.round(event.values[1]),
            //    " ",Math.round(event.values[2]) );

            double mouseMovementX = 0;
            double mouseMovementY = 0;

            mouseMovementX = mAccelerationX / 1.0;
            mouseCenteredPosX += mouseMovementX;
            if ( mouseCenteredPosX >= synergyServer.mClientScreenWidth ){
                mouseCenteredPosX = synergyServer.mClientScreenWidth - 1;
            }
            if ( mouseCenteredPosX >= 100 ){
                //mouseCenteredPosX = 100 - 1;
            }
            if ( mouseCenteredPosX < 0 ){
                mouseCenteredPosX = 0;
            }

            mouseMovementY = mAccelerationY / 1.0;
            mouseCenteredPosY += mouseMovementY;
            if ( mouseCenteredPosY >= synergyServer.mClientScreenHeight ){
                mouseCenteredPosY = synergyServer.mClientScreenHeight - 1;
            }
            if ( mouseCenteredPosY >= 100 ){
                //mouseCenteredPosY = 100 - 1;
            }
            if ( mouseCenteredPosY < 0 ){
                mouseCenteredPosY = 0;
            }


            if ((Math.abs(mouseMovementX)>1) || (Math.abs(mouseMovementY)>1) ) {
            //    synergyServer.relativeMousePosition((int) mouseMovementX,(int) mouseMovementY);
            }

            double mousePosXDelta = Math.abs( mouseCenteredPosX - mouseCenteredPosXOld);
            double mousePosYDelta = Math.abs( mouseCenteredPosY - mouseCenteredPosYOld);
            if ((mousePosXDelta>1) || (mousePosYDelta>1) ) {
                synergyServer.absoluteMousePosition((int) mouseCenteredPosX,(int) mouseCenteredPosY);
                mouseCenteredPosXOld = mouseCenteredPosX;
                mouseCenteredPosYOld = mouseCenteredPosY;
            }

        }
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.v(LOG_TAG, ">>> onCreate() ", this);
        setContentView(R.layout.main);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mButton1 = (Button) findViewById(R.id.button_1);
        mButton1.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Logger.v(LOG_TAG,"button 1 touched up");
                    synergyServer.mouseButtonLeftUp();
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Logger.v(LOG_TAG,"button 1 touched down");
                    synergyServer.mouseButtonLeftDown();
                }
                return false;
            }
        });

        mButton2 = (Button) findViewById(R.id.button_2);
        mButton2.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Logger.v(LOG_TAG,"button 2 touched up");
                    synergyServer.mouseButtonRightUp();
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Logger.v(LOG_TAG,"button 2 touched down");
                    synergyServer.mouseButtonRightDown();
                }
                return false;
            }
        });

        Logger.v(LOG_TAG, "<<< onCreate() ", this);
    }

    @Override
    protected void onStart() {
        Logger.v(LOG_TAG, ">>> onStart() ", this);
        super.onStart();
        synergyServer.start();
        Logger.v(LOG_TAG, "<<< onStart() ", this);
    }

    @Override
    protected void onDestroy() {
        Logger.v(LOG_TAG, ">>> onDestroy() ", this);
        super.onDestroy();
        synergyServer.stop();
        Logger.v(LOG_TAG, "<<< onDestroy() ", this);
    }


    @Override
    protected void onResume() {
        Logger.v(LOG_TAG, ">>> onResume() ", this);
        super.onResume();
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
        Logger.v(LOG_TAG, "<<< onResume() ", this);
    }
    
    @Override
    public void onPause() {
        Logger.v(LOG_TAG, ">>> onPause() ", this);
        super.onPause();
        mSensorManager.unregisterListener(this);
        Logger.v(LOG_TAG, "<<< onPause() ", this);
    }





    @Override
    protected void onStop() {
        Logger.v(LOG_TAG, ">>> onStop() ", this);
        super.onStop();
        Logger.v(LOG_TAG, "<<< onStop() ", this);
    }


    @Override
    protected void onRestart() {
        Logger.v(LOG_TAG, ">>> onRestart() ", this);
        super.onRestart();
        Logger.v(LOG_TAG, "<<< onRestart() ", this);
    }

    @Override
    protected void onPostResume() {
        Logger.v(LOG_TAG, ">>> onPostResume() ", this);
        super.onPostResume();
        Logger.v(LOG_TAG, "<<< onPostResume() ", this);
    }

    @Override
    protected void onNewIntent(Intent pIntent) {
        Logger.v(LOG_TAG, ">>> onNewIntent() ", this);
        super.onNewIntent(pIntent);
        Logger.v(LOG_TAG, "<<< onNewIntent() ", this);
    }

}
