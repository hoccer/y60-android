package com.artcom.y60;

import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.util.DisplayMetrics;

@SuppressWarnings("deprecation")
public class DnDTestActivityTest extends Y60ActivityInstrumentationTest<DnDTestActivity> implements
        DragListener {

    protected static final String LOG_TAG = "DnDTestActivityTest";
    private DraggingStartedEvent  mDraggingStarted;
    private DraggingAbortedEvent  mDraggingAboarted;
    @SuppressWarnings("unused")
	private DraggedEvent          mDragged;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mDraggingStarted = null;
        mDraggingAboarted = null;
        mDragged = null;
    }

    public DnDTestActivityTest() {
        super("com.artcom.y60", DnDTestActivity.class);

    }

    public void testDraggedToLocation() throws InterruptedException {

        getActivity().getDragAndDropHelper().addDragListener(this);
        touch(getActivity().getDragResource());
        waitForDragStarted(2500);
        moveAndRelease(200, 200);
        assertDragWasAborted(100);

        assertEquals("y callback param equals view value", mDraggingAboarted.y,
                mDraggingAboarted.draggedView.getTop());

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        assertEquals(
                "dragged to y = 200 ",
                200,
                (int)(  ( mDraggingAboarted.draggedView.getTop()
                        + (mDraggingAboarted.draggedView.getHeight() / 2.0)
                        + DragAndDropHelper.VERTICAL_OFFSET) 
                        * dm.density )
                );
    }

    public void testReleaseLongPress() throws InterruptedException {

        getActivity().getDragAndDropHelper().addDragListener(this);
        touch(getActivity().getDragResource());
        waitForDragStarted(1500);
        release(getActivity().getDragResource());
        assertDragWasAborted(200);
        assertEquals(View.VISIBLE, mDraggingAboarted.origin.getVisibility());
        // assertEquals("item should stay visible -- animation will follow",
        // View.VISIBLE,
        // mDraggingAboarted.draggedView.getVisibility());

        assertEquals("item should become invisible when animation is canceled", View.INVISIBLE,
                mDraggingAboarted.draggedView.getVisibility());
    }

    public void testDropTargetExistence() throws InterruptedException {

        getActivity().getDragAndDropHelper().addDragListener(this);
        assertEquals("AbsoluteLayout has original layout as only child", 1, getActivity()
                .getAbsoluteLayout().getChildCount());

        touch(getActivity().getDragResource());
        waitForDragStarted(1500);
        release(getActivity().getDragResource());
        assertDragWasAborted(100);

        LinearLayout dropTargetContainer = (LinearLayout) getActivity().getAbsoluteLayout()
                .getChildAt(0);
        assertEquals("There is one target", 1, dropTargetContainer.getChildCount());
        assertEquals("The drop target is visible ", View.VISIBLE, dropTargetContainer
                .getVisibility());

    }

    public void testCancelDraggingByNotDroppingOnTarget() throws Exception {

        getActivity().getDragAndDropHelper().addDragListener(this);
        assertFalse("The item is not dropped yet ", getActivity().isDroppedOnTarget());

        touch(getActivity().getDragResource());
        waitForDragStarted(1500);
        moveAndRelease(200, 150);
        assertDragWasAborted(2000);
    }

    public void testDroppingOnTarget() throws Exception {

        getActivity().getDragAndDropHelper().addDragListener(this);
        assertFalse("The item is not dropped yet ", getActivity().isDroppedOnTarget());

        touch(getActivity().getDragResource());
        waitForDragStarted(1500);
        moveAndRelease(50, 50);
        assertIsDroppedOnTarget();

        assertNull("dragging should not be aborted", mDraggingAboarted);
    }

    private void assertIsDroppedOnTarget() throws Exception {
        TestHelper.blockUntilTrue("The item should have been dropped", 2000,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() throws Exception {
                        return getActivity().isDroppedOnTarget();
                    }
                });
    }

    private void waitForDragStarted(long pInterval) throws InterruptedException {

        long time = System.currentTimeMillis();
        while (mDraggingStarted == null && System.currentTimeMillis() - time < pInterval) {
            Thread.sleep(10);
        }

        assertNotNull("dragging didn't start for " + pInterval + " millis!", mDraggingStarted);
    }

    /*private void waitForDragging(long pInterval) throws InterruptedException {

        long time = System.currentTimeMillis();
        while (mDragged == null && System.currentTimeMillis() - time < pInterval) {
            Thread.sleep(10);
        }

        assertNotNull("dragging didn't occur for " + pInterval + " millis!", mDragged);
    }*/

    private void assertDragWasAborted(long pInterval) throws InterruptedException {

        long time = System.currentTimeMillis();
        while (mDraggingAboarted == null && System.currentTimeMillis() - time < pInterval) {
            Thread.sleep(10);
        }

        assertNotNull("dragging didn't end for " + pInterval + " millis!", mDraggingAboarted);
    }

    @Override
    public void onDragged(View pOrigin, View pDraggedView, AbsoluteLayout pAbsoluteLayout, int pX,
            int pY) {
        mDragged = new DraggedEvent(pOrigin, pDraggedView, pAbsoluteLayout, pX, pY);
    }

    @Override
    public void onDraggingAborted(View pOrigin, View pDraggedView, int pX, int pY) {
        mDraggingAboarted = new DraggingAbortedEvent(pOrigin, pDraggedView, pX, pY);
    }

    @Override
    public void onDraggingStarted(View pOrigin) {
        mDraggingStarted = new DraggingStartedEvent(pOrigin);
    }

    class DraggedEvent {

        View           origin;
        View           draggedView;
        AbsoluteLayout layout;
        int            x;
        int            y;

        public DraggedEvent(View pOrigin, View pDraggedView, AbsoluteLayout pAbsoluteLayout,
                int pX, int pY) {

            origin = pOrigin;
            draggedView = pDraggedView;
            layout = pAbsoluteLayout;
            x = pX;
            y = pY;
        }
    }

    class DraggingAbortedEvent {

        View origin;
        View draggedView;
        int  x;
        int  y;

        public DraggingAbortedEvent(View pOrigin, View pDraggedView, int pX, int pY) {

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

    @Override
    public void onBeforeDraggingStarted(View pSourceView) {
        // TODO Auto-generated method stub

    }
}
