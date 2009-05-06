package com.artcom.y60;

import android.content.Context;
import android.os.Vibrator;

public class VibratingSlotLauncher extends DecoratingSlotLauncher {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = VibratingSlotLauncher.class.getName();

    private static final long DROPPING_VIBRATION = 100;
    
    private static final long ON_TARGET_VIBRATION = 50;

    
    
    // Instance Variables ------------------------------------------------

    private boolean mIsInFocus;
    

    
    // Constructors ------------------------------------------------------

    public VibratingSlotLauncher() {
        super();
        // TODO Auto-generated constructor stub
    }

    public VibratingSlotLauncher(SlotLauncher pTarget) {
        super(pTarget);
        // TODO Auto-generated constructor stub
    }

    
    // Public Instance Methods -------------------------------------------

    @Override
    public void launchThis() {
        
        Logger.v(LOG_TAG, "dropped, virbrator should start!");
        Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(DROPPING_VIBRATION);
    }

    public void focusThis() {
        
        if(!mIsInFocus){
            mIsInFocus = true;
            Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(ON_TARGET_VIBRATION);
        }
    }

    public void unfocusThis() {
        
        if(mIsInFocus){
            mIsInFocus = false;
            Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(ON_TARGET_VIBRATION);
        }
    }

}
