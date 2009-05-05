package com.artcom.y60;


import android.view.View;

public class StaticSlotViewer extends SlotViewer {
    
    // Instance Variables ------------------------------------------------

    private View mView;
    
    
    
    // Constructors ------------------------------------------------------

    public StaticSlotViewer(View pView) {
        
        mView = pView;
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    @Override
    public View view() {
        
        return mView;
    }

}
