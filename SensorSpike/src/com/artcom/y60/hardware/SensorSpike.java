package com.artcom.y60.hardware;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.artcom.y60.Logger;

public class SensorSpike extends Activity {
    public static final String LOG_TAG = "SensorSpike";
    
    private static final float CALIBRATION_DURATION = 2000; // in ms 
    
	private Button mCalibrateButton;
	private float mCalibratedX = 0;
	private float mCalibratedY = 0;
	private boolean mCalibrating;
	private Date mCalibrationStarted;
	private Date mLastUpdated = null;
	
	private float mPrevXAccel = 0;
	private float mPrevYAccel = 0;
	private float mPrevXVel = 0;
	private float mPrevYVel = 0;
	private float mCurrentXAccel = 0;
	private float mCurrentYAccel = 0;
	
	private LinearLayout mLayout;

	public float mAccXDisplacement;

	public float mAccYDisplacement;

	public SensorSpike() {
		mLastUpdated = new Date(System.currentTimeMillis());
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLayout = new LinearLayout(this);
        mCalibrateButton = new Button(this);
        mCalibrateButton.setText("Calibrate now");
        mCalibrateButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(SensorSpike.this, "Calibrating sensors", Toast.LENGTH_SHORT).show();
				calibrate();
			}
       
        } );

        mLayout.addView(mCalibrateButton);
        
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        
        AccelerometerSensorListener accelListener = new AccelerometerSensorListener();
        OrientationSensorListener orientationListener = new OrientationSensorListener();
        CompassSensorListener compassListener = new CompassSensorListener();
        
        sensorManager.registerListener(accelListener, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(orientationListener, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_UI);        
        sensorManager.registerListener(compassListener, SensorManager.SENSOR_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_UI);
        
        setContentView(mLayout);
    }
    
    private void calibrate() {
    	
    	mCalibratedX = 0;
    	mCalibratedY = 0;
    	mCalibrationStarted = new Date(System.currentTimeMillis());
    	mCalibrating = true;	
    }
    
    class AccelerometerSensorListener implements SensorListener {


		@Override
		public void onAccuracyChanged(int sensor, int accuracy) {
			Logger.v(LOG_TAG, "Accuracy of accelerometer changed");		
		}

		@Override
		public void onSensorChanged(int sensor, float[] values) {
		
			float xAccel = values[SensorManager.DATA_X];
			float yAccel = values[SensorManager.DATA_Y];
			float zAxis = values[SensorManager.DATA_Z];
			float rawXAxis = values[SensorManager.RAW_DATA_X];
			float rawYAxis = values[SensorManager.RAW_DATA_Y];
			float rawZAxis = values[SensorManager.RAW_DATA_Z];
	
			// Smoothing?
			
			//xAccel = Math.round(xAccel - mCalibratedX);
			//yAccel = Math.round(yAccel - mCalibratedY);
			
			Logger.v(LOG_TAG, "Got reading from accelerometer: data = (" +
					xAccel + "," + yAccel + "," + zAxis + "), raw = " +
					rawXAxis + "," + rawYAxis + "," + rawZAxis + ")");			
			
			Date now = new Date(System.currentTimeMillis());
			long timeDelta = now.getTime() - mLastUpdated.getTime();
			mLastUpdated.setTime(now.getTime());
			
			if (timeDelta < 500) {
				mCurrentXAccel = (mCurrentXAccel + xAccel) / 2;
				mCurrentYAccel = (mCurrentYAccel + yAccel) / 2;
				return;
			}
			
			if (mCalibrating) {
				
				if (now.getTime() - mCalibrationStarted.getTime() <= CALIBRATION_DURATION) {
					mCalibratedX = (mCalibratedX + xAccel) / 2;
					mCalibratedY = (mCalibratedY + yAccel) / 2;
				} else {
					mCurrentXAccel = 0;
					mCurrentYAccel = 0;
					
					mPrevXAccel = mCalibratedX;
					mPrevYAccel = mCalibratedY;
					
					mPrevXVel = 0;
					mPrevYVel = 0;
					
					mAccXDisplacement = 0;
					mAccYDisplacement = 0;

					Logger.v(LOG_TAG, "after calibration - baseline x/y accel: (" +
							mPrevXAccel + "," + mPrevYAccel + "), baseline x/y vel: (" +
							mPrevXVel + "," + mPrevYVel + ")");

					mLastUpdated = new Date(System.currentTimeMillis());
					mCalibrating = false;
				} 
			} else {

				float deltaXVel = mPrevXAccel * ((float)timeDelta/1000);
				float deltaYVel = mPrevYAccel * ((float)timeDelta/1000);

				float xDisplacement = mPrevXVel * ((float)timeDelta/1000);
				float yDisplacement = mPrevYVel * ((float)timeDelta/1000);

				Logger.v(LOG_TAG, "prev accel x/y: (" + mPrevXAccel + "," + mPrevYAccel + ")");
				Logger.v(LOG_TAG, "delta vel x/y: (" + deltaXVel + "," + deltaYVel + ")");

				mPrevXVel += deltaXVel;// mPrevXVel = Math.round(mPrevXVel);
				mPrevYVel += deltaYVel;// mPrevYVel = Math.round(mPrevYVel);

//				mPrevXAccel = xAccel;
//				mPrevYAccel = yAccel;
				
				mPrevXAccel = mCurrentXAccel;
				mPrevYAccel = mCurrentYAccel;
				
				//xDisplacement = Math.round(xDisplacement);
				//yDisplacement = Math.round(yDisplacement);
				
				mAccXDisplacement += xDisplacement;
				mAccYDisplacement += yDisplacement;

				Logger.v(LOG_TAG, "x/y displacement: (" + xDisplacement + "," + yDisplacement + ")");
				Logger.v(LOG_TAG, "x/y pos: (" + mAccXDisplacement + "," + mAccYDisplacement + ")");
				
				mCurrentXAccel = 0;
				mCurrentYAccel = 0;
			}
		}
    }
    
    class OrientationSensorListener implements SensorListener {

		@Override
		public void onAccuracyChanged(int sensor, int accuracy) {
//			Logger.v(LOG_TAG, "Accuracy of orientation sensor changed");		
		}

		@Override
		public void onSensorChanged(int sensor, float[] values) {
		
//			Logger.v(LOG_TAG, "Got reading from orientation sensor");
		}
    }
    
    class CompassSensorListener implements SensorListener {

		@Override
		public void onAccuracyChanged(int sensor, int accuracy) {
//			Logger.v(LOG_TAG, "Accuracy of compass sensor changed");		
		}

		@Override
		public void onSensorChanged(int sensor, float[] values) {
		
//			Logger.v(LOG_TAG, "Got reading from compass sensor");
		}
    }
}