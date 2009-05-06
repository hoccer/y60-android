package com.artcom.y60;

import android.content.Context;

public abstract class SlotLauncher {

    // Instance Variables ------------------------------------------------

    private Slot mSlot;
    
    
    
    // Public Instance Methods -------------------------------------------

    /** Override if you're interested */
    public void launch() {
    }
    
    /** Override if you're interested */
    public void focus() {
    }
    
    /** Override if you're interested */
    public void unfocus() {
    }
    
    
    
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
