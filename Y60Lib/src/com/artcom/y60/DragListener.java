package com.artcom.y60;

import android.view.View;
import android.widget.AbsoluteLayout;

public interface DragListener {

    public void onDraggingStarted(View pOrigin);

    public void onDragged(View pOrigin, View pDraggedView, AbsoluteLayout pAbsoluteLayout, int pX,
            int pY);

    public void onDraggingAborted(View pOrigin, View pDraggedView, int pX, int pY);

    public void onBeforeDraggingStarted(View pSourceView);
}
