package com.artcom.y60;

import android.content.Context;

public abstract class SlotLauncher {

    // Instance Variables ------------------------------------------------

    private Slot mSlot;
    
    
    
    // Public Instance Methods -------------------------------------------

    public abstract void launch();
    
    
    
    // Protected Instance Methods ----------------------------------------

    protected Slot getSlot() {
        
        return mSlot;
    }
    
    
    protected Context getContext() {
        
        return mSlot.getContext();
    }
    
    
    protected void invalidate() {
        
        getSlot().invalidate();
    }
    
    
    
    // Package Protected Instance Methods --------------------------------

    void setSlot(Slot pSlot) {
        
        mSlot = pSlot;
    }
}
