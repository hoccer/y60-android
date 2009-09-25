/**
 * 
 */
package com.artcom.y60;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnTouchListener;

class SlotLaunchingClickListener implements OnTouchListener {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "SlotLaunchingClickListener";

    // Instance Variables ------------------------------------------------

    private SlotLauncher        mLauncher;
    private SlotHolder          mHolder;
    private GestureDetector     mGest;
    private OnGestureListener   mSwiper;

    // Constructors ------------------------------------------------------

    public SlotLaunchingClickListener(SlotLauncher pLauncher, SlotHolder pHolder) {

        mLauncher = pLauncher;
        mHolder = pHolder;
        mSwiper = new SwipeListener();
        mGest = new GestureDetector(mSwiper);
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public boolean onTouch(View pArg0, MotionEvent pArg1) {
        // TODO Auto-generated method stub
        Logger.d(LOG_TAG, "slot clicked!");
        // try {
        // mLauncher.launch();
        // } catch (GomException gx) {
        // ErrorHandling.signalGomError(LOG_TAG, gx, mHolder.getContext());
        mGest.onTouchEvent(pArg1);
        // }
        return true;
    }

    class SwipeListener implements OnGestureListener {

        private static final String LOG_TAG = "NewHomeScreen.SwipeListener";

        public SwipeListener() {
            super();
        }

        public boolean onDown(MotionEvent arg0) {
            return false;
        }

        public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {

            if (mHolder.getGestureListener() == null) {
                return false;
            }
            mHolder.getGestureListener().onFling(arg0, arg1, arg2, arg3);

            return true;
        }

        public void onLongPress(MotionEvent arg0) {
        }

        public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
            return false;
        }

        public void onShowPress(MotionEvent arg0) {
        }

        public boolean onSingleTapUp(MotionEvent arg0) {
            Logger.d(LOG_TAG, "slot clicked!");
            mLauncher.launch();
            return false;
        }
    }

}