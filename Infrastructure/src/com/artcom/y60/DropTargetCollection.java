package com.artcom.y60;

import android.content.Context;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;

public class DropTargetCollection extends SlotHolder implements DragListener{

    private static final String LOG_TAG = "DropTargetCollection";
    
    
    // Instance Variables ------------------------------------------------

    private AbsoluteLayout mAbsoluteLayout;
    
    private LinearLayout mLinearLayout; 

    public DropTargetCollection(Context pContext, AbsoluteLayout pAbsoluteLayout) {

        super(pContext);
        
        mAbsoluteLayout     = pAbsoluteLayout;
        mLinearLayout       = new LinearLayout(pContext);     
    }

    public void invalidate() {

        mLinearLayout.removeAllViews();

        for (Slot slot: getSlots()) {
            mLinearLayout.addView(slot.getViewer().view());
        }

    }

    public LinearLayout getDropTargetLayout(){

        return mLinearLayout;
    }

    public Slot getfocusedDropTarget(View pThumbView){

        for (Slot dropTarget: getSlots()) {

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
        

        for (Slot dropTarget: getSlots()) {
            dropTarget.handleDragging(pDraggedView);
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

