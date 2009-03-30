package com.artcom.y60.infrastructure;

import java.io.Serializable;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class ErrorPresentationActivity extends Activity {

	private TableLayout mTableLayout;
	private TextView mTextView;
	private Button mOkButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTextView = new TextView( this );
		mTableLayout = new TableLayout( this);
		mOkButton = new Button( this );
		
		mOkButton.setText( "OK" );
		
		mTableLayout.addView( mTextView );
		mTableLayout.addView( mOkButton );
		
		setContentView( mTableLayout );
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Intent intent = getIntent();
		Throwable exception = (Throwable)intent.getSerializableExtra( ErrorHandling.ERROR );
		
		mTextView.setText( exception.getMessage() );
	}
	
}
