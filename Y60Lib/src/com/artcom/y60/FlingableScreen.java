package com.artcom.y60;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;

public class FlingableScreen implements AnimationListener {

    // Enumerations -----------------------------------------------------

    public enum Target {
        IN, OUT
    }

    // Constants ---------------------------------------------------------

    private static final int    ANIMATION_DURATION = 500;
    private static final String LOG_TAG            = "FlingableScreen";

    // Instance Variables ------------------------------------------------

    private AbsoluteLayout      mBaseLayout;

    private TableLayout         mContentLayout;

    private View                mContent;

    private Activity            mActivity;
    private String              mName;

    private Animation           mOutToLeft;
    private Animation           mOutToRight;
    private Animation           mInFromLeft;
    private Animation           mInFromRight;

    private int                 mBackgroundResource;

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
        mContentLayout = new TableLayout(mActivity);
        mContentLayout.setOrientation(TableLayout.HORIZONTAL);
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

        Logger.v(LOG_TAG, "animating");

        if (p_Target == Target.IN) {

            if (p_Direction == Direction.LEFT) {
                mBaseLayout.startAnimation(mInFromRight);
            } else {
                mBaseLayout.startAnimation(mInFromLeft);
            }
        } else {
            if (p_Direction == Direction.LEFT) {
                mBaseLayout.startAnimation(mOutToLeft);
            } else {
                mBaseLayout.startAnimation(mOutToRight);
            }
        }

        showBackground();
    }

    public void showBackground() {

        Window win = mActivity.getWindow();
        win.setBackgroundDrawableResource(mBackgroundResource);
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