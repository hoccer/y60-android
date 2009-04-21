package com.artcom.y60;

import android.app.Activity;
import android.content.Context;
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


public class DragAndDropHelper implements OnTouchListener {
    
    public static final String LOG_TAG = "DragAndDropHelper";
    
    private static final float SCALE_FACTOR = 0.3f;
    
    private static final int ANIMATION_DURATION = 1000;
    
    private ViewGroup mLayout;
    private View mSourceView;
    private Activity mActivity;
    private View mThumbView;
    private GestureDetector mGest;
    private AbsoluteLayout mDragLayout;
    
    public static DragAndDropHelper enableDragAndDrop(View pView, ViewGroup pLayout, Activity pActivity){
        
        DragAndDropHelper helper = new DragAndDropHelper(pView, pLayout, pActivity);
        pView.setOnTouchListener(helper);
        return helper;
    }
    
    
    public DragAndDropHelper(View pView, ViewGroup pLayout, Activity pActivity){
        
        mLayout = pLayout;
        mSourceView = pView;
        mActivity = pActivity;
        mGest = new GestureDetector(new ShareGestureListener());
    }
    

    @Override
    public boolean onTouch(View pV, MotionEvent pEvent) {
    
        if (mThumbView == null) {
             
            mThumbView = GraphicsHelper.scaleView(mSourceView, SCALE_FACTOR, mActivity);
            //mThumbView.setVisibility(View.INVISIBLE);
                       
            mDragLayout= new AbsoluteLayout(mActivity);
            mDragLayout.addView(mThumbView);
            mDragLayout.setOnTouchListener(this);
            mThumbView.setOnTouchListener(this);
        }
        
        switch (pEvent.getAction()) {
            case (MotionEvent.ACTION_UP):
                Logger.d(LOG_TAG, "and up!!!");
                //mThumbView.setVisibility(View.INVISIBLE);
//                mSourceView.setVisibility(View.VISIBLE);
                mActivity.setContentView(mLayout);
                mLayout.invalidate();
                break;
                
            case (MotionEvent.ACTION_MOVE):
                if (mThumbView.getVisibility() == View.VISIBLE) {
                    mThumbView.setLayoutParams(positionForDragging(pEvent));
                }
                break;
        }
        
        return mGest.onTouchEvent(pEvent);
    }
    
    private LayoutParams positionForDragging(MotionEvent pEvent) {
        
        int x = (int)pEvent.getX()-mThumbView.getWidth()/2;
        int y = (int)pEvent.getY()-mThumbView.getHeight()/2;
        
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT,
                                x,
                                y);
    }
    
    
    class ShareGestureListener implements OnGestureListener {

        @Override
        public boolean onDown(MotionEvent pE) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onFling(MotionEvent pE1, MotionEvent pE2, float pVelocityX,
                float pVelocityY) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onLongPress(MotionEvent pE) {
            
            Logger.v(LOG_TAG, "LOOOOOOOOOOOOOOOOOOOOOOOONG PRESS");
            
            
            
            //((Activity)mContext).getWindow().getWindowManager().
            
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

        @Override
        public boolean onScroll(MotionEvent pE1, MotionEvent pE2, float pDistanceX,
                float pDistanceY) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onShowPress(MotionEvent pE) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean onSingleTapUp(MotionEvent pE) {
            // TODO Auto-generated method stub
            return false;
        }
        
    }

    class ThumbnailAnimationListener implements Animation.AnimationListener {
        
        public void onAnimationEnd(Animation animation) {
            Logger.d(LOG_TAG, "animation end --------------------------------");
            //mSourceView.setVisibility(View.INVISIBLE);
            //mThumbView.setVisibility(View.VISIBLE);
            mActivity.setContentView(mDragLayout);
//            tab.setImageResource(R.drawable.up);
//            contents.setVisibility(View.GONE);
        }

        public void onAnimationRepeat(Animation animation) {
            // not needed
        }

        public void onAnimationStart(Animation animation) {
            // not needed
        }
    }

}
