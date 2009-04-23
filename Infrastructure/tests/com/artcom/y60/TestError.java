package com.artcom.y60;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.ErrorPresentationActivity;
import com.artcom.y60.ErrorHandling.Category;

import android.app.Instrumentation.ActivityMonitor;
import android.content.IntentFilter;
import android.test.ActivityUnitTestCase;

public class TestError extends ActivityUnitTestCase<ErrorPresentationActivity> {
	
	public TestError() {
		super(ErrorPresentationActivity.class);
		
	}

	public void testUserError()
	{
		IntentFilter filter = new IntentFilter("y60.intent.ERROR_PRESENTATION");
		ActivityMonitor monitor = new ActivityMonitor( filter, null, true );
		Exception e = new Exception( "Don't panic" );
		ErrorHandling.signalError( this.getClass().getSimpleName(),
				e, getActivity(), ErrorHandling.Category.UNSPECIFIED );
		
	}
	
}
