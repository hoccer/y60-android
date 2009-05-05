package com.artcom.y60;

import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;

public class DnDTestActivityTest extends Y60ActivityInstrumentationTest<DnDTestActivity> implements
        DragListener {

    protected static final String LOG_TAG = "DnDTestActivityTest";
    private DraggingStartedEvent mDraggingStarted;
    private DraggingEndedEvent mDraggingEnded;
    private DraggedEvent mDragged;

    public void setUp() throws Exception {
        super.setUp();
        mDraggingStarted = null;
        mDraggingEnded = null;
        mDragged = null;
        getActivity().getDragAndDropHelper().addDragListener(this);
    }

    public DnDTestActivityTest() {
        super("com.artcom.y60", DnDTestActivity.class);

    }

    public void testDraggedToLocation() throws InterruptedException {

        touch(getActivity().getDragResource());
        waitForDragStarted(1500);
        moveAndRelease(200, 200);
        waitForDragEnded(100);

        assertEquals("y callback param equals view value", mDraggingEnded.y,
                mDraggingEnded.draggedView.getTop());
        assertEquals("dragged to y = 200 ", 200.0, mDraggingEnded.draggedView.getTop()
                + (mDraggingEnded.draggedView.getHeight() / 2.0)
                + DragAndDropHelper.VERTICAL_OFFSET);

        // Logger.v(LOG_TAG, "width ", pDraggedView.getWidth(), " height ",
        // pDraggedView
        // .getHeight());
        // Logger.v(LOG_TAG, "onDraggingEnded ", pX, pY, pY +
        // pDraggedView.getHeight() / 2.0
        // + DragAndDropHelper.VERTICAL_OFFSET);
        // Logger.v(LOG_TAG, "getTop: ", pDraggedView.getTop(), "; getBottom: ",
        // pDraggedView
        // .getBottom());
        // Logger.v(LOG_TAG, "getLeft: ", pDraggedView.getLeft());

    }

    public void testReleaseLongPress() throws InterruptedException {

        touch(getActivity().getDragResource());
        waitForDragStarted(1500);
        release(getActivity().getDragResource());
        waitForDragEnded(100);
        assertEquals(View.VISIBLE, mDraggingEnded.origin.getVisibility());
        assertEquals(View.GONE, mDraggingEnded.draggedView.getVisibility());

    }

    // public void testExistenceOfDragTarget(){
    //        
    // }

    public void testDropTargetExistence() throws InterruptedException {

        assertEquals("AbsoluteLayout has original layout as only child", 1, getActivity()
                .getAbsoluteLayout().getChildCount());

        touch(getActivity().getDragResource());
        waitForDragStarted(1500);
        release(getActivity().getDragResource());
        waitForDragEnded(100);

        LinearLayout dropTargetContainer = (LinearLayout) getActivity().getAbsoluteLayout()
                .getChildAt(0);
        assertEquals("There is one target", 1, dropTargetContainer.getChildCount());
        assertEquals("The drop target is visible ", View.VISIBLE, dropTargetContainer
                .getVisibility());

    }

    public void testDroppingOnTarget() throws InterruptedException {

        assertFalse("The item is not dropped yet ", getActivity().isDroppedOnTarget());

        touch(getActivity().getDragResource());
        waitForDragStarted(1500);
        moveAndRelease(50, 50);
        waitForDragEnded(100);       

        assertTrue("The item was dropped", getActivity().isDroppedOnTarget());

    }

    private void waitForDragStarted(long pInterval) throws InterruptedException {

        long time = System.currentTimeMillis();
        while (mDraggingStarted == null && System.currentTimeMillis() - time < pInterval) {
            Thread.sleep(10);
        }

        assertNotNull("dragging didn't start for " + pInterval + " millis!", mDraggingStarted);
    }

    private void waitForDragging(long pInterval) throws InterruptedException {

        long time = System.currentTimeMillis();
        while (mDragged == null && System.currentTimeMillis() - time < pInterval) {
            Thread.sleep(10);
        }

        assertNotNull("dragging didn't occur for " + pInterval + " millis!", mDragged);
    }

    private void waitForDragEnded(long pInterval) throws InterruptedException {

        long time = System.currentTimeMillis();
        while (mDraggingEnded == null && System.currentTimeMillis() - time < pInterval) {
            Thread.sleep(10);
        }

        assertNotNull("dragging didn't end for " + pInterval + " millis!", mDraggingEnded);
    }

    @Override
    public void onDragged(View pOrigin, View pDraggedView, AbsoluteLayout pAbsoluteLayout, int pX,
            int pY) {
        mDragged = new DraggedEvent(pOrigin, pDraggedView, pAbsoluteLayout, pX, pY);
    }

    @Override
    public void onDraggingEnded(View pOrigin, View pDraggedView, int pX, int pY) {
        mDraggingEnded = new DraggingEndedEvent(pOrigin, pDraggedView, pX, pY);
    }

    @Override
    public void onDraggingStarted(View pOrigin) {
        mDraggingStarted = new DraggingStartedEvent(pOrigin);
    }

    class DraggedEvent {

        View origin;
        View draggedView;
        AbsoluteLayout layout;
        int x;
        int y;

        public DraggedEvent(View pOrigin, View pDraggedView, AbsoluteLayout pAbsoluteLayout,
                int pX, int pY) {

            origin = pOrigin;
            draggedView = pDraggedView;
            layout = pAbsoluteLayout;
            x = pX;
            y = pY;
        }
    }

    class DraggingEndedEvent {

        View origin;
        View draggedView;
        int x;
        int y;

        public DraggingEndedEvent(View pOrigin, View pDraggedView, int pX, int pY) {

            origin = pOrigin;
            draggedView = pDraggedView;
            x = pX;
            y = pY;
        }
    }

    class DraggingStartedEvent {

        View origin;

        public DraggingStartedEvent(View pOrigin) {

            origin = pOrigin;
        }
    }
}
