/*
 *  Copyright (C) 1993-2008, ART+COM AG Berlin, Germany <www.artcom.de>
 * 
 *  These coded instructions, statements, and computer programs contain
 *  proprietary information of ART+COM AG Berlin, and are copy protected
 *  by law. They may be used, modified and redistributed under the terms
 *  of GNU General Public License referenced below. 
 *     
 *  Alternative licensing without the obligations of the GPL is
 *  available upon request.
 * 
 *  GPL v3 Licensing:
 * 
 *  This file is part of the ART+COM Y60 Platform.
 * 
 *  ART+COM Y60 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ART+COM Y60 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ART+COM Y60.  If not, see <http: * www.gnu.org/licenses/>.
 */

package com.artcom.y60;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

public class Y60 extends Activity {
	
	private static final String LOG_TAG= "Y60 Stargate 108";
	
	
    private TableLayout mTableLayout;
	private EditText mEditText;
	private Button mSetNameButton;
	private Button mStartY60Button;
	private Button mStopY60Button;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	mTableLayout = new TableLayout(this);
    	mEditText = new EditText(this);    	
    	mSetNameButton = new Button(this);
    	mStartY60Button = new Button(this);
    	mStopY60Button = new Button(this);  
    	
    	mSetNameButton.setText("Set device name");
    	mStartY60Button.setText(R.string.start_Y60_label);
    	mStopY60Button.setText("Stop Y60");
    	
    	mTableLayout.addView(mEditText);
    	mTableLayout.addView(mSetNameButton);
    	mTableLayout.addView(mStartY60Button);
    	mTableLayout.addView(mStopY60Button);
    	
    	mSetNameButton.setOnClickListener(new RenameClickListener());
    	mStartY60Button.setOnClickListener(new OnClickListener(){
    		public void onClick(View view){
    			//hide status bar
    			Window win = getWindow();
    			win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
    					WindowManager.LayoutParams.FLAG_FULLSCREEN);
    			launchEntryPoint();
    		} 
    	});
    	
    	setContentView(mTableLayout);
    	

	
    }
    
    public void onResume() {
    	super.onResume();
    }
    
    public boolean onTouchEvent(MotionEvent event){
    	
		return true;
    }
    
    private void launchEntryPoint(){
    	
    	Intent intent = new Intent("android.intent.category.HOME");
//    	ComponentName component = new ComponentName(
//				"com.artcom.tgallery.homescreen",
//				"com.artcom.tgallery.homescreen.HomeScreen");
//		intent.setComponent(component);
		startActivity(intent);
	
    }
    
    class RenameClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			String configFile= getResources().getString(R.string.configFile);
			JSONObject configuration = null;
			try {

				FileReader fr = new FileReader(configFile);
				char[] inputBuffer = new char[255];
				fr.read(inputBuffer);
				configuration = new JSONObject(new String(inputBuffer));
				fr.close();
				
				FileWriter fw = new FileWriter(configFile);
				configuration.put("device-path", "devices/mobile/" + mEditText.getText().toString());
				fw.write(configuration.toString());
				fw.close();
				
				Toast.makeText(Y60.this, "Device name changed to " + mEditText.getText().toString(), 
						Toast.LENGTH_SHORT).show();

			} catch (FileNotFoundException e) {
				Logger.e( LOG_TAG, "Could not find configuration file ", configFile );
				throw new RuntimeException(e);
			} catch (UnsupportedEncodingException e) {
				Logger.e( LOG_TAG, "Configuration file ", configFile, " uses unsupported encoding" );
				throw new RuntimeException(e);
			} catch (IOException e) {
				Logger.e( LOG_TAG, "Error while reading configuration file ", configFile );
				throw new RuntimeException(e);
			} catch (JSONException e) {
				Logger.e( LOG_TAG, "Error while parsing configuration file ", configFile );
				throw new RuntimeException(e);
			}

		}

    	
    }
    
}