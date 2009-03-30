package com.artcom.y60.infrastructure;

import android.content.Context;
import android.content.Intent;

public class ErrorHandling {

	public static final String ERROR = "...";
	
	public static void signal_user_error( Context context, Throwable error )
	{
		Intent intent = new Intent( context, ErrorPresentationActivity.class );
		intent.putExtra( ERROR, error);
		context.startActivity( intent );
	}
	
	public static void signal_dev_error()
	{
		
	}
	
	public static void signal_admin_error()
	{
		
	}
}
