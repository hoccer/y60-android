package com.artcom.y60;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;

/**
 * Helper for adding drag-and-drop functionality to views.
 */
public class DragAndDropHelper implements OnTouchListener {
    
    enum State { NOT_DRAGGING, IN_ANIMATION, DRAGGING };
    
    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "DragAndDropHelper";
    
    private static final float SCALE_FACTOR = 0.4f;
    
    private static final int ANIMATION_DURATION = 200;

    public static final int VERTICAL_OFFSET = 20;
    
    // Instance Variables ------------------------------------------------
    
    /** The Layout in which the dragging takes place */
    private AbsoluteLayout mAbsoluteLayout;
    
    private ViewGroup mActivityViewGroup;
    
    /** The View to be dragged around */
    private View mSourceView;
    
    /** The foreground activity that displayed the draggable view */
    private Activity mActivity;
    
    /** A thumbnailed screenshot of the draggable view */
    private View mThumbView;
    
    /** Used for detecting the long press */
    private GestureDetector mGest;
    
    /**
     * For clients which are interested in touch events, e.g. so that it's still possible to
     * fling a picture which is also draggable.
     */
    private OnTouchListener mDelegateTouchListener;
    
    private List<DragListener> mDragListenerList;
    
    private View mDefaultThumbnail;
      
    private DropTargetCollection mDropTargetCollection;
    
    private boolean mIsDropTargetEnabled;
    
    private State mState;
    
    // Static Methods ----------------------------------------------------
    
    /**
     * Makes the given draggable (activated by a long press). Uses the given default
     * as view for dragging.
     */
    public static DragAndDropHelper enableDragAndDrop(View pView, AbsoluteLayout pLayout, ViewGroup pActivityViewGroup, Activity pActivity, View pDefaultThumbnail){
        
        DragAndDropHelper helper = new DragAndDropHelper(pView, pLayout, pActivityViewGroup, pActivity, pDefaultThumbnail);
        return helper;
    }
    
    /**
     * Makes the given draggable (activated by a long press). Uses a screenshot of the draggable
     * view (drawn to a bitmap off-screen) for dragging.
     */
    public static DragAndDropHelper enableDragAndDrop(View pView, AbsoluteLayout pLayout, ViewGroup pActivityViewGroup, Activity pActivity){
        
        return enableDragAndDrop(pView, pLayout, pActivityViewGroup, pActivity, null);
    }
    
    
    // Constructors ------------------------------------------------------

    public DragAndDropHelper(View pView, AbsoluteLayout pLayout, ViewGroup pActivityViewGroup, Activity pActivity, View pDefaultThumbnail){
    
        mState = State.NOT_DRAGGING;
        mActivityViewGroup = pActivityViewGroup;
        
        mAbsoluteLayout = pLayout;
        mAbsoluteLayout.setOnTouchListener(this); //override old listener, we take responsibility
        mAbsoluteLayout.setLongClickable(true);
        
        mSourceView = pView;
        mSourceView.setOnTouchListener(this); //override old listener, we take responsibility
        mSourceView.setLongClickable(true);
        
        mDefaultThumbnail = pDefaultThumbnail;
                
        mActivity = pActivity;
     
        mIsDropTargetEnabled = false;
        mDropTargetCollection = new DropTargetCollection(mActivity, mAbsoluteLayout);
        
        mDragListenerList= new LinkedList<DragListener>();
        
        mGest = new GestureDetector(new ShareGestureListener());
    }
    
    // Public Instance Methods -------------------------------------------

    /**
     * We take complete responsibility for handling incoming touch events.
     * Since the return value is always true, the touch events are not delegated to a higher level view.
     * Return true ensures that the next touch event is always processed by a first-level pTouchedView  
     * The DragAndDropHelper cares for dragging and long press 
     */
    @Override
    public boolean onTouch(View pTouchedView, MotionEvent pEvent) {
    
        Logger.d(LOG_TAG, "\nnext onTouch ", pTouchedView, " ", pEvent.getAction(), " ", pEvent.getX(), " ", pEvent.getY());
        
        int action = pEvent.getAction(); 
        if (mState == State.DRAGGING) {
         
            switch (action) {
                case (MotionEvent.ACTION_UP):
                    endDragging(pEvent);
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    drag(pEvent);
                    return true;
            }
            
        } else if (mState == State.IN_ANIMATION) {
            
            if (action == MotionEvent.ACTION_UP) {
                
                mState = State.NOT_DRAGGING;
                return true;
            }
        }
        
        //check for long press, return true if detected
        //better: register for long press... always returns false
        if (pTouchedView == mSourceView/* || isCurrentlyDragging()*/) {
            if (mGest.onTouchEvent(pEvent)) {
                Logger.d(LOG_TAG, "DnD gesture: true");
                return true;
            }else{
                Logger.d(LOG_TAG, "DnD gesture: false");
            }
            
        }
        
        if (mDelegateTouchListener != null) {
            
            mDelegateTouchListener.onTouch(pTouchedView, pEvent);
            Logger.d(LOG_TAG, "delegate touch event to mDelegateTouchListener");
        }
            return true;
            
    }
    
    public void setDelegateOnTouchListener(OnTouchListener pListener) {
        
        mDelegateTouchListener = pListener;
    }
    
    public void addDragListener(DragListener pListener) {
        
        mDragListenerList.add(pListener);
        Logger.v(LOG_TAG, "number of drag listerners: ", mDragListenerList.size());
    }

    public void addDropTarget(String pName, SlotLauncher pLauncher, SlotViewer pViewer){
        
        if (!mIsDropTargetEnabled) {
            
            addDragListener(mDropTargetCollection);
            mIsDropTargetEnabled= true;
        }

        pViewer.view().setOnTouchListener(this);
        mDropTargetCollection.addSlot(pName, pLauncher, pViewer);
        mDropTargetCollection.invalidate();
    }
    
    // Private Instance Methods ------------------------------------------
    
    private void drag(MotionEvent pEvent) {
        
        LayoutParams position = positionForDragging(pEvent);
        mThumbView.setLayoutParams(position);
        
        if (mDragListenerList.size() > 0){
            Iterator<DragListener> it = mDragListenerList.iterator();
            while (it.hasNext()){
                it.next().onDragged(mSourceView, mThumbView, mAbsoluteLayout, position.x, position.y);
            }
        }       
    }

    private void endDragging(MotionEvent pEvent) {
                
        mThumbView.setVisibility(View.GONE);
        mAbsoluteLayout.removeView(mThumbView);   
        
        if(mIsDropTargetEnabled){
        
            Slot target= mDropTargetCollection.getfocusedDropTarget(mThumbView);
            if(target != null){
                target.getLauncher().launch();
            }
            mAbsoluteLayout.removeView(mDropTargetCollection.getDropTargetLayout());            
        }
        
        mActivityViewGroup.setVisibility(View.VISIBLE);
        mAbsoluteLayout.invalidate();
        
        if (mDragListenerList.size() > 0){
            Iterator<DragListener> it = mDragListenerList.iterator();
            while (it.hasNext()){
                LayoutParams position = positionForDragging(pEvent);
                it.next().onDraggingEnded(mSourceView, mThumbView, position.x, position.y);
            }
        }
 
        mThumbView = null; // let the gc take care of it
        
        mState = State.NOT_DRAGGING;
        
        System.gc(); // would be nice...
    }

    
    //return top left + vertical offsetted for positioning the view
    private LayoutParams positionForDragging(MotionEvent pEvent) {
        
        int x = (int)pEvent.getX() - mThumbView.getWidth()/2;
        int y = (int)pEvent.getY() - mThumbView.getHeight()/2 - VERTICAL_OFFSET;
        
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT,
                                x,
                                y);
    }
    
    private boolean isCurrentlyDragging() {
        return mThumbView != null && mThumbView.getVisibility() == View.VISIBLE;
    }
    
    // Inner Classes -----------------------------------------------------
    
    class ShareGestureListener implements OnGestureListener {

        @Override
        public void onLongPress(MotionEvent pE) {
            
            Logger.v(LOG_TAG, "LOOOOOOOOOOOOOOOOOOOOOOOONG PRESS");

            if (mThumbView == null) {
                mThumbView = mDefaultThumbnail;
                
                //if default View IS null
                if (mThumbView == null) {
                    
                    mThumbView = GraphicsHelper.scaleView(mSourceView, SCALE_FACTOR, mActivity);
                }
                mThumbView.setVisibility(View.INVISIBLE);
                mThumbView.setOnTouchListener(DragAndDropHelper.this);
                mAbsoluteLayout.addView(mThumbView);
            }
            
            int x = (int)pE.getX();//-mDrawable.getMinimumWidth()/2;
            int y = (int)pE.getY()-10;//-mDrawable.getMinimumHeight()*2;
            
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    x-(int)(mSourceView.getWidth()*SCALE_FACTOR/2),
                    y-(int)(mSourceView.getHeight()*SCALE_FACTOR/2-VERTICAL_OFFSET));

            mThumbView.setLayoutParams(params);
            
            TranslateAnimation translate = new TranslateAnimation(0, x-mSourceView.getWidth()/2,
                    0, y-mSourceView.getHeight()/2-VERTICAL_OFFSET);
            translate.setDuration(ANIMATION_DURATION);
         
            ScaleAnimation scale = new ScaleAnimation( 1.0f, SCALE_FACTOR, 
                    1.0f, SCALE_FACTOR, 
                    Animation.ABSOLUTE, x,
                    Animation.ABSOLUTE, y);
            scale.setDuration(ANIMATION_DURATION);
            
            AnimationSet anims = new AnimationSet(true);
            anims.addAnimation(translate);
            anims.addAnimation(scale);
            anims.setAnimationListener(new ThumbnailAnimationListener());
            
            mState = State.IN_ANIMATION;
            mSourceView.startAnimation(anims);
            
        }

        public boolean onDown(MotionEvent pE) { return false; }
        
        public boolean onFling(MotionEvent pE1, MotionEvent pE2, float pVelocityX, float pVelocityY) { 
            Logger.d(LOG_TAG, "dnd ShareGestureListener detects fling, event is potentially delegated"); 
            return false; 
        }
        
        public boolean onScroll(MotionEvent pE1, MotionEvent pE2, float pDistanceX, float pDistanceY) { return false; }
        public void onShowPress(MotionEvent pE) {}
        public boolean onSingleTapUp(MotionEvent pE) { return false; }
    }

    class ThumbnailAnimationListener implements Animation.AnimationListener {
        
        public void onAnimationEnd(Animation animation) {
   
            if (mState == State.IN_ANIMATION) {
                
                mActivityViewGroup.setVisibility(View.GONE);
                mThumbView.setVisibility(View.VISIBLE);
                
                //place drag targets:
                if(mIsDropTargetEnabled){
                    mAbsoluteLayout.addView(mDropTargetCollection.getDropTargetLayout(), 0);
                    Logger.d(LOG_TAG, "\t\t\t display drop targets");
                }
                
                mAbsoluteLayout.invalidate();
                Vibrator vibrator = (Vibrator)mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(ANIMATION_DURATION);
                
                mState = State.DRAGGING;
                
                if (mDragListenerList.size() > 0){
                    Iterator<DragListener> it = mDragListenerList.iterator();
                    while (it.hasNext()){
                        it.next().onDraggingStarted(mSourceView);
                    }
                }
                
            }
            
            Logger.d(LOG_TAG, "animation end -----------------am Ende von onAnimationEnd()");            
        }

        public void onAnimationRepeat(Animation animation) {}
        
        public void onAnimationStart(Animation animation) {}        
    }

}