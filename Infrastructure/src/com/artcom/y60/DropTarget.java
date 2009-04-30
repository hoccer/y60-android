package com.artcom.y60;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;

public class DropTarget {

    private static final int PADDING_X = 15; 
    private static final int PADDING_Y = 100;
    private static final long DROPPING_VIBRATION = 100;
    private static final String LOG_TAG = "DropTarget";
    private static final long ON_TARGET_VIBRATION = 50;
    ImageView mImageView;
    DropListener mDropListener;
    String mName;
    Activity mActivity;
    
    boolean mIsInFocus;
    
    public static DropTarget createTargetTv(){
        
        return null;
    }

    public DropTarget(ImageView pImageView, DropListener pDropListener, Activity pActivity, String pName) {
        super();
        mImageView = pImageView;
        mDropListener = pDropListener;
        mName= pName;
        mIsInFocus = false;
        mActivity= pActivity;
    }

    public ImageView getImageView(){
        return mImageView;
    }

    public void dropped(Activity pActivity){
        Logger.v(LOG_TAG, "dropped, virbrator should start!");
        Vibrator vibrator = (Vibrator)pActivity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(DROPPING_VIBRATION);
        mDropListener.onDropped();
    }

    public String toString(){
        return "DropTarget: '" + mName + " '";
    }

    public boolean isOnFocus(View pThumbView){

        int x= pThumbView.getLeft() + pThumbView.getWidth()/2; //mid of thumb
        int y= (pThumbView.getTop() + PADDING_Y); 

        if( getImageView().getLeft()+PADDING_X < x && 
                getImageView().getRight()-PADDING_X > x &&  
                getImageView().getBottom() > y){

            //Logger.v(LOG_TAG, "i am on item: ", toString());
            return true;
        }
        return false;

    }

    public void focus() {

        if(!mIsInFocus){
            mIsInFocus = true;
            Vibrator vibrator = (Vibrator)mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(ON_TARGET_VIBRATION);
        }
        
       // mImageView.setBackgroundResource(R.drawable.red80);
//        Drawable d= mImageView.getDrawable();
//        if(d != null){
//            d.setColorFilter(Color.parseColor("#585C38"), PorterDuff.Mode.DST_ATOP);
//            Logger.v(LOG_TAG, "drawable parsecolor");
//        }
    }

    public void unfocus() {
        
        if(mIsInFocus){
            mIsInFocus = false;
            Vibrator vibrator = (Vibrator)mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(ON_TARGET_VIBRATION);
        }

    }

    public void handleDragging(View pDraggedView) {
       
        if(isOnFocus(pDraggedView)){               
            focus();
        }else{
            unfocus();
        }        
    }

}




