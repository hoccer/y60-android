package com.artcom.y60;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class SequenceAnimationViaListener implements AnimationListener {

    Animation mSequencedAnimation;
    View      mView;

    public SequenceAnimationViaListener(View pView, Animation pAnimation) {
        mSequencedAnimation = pAnimation;
        mView = pView;
    }

    @Override
    public void onAnimationEnd(Animation pAnimation) {
        mView.startAnimation(mSequencedAnimation);
    }

    @Override
    public void onAnimationRepeat(Animation pAnimation) {
    }

    @Override
    public void onAnimationStart(Animation pAnimation) {
    }

}
