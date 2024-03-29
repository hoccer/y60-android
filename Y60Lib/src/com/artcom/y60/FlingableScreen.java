package com.artcom.y60;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

@SuppressWarnings("deprecation")
public class FlingableScreen implements AnimationListener {

    // Enumerations -----------------------------------------------------

    public enum Target {
        IN, OUT
    }

    // Constants ---------------------------------------------------------

    protected static final int  ANIMATION_DURATION  = 500;
    private static final String LOG_TAG             = "FlingableScreen";

    // Instance Variables ------------------------------------------------

    private AbsoluteLayout      mBaseLayout;
    private RelativeLayout      mContentLayout;
    private View                mContent;

    protected Activity          mActivity;
    private String              mName;

    private Animation           mOutToLeft;
    private Animation           mOutToRight;
    private Animation           mInFromLeft;
    private Animation           mInFromRight;
    private Animation           mOutToTop;
    private Animation           mOutToBottom;
    private Animation           mInFromTop;
    private Animation           mInFromBottom;

    private int                 mBackgroundResource;
    private Drawable            mBackgroundDrawable = null;

    private boolean             mIsAnimating        = false;

    // Constructors ------------------------------------------------------

    public FlingableScreen(String name, Activity pActivity, int pBackgroundResource) {

        mName = name;
        mActivity = pActivity;
        ProgressBar progress = new ProgressBar(pActivity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progress.setLayoutParams(params);
        mContent = progress;
        mContent.setLongClickable(true);
        mContentLayout = new RelativeLayout(mActivity);
        mContentLayout.setLongClickable(true);
        mContentLayout.addView(mContent);
        mBaseLayout = new AbsoluteLayout(mActivity);
        mBaseLayout.addView(mContentLayout);
        mBaseLayout.setLongClickable(true);

        mOutToLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mOutToLeft.setFillEnabled(true);
        mOutToLeft.setFillAfter(true);
        mOutToLeft.setFillBefore(true);
        mOutToLeft.setDuration(ANIMATION_DURATION);
        mOutToLeft.setAnimationListener(this);

        mInFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mInFromLeft.setFillEnabled(true);
        mInFromLeft.setFillAfter(true);
        mInFromLeft.setFillBefore(true);
        mInFromLeft.setDuration(ANIMATION_DURATION);
        mInFromLeft.setAnimationListener(this);

        mOutToRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mOutToRight.setFillEnabled(true);
        mOutToRight.setFillAfter(true);
        mOutToRight.setFillBefore(true);
        mOutToRight.setDuration(ANIMATION_DURATION);
        mOutToRight.setAnimationListener(this);

        mInFromRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mInFromRight.setFillEnabled(true);
        mInFromRight.setFillAfter(true);
        mInFromRight.setFillBefore(true);
        mInFromRight.setDuration(ANIMATION_DURATION);
        mInFromRight.setAnimationListener(this);

        mOutToTop = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1);
        mOutToTop.setFillEnabled(true);
        mOutToTop.setFillAfter(true);
        mOutToTop.setFillBefore(true);
        mOutToTop.setDuration(ANIMATION_DURATION);
        mOutToTop.setAnimationListener(this);

        mInFromTop = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_SELF, 0);
        mInFromTop.setFillEnabled(true);
        mInFromTop.setFillAfter(true);
        mInFromTop.setFillBefore(true);
        mInFromTop.setDuration(ANIMATION_DURATION);
        mInFromTop.setAnimationListener(this);

        mOutToBottom = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1);
        mOutToBottom.setFillEnabled(true);
        mOutToBottom.setFillAfter(true);
        mOutToBottom.setFillBefore(true);
        mOutToBottom.setDuration(ANIMATION_DURATION);
        mOutToBottom.setAnimationListener(this);

        mInFromBottom = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0);
        mInFromBottom.setFillEnabled(true);
        mInFromBottom.setFillAfter(true);
        mInFromBottom.setFillBefore(true);
        mInFromBottom.setDuration(ANIMATION_DURATION);
        mInFromBottom.setAnimationListener(this);

        mBackgroundResource = pBackgroundResource;
    }

    // Public Instance Methods -------------------------------------------

    public View getContent() {

        return mContent;
    }

    public AbsoluteLayout getBaseLayout() {

        return mBaseLayout;
    }

    public ViewGroup getContentLayout() {

        return mContentLayout;
    }

    public String getName() {

        return mName;
    }

    public void invalidate() {

        refresh();
    }

    public String toString() {

        return getName();
    }

    public void animate(Direction p_Direction, Target p_Target) {

        mBaseLayout.clearAnimation();

        if (p_Target == Target.IN) {

            if (p_Direction == Direction.LEFT) {
                mBaseLayout.startAnimation(mInFromRight);
            } else if (p_Direction == Direction.RIGHT) {
                mBaseLayout.startAnimation(mInFromLeft);
            } else if (p_Direction == Direction.TOP) {
                mBaseLayout.startAnimation(mInFromBottom);
            } else {
                mBaseLayout.startAnimation(mInFromTop);
            }
        } else {
            if (p_Direction == Direction.LEFT) {
                mBaseLayout.startAnimation(mOutToLeft);
            } else if (p_Direction == Direction.RIGHT) {
                mBaseLayout.startAnimation(mOutToRight);
            } else if (p_Direction == Direction.TOP) {
                mBaseLayout.startAnimation(mOutToTop);
            } else {
                mBaseLayout.startAnimation(mOutToBottom);
            }
        }

        showBackground();
    }

    public void clearAnimation() {
        mBaseLayout.clearAnimation();
    }

    public void showBackground() {
        if (mBackgroundDrawable != null) {
            // mBaseLayout.setBackgroundDrawable(mBackgroundDrawable);
            mActivity.getWindow().setBackgroundDrawable(mBackgroundDrawable);
        } else {
            mActivity.getWindow().setBackgroundDrawableResource(mBackgroundResource);
            // mBaseLayout.setBackgroundResource(mBackgroundResource);
        }
    }

    // Protected Instance Methods ----------------------------------------

    public void setContent(View pContent) {
        mContent = pContent;
        mContentLayout.removeAllViews();
        mBaseLayout.removeAllViews();
        mContentLayout.addView(pContent);
        mBaseLayout.addView(mContentLayout);
    }

    public boolean isAnimationRunning() {
        if (mBaseLayout.getAnimation() == null) {
            return false;
        }
        return !mBaseLayout.getAnimation().hasEnded();
    }

    protected synchronized void refresh() {

    }

    @Override
    public void onAnimationEnd(Animation pArg0) {
    }

    @Override
    public void onAnimationRepeat(Animation pArg0) {
    }

    @Override
    public void onAnimationStart(Animation pArg0) {
        mIsAnimating = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(ANIMATION_DURATION + 50);
                } catch (InterruptedException e) {
                    ErrorHandling.signalUnspecifiedError(LOG_TAG, e, mActivity);
                }
                mIsAnimating = false;
            }

        }).start();
    }

    public boolean isAnimating() {
        return mIsAnimating;
    }

    // Inner Classes -----------------------------------------------------

}
