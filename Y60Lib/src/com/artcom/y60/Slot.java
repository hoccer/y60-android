package com.artcom.y60;

import android.content.Context;
import android.view.View;


/**
 * Represents a UI element the user can interact with in a simple fashion, such as
 * 
 * <ul>
 * <li>application icons displayed on the home screen</li>
 * <li>drop targets for sharing and shifting resources</li>
 * </ul>
 * 
 * The purpose the slot API to provide decoupling between client (application) code which defines
 * the interaction options for the user (e.g. to shift a video to TV) and code providing the needed
 * functionality (e.g. send a play command to the TV). Additionally, the slot API allows both
 * (client as well as provider) to abstract from details of UI primitives (clicking, tapping, etc.).
 * Decoupling is achieved by using a {@link SlotHolder}, which acts as a bridge between both sides:
 * a slot holder is initialized and passed to the client, which uses it to e.g. activate and
 * deactivate it for interaction; slots use the holder to update (invalidate) the application view. 
 * 
 * From a provider perspective, a slot is basically made up of two objects:
 * 
 * <ul>
 * <li>a {@link SlotLauncher} providing implementation for gaining/losing focusing and for
 *     <i>launching</i> this slot, i.e. clicking it or drag-and-dropping something on it, such as
 *     firing an Intent to launch an Activity or just making a simple API to somewhere.</li>
 * <li>a {@link SlotViewer} providing implementation for viewing this slot, i.e. retrieving a
 *     {@link android.view.View} on this slot</li>
 * </ul>
 * 
 * @see SlotHolder, SlotLauncher, SlotViewer, StaticSlotViewer, StatusToggler
 * @author arne
 *
 */
public class Slot {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = Slot.class.getName();

    private static final int PADDING_X = 15;

    private static final int PADDING_Y = 100;
    
    
    
    // Instance Variables ------------------------------------------------

    /** The holder this slot lies in */
    private SlotHolder mHolder;
    
    /**
     * Is used to launch whatever the slot is supposed to do, and to react to gaining/losing focus
     */
    private SlotLauncher mLauncher;
    
    /** Is used to render a view on this slot. */
    private SlotViewer mViewer;

    /** This slot's name */
    private String mName;

    private boolean mIsInFocus;
    
    
    // Constructors ------------------------------------------------------

    /**
     * Creates a new slot using the given holder and propagates the holder to the given launcher
     * and viewer.
     */
    public Slot(String pName, SlotLauncher pLauncher, SlotViewer pView) {
        
        mIsInFocus = false;
        mName = pName;
        
        mLauncher = pLauncher;
        mLauncher.setSlot(this);
        
        mViewer = pView;
        mViewer.setSlot(this);
    }
    
    
    
    // Public Instance Methods -------------------------------------------
    
    public synchronized SlotLauncher getLauncher() {
        
        return mLauncher;
    }
    
    
    public SlotViewer getViewer() {
        
        return mViewer;
    }
    
    
    public synchronized String toString() {
        
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
    
    
    /**
     * Activates this slot for interaction, i.e. launch is executed when the view is clicked.
     */
    public synchronized void activate() {
        
        Logger.d(LOG_TAG, "activating slot with launcher ", mLauncher, " and viewer ", mViewer);
        
        assertProperlyInitialized();
        
        View view = mViewer.view();
        view.setOnClickListener(new SlotLaunchingClickListener(mLauncher, mHolder));
    }
    
    
    /**
     * Deactivates this slot.
     */
    public synchronized void deactivate() {
        
        Logger.d(LOG_TAG, "deactivating slot with launcher ", mLauncher, " and viewer ", mViewer);
        
        assertProperlyInitialized();
        
        View view = mViewer.view();
        view.setOnClickListener(null);
    }
    
    
    public boolean isOnFocus(View pThumbView) {
        
        assertProperlyInitialized();
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

    
    public synchronized void handleDragging(View pDraggedView) {
    
        assertProperlyInitialized();
        if(isOnFocus(pDraggedView)) {
            
            if (!mIsInFocus) {
                
                mIsInFocus = true;
                mLauncher.focus();
            }
        } else {
            
            if (mIsInFocus) {
                
                mIsInFocus = false;
                mLauncher.unfocus();
            }
        }        
    }
    
    
    public void setHolder(SlotHolder pHolder) {
        
        mHolder = pHolder;
    }
    
    
    public synchronized void prepend(DecoratingSlotLauncher pDeco) {
        
        pDeco.setTarget(mLauncher);
        pDeco.setSlot(this);
        mLauncher = pDeco;
    }

    
    // Protected Instance Methods ----------------------------------------

    protected void invalidate() {

        assertProperlyInitialized();
        mHolder.invalidate();
    }
  
  
    protected SlotHolder getHolder() {

        assertProperlyInitialized();
        return mHolder;
    }
    
    
    protected Context getContext() {
        
        assertProperlyInitialized();
        return mHolder.getContext();
    }
    
    
    
    // Private Instance Methods ------------------------------------------

    private void assertProperlyInitialized() {
        
        if (mHolder == null) {
            
            throw new IllegalStateException("Holder for slot "+getName()+" was never set!");
        }
    }
}
