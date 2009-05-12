/**
 * 
 */
package com.artcom.y60;

import android.view.View;
import android.view.View.OnClickListener;


class SlotLaunchingClickListener implements OnClickListener {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "SlotLaunchingClickListener";
    
    

    // Instance Variables ------------------------------------------------

	private SlotLauncher mLauncher;
	private SlotHolder mHolder;
	
	
	
	// Constructors ------------------------------------------------------

	public SlotLaunchingClickListener(SlotLauncher pLauncher, SlotHolder pHolder) {
		
	    mLauncher = pLauncher;
	    mHolder = pHolder;
	}
	
	
	
	// Public Instance Methods -------------------------------------------
	
	public void onClick(View pView) {
	    
	    Logger.d(LOG_TAG, "slot clicked!");
	    mLauncher.launch();
	}
	
	
}