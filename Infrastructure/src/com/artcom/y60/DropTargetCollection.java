package com.artcom.y60;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;

public class DropTargetCollection implements DragListener{

    private static final int PADDING_X = 15; 
    private static final int PADDING_Y = 100;
    private static final String LOG_TAG = "DropTargetCollection";
    List<DropTarget> mDropTargetList;
    AbsoluteLayout mAbsoluteLayout;
    LinearLayout mLinearLayout; 

    public DropTargetCollection(Context pContext, AbsoluteLayout pAbsoluteLayout) {

        mAbsoluteLayout     = pAbsoluteLayout;
        mDropTargetList     = new LinkedList<DropTarget>();
        mLinearLayout       = new LinearLayout(pContext);     
    }

    private void configureLayout() {

        mLinearLayout.removeAllViews();

        Iterator<DropTarget>it= mDropTargetList.iterator();
        while (it.hasNext()) {
            mLinearLayout.addView(it.next().getImageView());
        }

    }

    public void addDropTarget(DropTarget pDropTarget){

        mDropTargetList.add(pDropTarget);
        configureLayout();

    }

    public LinearLayout getDropTargetLayout(){

        return mLinearLayout;
    }

    public DropTarget getfocusedDropTarget(View pThumbView){

        DropTarget dropTarget;
        Iterator<DropTarget>it= mDropTargetList.iterator();

        while (it.hasNext()) {
            dropTarget= it.next();

            //           Logger.v(LOG_TAG,    "dropTarget.getLeft: ", dropTarget.getImageView().getLeft()+PADDING_X,
            //                   "\t< X thumb: ", x,
            //                   " < \tdropTarget.getRight: ", dropTarget.getImageView().getRight()-PADDING_X);

            if(dropTarget.isOnFocus(pThumbView)){
                return dropTarget;
            }

        }
        return null;
    }
    
    

    @Override
    public void onDragged(View pOrigin, View pDraggedView, AbsoluteLayout pAbsoluteLayout, int pX,
            int pY) {
        

        DropTarget dropTarget;
        Iterator<DropTarget>it= mDropTargetList.iterator();

        while (it.hasNext()) {
            dropTarget= it.next();

            if(dropTarget.isOnFocus(pDraggedView)){
                
                dropTarget.focus();
            }else{
                dropTarget.unfocus();
            }

        }
    }

    @Override
    public void onDraggingEnded(View pOrigin, View pDraggedView, int pX, int pY) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDraggingStarted(View pOrigin) {
        // TODO Auto-generated method stub

    }


}

