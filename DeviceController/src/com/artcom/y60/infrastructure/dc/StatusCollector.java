package com.artcom.y60.infrastructure.dc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StatusCollector extends BroadcastReceiver
{
	private static final String LOG_TAG = "StatusCollector";
	
	public enum ScreenState { UNKNOWN, ON, OFF };
	private ScreenState m_screenState;
	
	private static StatusCollector m_instance = null;
	
	private StatusCollector() {
		m_screenState = ScreenState.UNKNOWN;
	}
	
	@Override
	public void onReceive( Context context, Intent intent ) {
		Log.v( LOG_TAG, "Received broadcast" );
		
		String action = intent.getAction();
		Log.v( LOG_TAG, "Received action " + action );

		if (action.equals( "SCREEN_ON" )) {
			m_screenState = ScreenState.ON;
		} else if (action.equals( "SCREEN_OFF" )) {
			m_screenState = ScreenState.OFF;
		}
	}

	public ScreenState getScreenState() {
		return m_screenState;
	}
	
	public static StatusCollector getInstance() {
		
		if (m_instance == null) {
			m_instance = new StatusCollector();
		}
		
		return m_instance;
	}
}
