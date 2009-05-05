package com.artcom.y60;

import android.content.Context;
import android.view.View;
import android.view.View.OnTouchListener;


public class Slot {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "Slot";
    
    
    
    // Instance Variables ------------------------------------------------

    private SlotHolder mHolder;
    
    private SlotLauncher mLauncher;
    
    private SlotViewer mViewer;
    
    
    
    // Constructors ------------------------------------------------------

    public Slot(SlotLauncher pLauncher, SlotViewer pView, SlotHolder pHolder) {
        
        mLauncher = pLauncher;
        mLauncher.setSlot(this);
        
        mViewer = pView;
        mViewer.setSlot(this);
        
        mHolder = pHolder;
    }
    
    
    
    // Public Instance Methods -------------------------------------------
    
    public SlotLauncher getLauncher() {
        
        return mLauncher;
    }
    
    
    public SlotViewer getViewer() {
        
        return mViewer;
    }
    
    
    public String toString() {
        
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        buf.append(mLauncher.toString());
        buf.append("::");
        buf.append(mViewer.toString());
        buf.append("]");
        
        return buf.toString();
    }
    
    
    public void activate(OnTouchListener pTouchListener) {
        
        Logger.d(LOG_TAG, "activating slot with launcher ", mLauncher, " and viewer ", mViewer);
        
        View view = mViewer.view();
        view.setOnClickListener(new SlotLaunchingClickListener(mLauncher, mHolder));
        view.setOnTouchListener(pTouchListener);
    }
    
    
    public void deactivate() {
        
        Logger.d(LOG_TAG, "deactivating slot with launcher ", mLauncher, " and viewer ", mViewer);
        
        View view = mViewer.view();
        view.setOnClickListener(null);
        view.setOnTouchListener(null);
    }
    
    
    
    // Protected Instance Methods ----------------------------------------

    protected void invalidate() {

        mHolder.invalidate();
    }
  
  
    protected SlotHolder getHolder() {

        return mHolder;
    }
    
    
    protected Context getContext() {
        
        return mHolder.getContext();
    }
}
