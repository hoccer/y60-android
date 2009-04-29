package com.artcom.y60;

import android.test.TouchUtils;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AbsoluteLayout.LayoutParams;

public class DnDTestActivityTest extends Y60ActivityInstrumentationTest<DnDTestActivity> {
    
    /** height of the window's status bar in pixels */
    private static final int STATUS_BAR_HEIGHT = 50;

    public DnDTestActivityTest() {
        super("com.artcom.y60", DnDTestActivity.class);
                
    }
    
    public void testLongPress(){
        
        DnDTestActivity act = getActivity();
        DragAndDropHelper dnd = act.getDragAndDropHelper();
        
        dnd.addDragListener(new DragListener(){

            @Override
            public void onDragged(View pOrigin, View pDraggedView, AbsoluteLayout pAbsoluteLayout, int pX, int pY) {

                Logger.v(tag(), "onDragged ", pX, pY);
            }

            @Override
            public void onDraggingEnded(View pOrigin, View pDraggedView, int pX, int pY) {
                
                Logger.v(tag(), "onDraggingEnded ", pX, pY);
                
                assertEquals(0 - DragAndDropHelper.VERTICAL_OFFSET - STATUS_BAR_HEIGHT,
                             pY+pDraggedView.getHeight()/2);
                
                // horizontal center of image should be at horiz. center of the screen
                assertEquals(160, pX+pDraggedView.getWidth()/2); 
            }

            @Override
            public void onDraggingStarted(View pOrigin) {
                
                Logger.v(tag(), "onDraggingStarted");
            }
        });
        
        TouchUtils.longClickView(this, getActivity().getDragResource());        
        TouchUtils.dragViewToTop(this, getActivity().getDragResource());
        
    }
    
    public void testReleaseLongPress(){
        
        //get dnd-helper to register lsner
        DnDTestActivity testActivity = getActivity();
        DragAndDropHelper dnd= testActivity.getDragAndDropHelper();
        
        dnd.addDragListener(new DragListener(){

            @Override
            public void onDragged(View pOrigin, View pDraggedView, AbsoluteLayout pAbsoluteLayout, int pX, int pY) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onDraggingEnded(View pOrigin, View pDraggedView, int pX, int pY) {
                
                assertEquals(pOrigin.getVisibility(), View.VISIBLE);
                assertEquals(pDraggedView.getVisibility(), View.INVISIBLE);
                
            }

            @Override
            public void onDraggingStarted(View pOrigin) {
                // TODO Auto-generated method stub
                
            }
            
        });
        
        
        TouchUtils.longClickView(this, getActivity().getDragResource());        
    }
    
    public void testExistenceOfDragTarget(){
        
    }

}
