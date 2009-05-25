package com.artcom.y60;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;


public class ErrorPresentationActivity extends Activity {

	private TableLayout mTableLayout;
	private TextView mTextView;
	private Button mOkButton;
	
	enum Mode { DEBUG, PRODUCTION }
	
	// TODO this will eventually need to be configurable, possibly via preferences
	private static final Mode mMode = Mode.DEBUG;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTextView = new TextView(this);
		mTableLayout = new TableLayout(this);
		mOkButton = new Button(this);
		
		mOkButton.setText("OK");
		mOkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
		
		mTableLayout.addView( mTextView );
		mTableLayout.addView( mOkButton );
		
		setContentView( mTableLayout );
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Intent intent = getIntent();
		if (mMode == Mode.DEBUG) {

			String logTag = "[unknown source]";
			if (intent.hasExtra(ErrorHandling.ID_LOGTAG)) {
				logTag = intent.getStringExtra(ErrorHandling.ID_LOGTAG);
			}
			
			String message = "(unspecified error)";
			if (intent.hasExtra(ErrorHandling.ID_MESSAGE)) {
			    message = intent.getStringExtra(ErrorHandling.ID_MESSAGE);
				
				// Sort by class here later, for now, we just log everything in verbose mode
				Logger.e(logTag, message);
			}
			
			mTextView.setText("Oops! " + logTag + " says: " + message);
			
		} else if (mMode == Mode.PRODUCTION) {
			
			mTextView.setText(R.string.err_user_general);
		}
	}
	
}
