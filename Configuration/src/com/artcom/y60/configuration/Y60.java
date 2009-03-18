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

package com.artcom.y60.configuration;

import com.artcom.y60.configuration.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class Y60 extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	launchEntryPoint();
    	super.onCreate(savedInstanceState);
        
    	// hide status bar
        Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.main);
    }
    
    public boolean onTouchEvent(MotionEvent event){
    	
		return true;
    }
    
    private void launchEntryPoint(){
    	/*
    	Intent intent = new Intent();
    	ComponentName component = new ComponentName(
				"com.artcom.y60.homescreen",
				"com.artcom.y60.homescreen.HomeScreen");
		intent.setComponent(component);
		startActivity(intent);
		//finish();
		    
	    */
    }
    
    
}