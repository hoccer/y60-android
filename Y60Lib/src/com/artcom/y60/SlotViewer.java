package com.artcom.y60;

import android.view.View;

public abstract class SlotViewer {

    // Instance Variables ------------------------------------------------

    private Slot mSlot;
    
    
    
    // Public Instance Methods -------------------------------------------

    public abstract View view();
    
    
    
    // Protected Instance Methods ----------------------------------------

    protected Slot getSlot() {
        
        return mSlot;
    }

    
    
    // Package Protected Instance Methods --------------------------------

    void setSlot(Slot pSlot) {
        
        mSlot = pSlot;
    }
}
