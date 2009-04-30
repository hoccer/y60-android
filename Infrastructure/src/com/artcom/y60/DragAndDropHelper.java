package com.artcom.y60;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;


public class DragAndDropHelper implements OnTouchListener {
    
    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "DragAndDropHelper";
    
    private static final float SCALE_FACTOR = 0.4f;
    
    private static final int ANIMATION_DURATION = 200;

    public static final int VERTICAL_OFFSET = 20;
    
    // Instance Variables ------------------------------------------------
    
    /** The Layout in which the dragging takes place */
    private AbsoluteLayout mLayout;
    
    /** The View to be dragged around */
    private View mSourceView;
    
    /** The foreground activity that displayed the draggable view */
    private Activity mActivity;
    
    /** A thumbnailed screenshot of the draggable view */
    private View mThumbView;
    
    /** Used for detecting the long press */
    private GestureDetector mGest;
    
    private OnTouchListener mTouchListener;
    
    private DragAndDropListener mDragListener;
    
    private View mDefaultThumbnail;
    
    // Static Methods ----------------------------------------------------
    
    /**
     * Makes the given draggable by longpressing it.
     */
    public static DragAndDropHelper enableDragAndDrop(View pView, AbsoluteLayout pLayout, Activity pActivity, View pDefaultThumbnail){
        
        DragAndDropHelper helper = new DragAndDropHelper(pView, pLayout, pActivity, pDefaultThumbnail);
        return helper;
    }
    
    public static DragAndDropHelper enableDragAndDrop(View pView, AbsoluteLayout pLayout, Activity pActivity){
        
        return enableDragAndDrop(pView, pLayout, pActivity, null);
    }
    
    
    // Constructors ------------------------------------------------------

    public DragAndDropHelper(View pView, AbsoluteLayout pLayout, Activity pActivity, View pDefaultThumbnail){
        
        mLayout = pLayout;
        mLayout.setOnTouchListener(this);
        mLayout.setLongClickable(true);
        
        mSourceView = pView;
        mSourceView.setOnTouchListener(this);
        mSourceView.setLongClickable(true);
        
        mDefaultThumbnail = pDefaultThumbnail;
                
        mActivity = pActivity;
        mGest = new GestureDetector(new ShareGestureListener());
    }
    
    // Public Instance Methods -------------------------------------------

//    @Override
    public boolean onTouch(View pTouchedView, MotionEvent pEvent) {
    
        Logger.d(LOG_TAG, "onTouch ", pTouchedView, " ", pEvent.getAction());
        
        if (isCurrentlyDragging() /*|| pTouchedView == mThumbView || pTouchedView == mSourceView */) {
            
            switch (pEvent.getAction()) {
                case (MotionEvent.ACTION_UP):
                    endDragging(pEvent);
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    drag(pEvent);
                    return true;
            }
        }
        
        if (pTouchedView == mSourceView || isCurrentlyDragging()) {
            if (mGest.onTouchEvent(pEvent)) {
                Logger.d(LOG_TAG, "dnd gesture detects s o m e t h i n g");
                return true;
            }
            Logger.d(LOG_TAG, "dnd gesture didnt consume touch event");
        }
        
        
        
        if (mTouchListener != null) {
            
            Logger.d(LOG_TAG, "delegate touch event to pic gallery lsner");
            return mTouchListener.onTouch(pTouchedView, pEvent);
            
        } else {
            
            return false;
        }
    }
    
    public void setOnTouchListener(OnTouchListener pListener) {
        
        mTouchListener = pListener;
    }
    
    public void setDragAndDropListener(DragAndDropListener pListener) {
        
        mDragListener = pListener;
    }

    // Private Instance Methods ------------------------------------------
    
    private void drag(MotionEvent pEvent) {
        
        LayoutParams position = positionForDragging(pEvent);
        mThumbView.setLayoutParams(position);
        if (mDragListener != null) {
            mDragListener.onDragged(mSourceView, mThumbView, position.x, position.y);
        }
    }

    private void endDragging(MotionEvent pEvent) {
        
        Logger.d(LOG_TAG, "and up!!!");
        
        mThumbView.setVisibility(View.GONE);
        mLayout.removeView(mThumbView);   
        mSourceView.setVisibility(View.VISIBLE);
        mLayout.invalidate();
        
        if (mDragListener != null) { 
            LayoutParams position = positionForDragging(pEvent);
            mDragListener.onDraggingEnded(mSourceView, mThumbView, position.x, position.y);
        }
        
        mThumbView = null; // let the gc take care of it
        System.gc(); // would be nice...
    }

    private LayoutParams positionForDragging(MotionEvent pEvent) {
        
        int x = (int)pEvent.getX()-mThumbView.getWidth()/2;
        int y = (int)pEvent.getY()-mThumbView.getHeight()/2-VERTICAL_OFFSET;
        
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

//        @Override
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
                mLayout.addView(mThumbView);
            }
            
            Logger.v(LOG_TAG, "thumb: ", mThumbView.toString());
            
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
            
            mSourceView.startAnimation(anims);
            
        }

        public boolean onDown(MotionEvent pE) { return false; }
        public boolean onFling(MotionEvent pE1, MotionEvent pE2, float pVelocityX, float pVelocityY) { Logger.d(LOG_TAG, "dnd gesture detects fling"); return false; }
        public boolean onScroll(MotionEvent pE1, MotionEvent pE2, float pDistanceX, float pDistanceY) { return false; }
        public void onShowPress(MotionEvent pE) {}
        public boolean onSingleTapUp(MotionEvent pE) { return false; }
    }

    class ThumbnailAnimationListener implements Animation.AnimationListener {
        
        public void onAnimationEnd(Animation animation) {
            
            Logger.d(LOG_TAG, "animation end -----------------");
            
            mSourceView.setVisibility(View.INVISIBLE);
            mThumbView.setVisibility(View.VISIBLE);
            mLayout.invalidate();
            Vibrator vibrator = (Vibrator)mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(ANIMATION_DURATION);
            if (mDragListener != null) {
                mDragListener.onDraggingStarted(mSourceView);
            }
        }

        public void onAnimationRepeat(Animation animation) {}
        public void onAnimationStart(Animation animation) {}
    }
}
