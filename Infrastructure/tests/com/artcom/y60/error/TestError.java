package com.artcom.y60.error;

import android.app.Instrumentation.ActivityMonitor;
import android.app.Instrumentation.ActivityResult;
import android.content.IntentFilter;
import android.test.ActivityUnitTestCase;

public class TestError extends ActivityUnitTestCase<ErrorPresentationActivity> {

	public TestError() {
		super(ErrorPresentationActivity.class);
		
	}

	public void testUserError()
	{
		IntentFilter filter = new IntentFilter("y60.intent.ERROR_PRESENTATION");
		
		ActivityMonitor monitor = new ActivityMonitor( filter, null, false );
		Exception e = new Exception( "Don't panic" );
		ErrorHandling.signal_user_error( getActivity(), e );
		
	}
	
}
