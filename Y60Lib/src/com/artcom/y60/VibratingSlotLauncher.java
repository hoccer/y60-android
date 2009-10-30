package com.artcom.y60;

import android.content.Context;
import android.os.Vibrator;

public class VibratingSlotLauncher extends DecoratingSlotLauncher {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG             = VibratingSlotLauncher.class.getSimpleName();

    private static final long DROPPING_VIBRATION = 100;
    
    private static final long ON_TARGET_VIBRATION = 50;

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
        
        Logger.v(LOG_TAG, "launch virbrating slot launcher");
        Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(DROPPING_VIBRATION);
    }

    @Override
    public void focusThis() {
        
        Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(ON_TARGET_VIBRATION);
    }

    @Override
    public void unfocusThis() {
        
        Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(ON_TARGET_VIBRATION);
    }

}
