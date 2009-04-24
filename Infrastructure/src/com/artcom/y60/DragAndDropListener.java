package com.artcom.y60;

import android.view.View;

public interface DragAndDropListener {

    public void onDraggingStarted(View pOrigin);
    
    public void onDragged(View pOrigin, View pDraggedView, int pX, int pY);
    
    public void onDraggingEnded(View pOrigin, View pDraggedView, int pX, int pY);
}
