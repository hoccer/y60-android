package com.artcom.y60;

import android.app.Activity;
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
    
    private static final float SCALE_FACTOR = 0.5f;
    
    private static final int ANIMATION_DURATION = 1000;
    
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
    
    // Static Methods ----------------------------------------------------
    
    /**
     * Makes the given draggable by longpressing it.
     */
    public static DragAndDropHelper enableDragAndDrop(View pView, AbsoluteLayout pLayout, Activity pActivity){
        
        DragAndDropHelper helper = new DragAndDropHelper(pView, pLayout, pActivity);
        return helper;
    }
    
    // Constructors ------------------------------------------------------

    public DragAndDropHelper(View pView, AbsoluteLayout pLayout, Activity pActivity){
        
        mLayout = pLayout;
        mLayout.setOnTouchListener(this);
        
        mSourceView = pView;
        mSourceView.setOnTouchListener(this);
        
        mActivity = pActivity;
        mGest = new GestureDetector(new ShareGestureListener());
    }
    
    // Public Instance Methods -------------------------------------------

    @Override
    public boolean onTouch(View pV, MotionEvent pEvent) {
    
        Logger.d(LOG_TAG, "onTouch ", pV, " ", pEvent.getAction());
        
        if (mThumbView == null) {
            
            mThumbView = GraphicsHelper.scaleView(mSourceView, SCALE_FACTOR, mActivity);
            mThumbView.setVisibility(View.INVISIBLE);
            mThumbView.setOnTouchListener(this);
            mLayout.addView(mThumbView);
        }
        
        if (isCurrentlyDragging()) {
            
            switch (pEvent.getAction()) {
                case (MotionEvent.ACTION_UP):
                    endDragging();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    drag(pEvent);
                    break;
            }
        }
        
        if (pV == mSourceView || isCurrentlyDragging()) {
            mGest.onTouchEvent(pEvent);
        }
        
        return true;
    }

    // Private Instance Methods ------------------------------------------
    
    private void drag(MotionEvent pEvent) {
        
        mThumbView.setLayoutParams(positionForDragging(pEvent));
    }

    private void endDragging() {
        
        Logger.d(LOG_TAG, "and up!!!");
        mThumbView.setVisibility(View.INVISIBLE);
        mSourceView.setVisibility(View.VISIBLE);
        mLayout.invalidate();
    }

    private LayoutParams positionForDragging(MotionEvent pEvent) {
        
        int x = (int)pEvent.getX()-mThumbView.getWidth()/2;
        int y = (int)pEvent.getY()-mThumbView.getHeight()/2;
        
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT,
                                x,
                                y);
    }
    
    private boolean isCurrentlyDragging() {
        return mThumbView.getVisibility() == View.VISIBLE;
    }
    
    // Inner Classes -----------------------------------------------------
    
    class ShareGestureListener implements OnGestureListener {

        @Override
        public void onLongPress(MotionEvent pE) {
            
            Logger.v(LOG_TAG, "LOOOOOOOOOOOOOOOOOOOOOOOONG PRESS");
            
            int x = (int)pE.getX();//-mDrawable.getMinimumWidth()/2;
            int y = (int)pE.getY()-10;//-mDrawable.getMinimumHeight()*2;
            
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    x-(int)(mSourceView.getWidth()*SCALE_FACTOR/2),
                    y-(int)(mSourceView.getHeight()*SCALE_FACTOR/2));

            mThumbView.setLayoutParams(params);
            
            TranslateAnimation translate = new TranslateAnimation(0, x-mSourceView.getWidth()/2,
                    0, y-mSourceView.getHeight()/2);
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
        public boolean onFling(MotionEvent pE1, MotionEvent pE2, float pVelocityX, float pVelocityY) { return false; }
        public boolean onScroll(MotionEvent pE1, MotionEvent pE2, float pDistanceX, float pDistanceY) { return false; }
        public void onShowPress(MotionEvent pE) {}
        public boolean onSingleTapUp(MotionEvent pE) { return false; }
    }

    class ThumbnailAnimationListener implements Animation.AnimationListener {
        
        public void onAnimationEnd(Animation animation) {
            
            Logger.d(LOG_TAG, "animation end --------------------------------");
//            mLayout.getChildAt(0).setVisibility(View.INVISIBLE);
            mSourceView.setVisibility(View.INVISIBLE);
            mThumbView.setVisibility(View.VISIBLE);
            mLayout.invalidate();
        }

        public void onAnimationRepeat(Animation animation) {}
        public void onAnimationStart(Animation animation) {}
    }
}
