package com.artcom.y60;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

public class DnDTestActivity extends Activity {

    public static final String LOG_TAG = "DndTestActivity";
    
    private ImageView mResourceScreenshot;
    
    private BitmapDrawable mDrawable;

    private AbsoluteLayout mLayout;
    
    private DragAndDropHelper mDragonDropper;

    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.dnd_test_layout);
        mLayout = (AbsoluteLayout)findViewById(R.id.share_outer_layout); 
        
        mResourceScreenshot = (ImageView)findViewById(R.id.mock_resource_view);
        mDragonDropper = DragAndDropHelper.enableDragAndDrop(mResourceScreenshot, mLayout, this);
    }
    
    public ImageView getDragResource(){
        
        return mResourceScreenshot;
        
    }
    
    DragAndDropHelper getDragAndDropHelper() {
        
        return mDragonDropper;
    }
}
