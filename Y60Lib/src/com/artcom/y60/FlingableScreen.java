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

public class FlingableScreen implements AnimationListener {

    // Enumerations -----------------------------------------------------

    public enum Target {
        IN, OUT
    }

    // Constants ---------------------------------------------------------

    private static final int    ANIMATION_DURATION  = 500;
    private static final String LOG_TAG             = "FlingableScreen";

    // Instance Variables ------------------------------------------------

    private AbsoluteLayout      mBaseLayout;

    private RelativeLayout      mContentLayout;

    private View                mContent;

    private Activity            mActivity;
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

    // Constructors ------------------------------------------------------

    public FlingableScreen(String p_Name, Activity pActivity, int pBackgroundResource) {

        mName = p_Name;
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
        mOutToLeft.setDuration(ANIMATION_DURATION);
        mOutToLeft.setAnimationListener(this);

        mInFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mInFromLeft.setDuration(ANIMATION_DURATION);
        mInFromLeft.setAnimationListener(this);

        mOutToRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mOutToRight.setDuration(ANIMATION_DURATION);
        mOutToRight.setAnimationListener(this);

        mInFromRight = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mInFromRight.setDuration(ANIMATION_DURATION);
        mInFromRight.setAnimationListener(this);

        mOutToTop = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1);
        mOutToTop.setDuration(ANIMATION_DURATION);
        mOutToTop.setAnimationListener(this);

        mInFromTop = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_SELF, 0);
        mInFromTop.setDuration(ANIMATION_DURATION);
        mInFromTop.setAnimationListener(this);

        mOutToBottom = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1);
        mOutToBottom.setDuration(ANIMATION_DURATION);
        mOutToBottom.setAnimationListener(this);

        mInFromBottom = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0);
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

    public void showBackground() {
        if (mBackgroundDrawable != null) {
            // mBaseLayout.setBackgroundDrawable(mBackgroundDrawable);
            mActivity.getWindow().setBackgroundDrawable(mBackgroundDrawable);
        } else {
            mActivity.getWindow().setBackgroundDrawableResource(mBackgroundResource);
            // mBaseLayout.setBackgroundResource(mBackgroundResource);
        }
    }

    public void setBackgroundForFlingable(Drawable pBackground) {
        mBackgroundDrawable = pBackground;
        mActivity.getWindow().setBackgroundDrawable(pBackground);
        // mBaseLayout.setBackgroundDrawable(mBackgroundDrawable);
    }

    public void clear() {

    }

    // Protected Instance Methods ----------------------------------------

    protected void setContent(View pContent) {
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
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationRepeat(Animation pArg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAnimationStart(Animation pArg0) {
        // TODO Auto-generated method stub

    }

    // Inner Classes -----------------------------------------------------

}
