package com.artcom.y60;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
//import android.view.animation.Animation.AnimationListener;

public class ViewMagnifyingSlotLauncher extends DecoratingSlotLauncher {

    private static final String LOG_TAG = ViewMagnifyingSlotLauncher.class.getName();

    public ViewMagnifyingSlotLauncher() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ViewMagnifyingSlotLauncher(SlotLauncher pTarget) {
        super(pTarget);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void focusThis() {

        // Logger.d(LOG_TAG, "focus!");
        // View view = getSlot().getViewer().view();
        // if (view.getAnimation() == null) {
        //            
        // int pivx = view.getWidth()/2;
        // int pivy = view.getHeight()/2;
        // ScaleAnimation scale = new ScaleAnimation(0.5f, 1.6f, 1.6f, 0.5f,
        // pivx, pivy);
        // scale.setDuration(800);
        // scale.setZAdjustment(Animation.ZORDER_TOP);
        // scale.setRepeatCount(Animation.INFINITE);
        // scale.setRepeatMode(Animation.REVERSE);
        // view.startAnimation(scale);
        // }
    }

    @Override
    protected void launchThis() {
        Logger.v(LOG_TAG, "launch magnifying slot launcher");
        View view = getSlot().getViewer().view();
        int pivx = view.getWidth() / 2;
        int pivy = view.getHeight() / 2;

        ScaleAnimation scale = new ScaleAnimation(1.0f, 10.0f, 1.0f, 10.0f, pivx, pivy);
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.1f);

        AnimationSet launchAnimation = new AnimationSet(true);
        launchAnimation.addAnimation(scale);
        launchAnimation.addAnimation(alpha);
        launchAnimation.setZAdjustment(Animation.ZORDER_TOP);
        launchAnimation.setDuration(800);

        view.startAnimation(launchAnimation);
    }

    @Override
    protected void unfocusThis() {

        View view = getSlot().getViewer().view();
        view.clearAnimation();
    }

}
