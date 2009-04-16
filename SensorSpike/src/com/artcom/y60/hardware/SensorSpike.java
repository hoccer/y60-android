package com.artcom.y60.hardware;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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
    	
	public enum Directions { UP, DOWN, LEFT, RIGHT, UNDEFINED };

	private Button mCalibrateButton;
	private float mCalibratedX = 0;
	private float mCalibratedY = 0;
	public float mCalibratedZ;
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
        
        sensorManager.registerListener(accelListener, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(orientationListener, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_UI);        
        sensorManager.registerListener(compassListener, SensorManager.SENSOR_MAGNETIC_FIELD, SensorManager.SENSOR_DELAY_UI);
        
        setContentView(mLayout);
    }
    
    private void calibrate() {
    	
    	mCalibratedX = 0;
    	mCalibratedZ = 0;
    	mCalibrationStarted = new Date(System.currentTimeMillis());
    	mCalibrating = true;	
    }
    
    class AccelerometerSensorListener implements SensorListener {

    	private Date mLastStarted;
    	private Map<Directions,Integer> mDirectionCounters;
    	
    	private static final double SIGNIFICANCE_THRESHOLD = 3;
		private static final long ACCUMULATION_PERIOD = 500;

		private float mPrevXAccel = 0;
		private float mPrevZAccel = 0;
		private Directions mDominantDirection;
		
		public AccelerometerSensorListener() {
			mLastStarted = new Date(System.currentTimeMillis());
			mDirectionCounters = new HashMap<Directions,Integer>();
			mDirectionCounters.put(Directions.UP, 0);
			mDirectionCounters.put(Directions.DOWN, 0);
			mDirectionCounters.put(Directions.LEFT, 0);
			mDirectionCounters.put(Directions.RIGHT, 0);
			mDirectionCounters.put(Directions.UNDEFINED, 0);
			mDominantDirection = Directions.UNDEFINED;
		}

		@Override
		public void onAccuracyChanged(int sensor, int accuracy) {
//			Logger.v(LOG_TAG, "Accuracy of accelerometer changed");		
		}

		@Override
		public void onSensorChanged(int sensor, float[] values) {
		
			float xAccel = values[SensorManager.DATA_X];
			float yAccel = values[SensorManager.DATA_Y];
			float zAccel = values[SensorManager.DATA_Z];
			float rawXAxis = values[SensorManager.RAW_DATA_X];
			float rawYAxis = values[SensorManager.RAW_DATA_Y];
			float rawZAxis = values[SensorManager.RAW_DATA_Z];			

			zAccel += SensorManager.STANDARD_GRAVITY;
		
//			Logger.v(LOG_TAG, "Got reading from accelerometer: data = (" +
//					xAccel + "," + yAccel + "," + zAccel + "), raw = " +
//					rawXAxis + "," + rawYAxis + "," + rawZAxis + ")");	

			float diffX = ((xAccel) - (mPrevXAccel));
			float diffZ = ((zAccel) - (mPrevZAccel));
			
//			Logger.d(LOG_TAG, xAccel + "," + zAccel + "," + mPrevXAccel + "," + mPrevZAccel);
//			Logger.v(LOG_TAG, "\t\t\t\tdiffX = " + diffX + ", diffZ = " + diffZ);
			
			mPrevXAccel = xAccel;
			mPrevZAccel = zAccel;

			Date now = new Date(System.currentTimeMillis());
			
			if (now.getTime() - mLastStarted.getTime() > ACCUMULATION_PERIOD) {
				
				switch (mDominantDirection) {
				case UP:
					Logger.d(LOG_TAG, "Consensus movement: UP");
					break;
				case DOWN:
					Logger.d(LOG_TAG, "Consensus movement: DOWN");
					break;
				case LEFT:
					Logger.d(LOG_TAG, "Consensus movement: LEFT");
					break;
				case RIGHT:
					Logger.d(LOG_TAG, "Consensus movement: RIGHT");
					break;
				}
				
				mDirectionCounters.put(Directions.UP, 0);
				mDirectionCounters.put(Directions.DOWN, 0);
				mDirectionCounters.put(Directions.LEFT, 0);
				mDirectionCounters.put(Directions.RIGHT, 0);
				
				mDominantDirection = Directions.UNDEFINED;
				
				mLastStarted.setTime(now.getTime());
			}
			
			if (isSignificant(diffX)) {
//				Logger.d(LOG_TAG, "horizontal movement detected");
			} else if (isSignificant(diffZ)) {
//				Logger.d(LOG_TAG, "vertical movement detected");
			} else {
//				Logger.d(LOG_TAG, "no movement detected");
				return;
			}
			
			float max = Math.max(Math.abs(diffX), Math.abs(diffZ));
			
			if (max == Math.abs(diffX)) {
				if (diffX < 0) {
					Logger.d(LOG_TAG, "-->");
					int updatedValue = mDirectionCounters.get(Directions.RIGHT) + 1;
					mDirectionCounters.put(Directions.RIGHT, updatedValue);
					if (updatedValue >= mDirectionCounters.get(mDominantDirection)) {
						mDominantDirection = Directions.RIGHT;
					}
				} else {
					Logger.d(LOG_TAG, "<--");					
					int updatedValue = mDirectionCounters.get(Directions.LEFT) + 1;
					mDirectionCounters.put(Directions.LEFT, updatedValue);
					if (updatedValue >= mDirectionCounters.get(mDominantDirection)) {
						mDominantDirection = Directions.LEFT;
					}
				}
			} else { // max == Math.abs(diffZ)
				if (diffZ < 0) {
					Logger.d(LOG_TAG, "v");
					int updatedValue = mDirectionCounters.get(Directions.DOWN) + 1;
					mDirectionCounters.put(Directions.DOWN, updatedValue);
					if (updatedValue >= mDirectionCounters.get(mDominantDirection)) {
						mDominantDirection = Directions.DOWN;
					}

				}
				else {
					Logger.d(LOG_TAG, "^");
					int updatedValue = mDirectionCounters.get(Directions.UP) + 1;
					mDirectionCounters.put(Directions.UP, updatedValue);
					if (updatedValue >= mDirectionCounters.get(mDominantDirection)) {
						mDominantDirection = Directions.UP;
					}
				}
			}
			

		}

		private boolean isSignificant(float value) {
			if (Math.abs(value) > SIGNIFICANCE_THRESHOLD) {
				return true;
			}
			return false;
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
