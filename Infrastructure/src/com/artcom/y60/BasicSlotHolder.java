package com.artcom.y60;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.View.OnTouchListener;

public class BasicSlotHolder implements SlotHolder {

    // Instance Variables ------------------------------------------------

    private Context mContext;
    
    private List<Slot> mSlots;
    
    
    
    // Constructors ------------------------------------------------------

    public BasicSlotHolder(Context pContext) {
        
        mContext = pContext;
        mSlots = new LinkedList<Slot>();
    }
    

    
    // Public Instance Methods -------------------------------------------

    @Override
    public void invalidate() {
        
        // nothing to do
    }
    
    @Override
    public Context getContext() {
        
        return mContext;
    }

    public void activateSlots(OnTouchListener pTouchListener) {
        
        for (Slot slot: mSlots) {
            
            slot.activate(pTouchListener);
        }
    }

    public void deactivateSlots() {
        
        for (Slot slot: mSlots) {
            
            slot.deactivate();
        }
    }

    public void addSlot(String pName, SlotLauncher pLauncher, SlotViewer pViewer) {
        
        Slot slot = new Slot(pName, pLauncher, pViewer, this);
        mSlots.add(slot);
    }

    public List<Slot> getSlots() {
        
        return new LinkedList<Slot>(mSlots);
    }
}
