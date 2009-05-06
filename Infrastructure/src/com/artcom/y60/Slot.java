package com.artcom.y60;

import android.content.Context;
import android.view.View;
import android.view.View.OnTouchListener;


public class Slot {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = Slot.class.getName();

    private static final int PADDING_X = 15;

    private static final int PADDING_Y = 100;
    
    
    
    // Instance Variables ------------------------------------------------

    private SlotHolder mHolder;
    
    private SlotLauncher mLauncher;
    
    private SlotViewer mViewer;

    private String mName;

    
    
    // Constructors ------------------------------------------------------

    public Slot(String pName, SlotLauncher pLauncher, SlotViewer pView, SlotHolder pHolder) {
        
        mName = pName;
        
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
        buf.append(mName);
        buf.append("[");
        buf.append(mLauncher.toString());
        buf.append("::");
        buf.append(mViewer.toString());
        buf.append("]");
        
        return buf.toString();
    }
    
    
    public String getName() {
        
        return mName;
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
    
    
    public boolean isOnFocus(View pThumbView) {
        
        int x= pThumbView.getLeft() + pThumbView.getWidth()/2; //mid of thumb
        int y= (pThumbView.getTop() + PADDING_Y); 
    
        View view = getViewer().view();
        if( view.getLeft()+PADDING_X < x && 
            view.getRight()-PADDING_X > x &&  
            view.getBottom() > y){
    
            //Logger.v(LOG_TAG, "i am on item: ", toString());
            return true;
        }
        return false;
    
    }

    
    public void handleDragging(View pDraggedView) {
    
        if(isOnFocus(pDraggedView)){               
            mLauncher.focus();
        }else{
            mLauncher.unfocus();
        }        
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
