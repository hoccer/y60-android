package com.artcom.y60;

import java.util.List;

import android.content.Context;
import android.view.View.OnTouchListener;

public interface SlotHolder {

    public void invalidate();
    
    public Context getContext();
    
    public void activateSlots(OnTouchListener pTouchListener);
    
    public void deactivateSlots();
    
    public List<Slot> getSlots();
}
