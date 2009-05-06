package com.artcom.y60;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;


public class DnDTestActivity extends Activity {

    public static final String LOG_TAG = "DndTestActivity";
    
    private ImageView mResourceScreenshot;
    
    private BitmapDrawable mDrawable;

    private AbsoluteLayout mLayout;
    
    private DragAndDropHelper mDragonDropper;

    private boolean mIsDroppedOnTarget = false;

    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.dnd_test_layout);
        mLayout = (AbsoluteLayout)findViewById(R.id.share_outer_layout); 
        
        mResourceScreenshot = (ImageView)findViewById(R.id.mock_resource_view);
        mDragonDropper = DragAndDropHelper.enableDragAndDrop(mResourceScreenshot, mLayout, this);
        
        ImageView sendToImage = new ImageView(this);
        sendToImage.setBackgroundResource(R.drawable.red80);
        sendToImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        SlotLauncher launcher= new SlotLauncher() {

            @Override
            public void launch() {
                
                mIsDroppedOnTarget = true;
                
            }
            
        };
        mDragonDropper.addDropTarget(new Slot("CONTACTS", launcher, new StaticSlotViewer(sendToImage)));
        
    }
    
    public ImageView getDragResource(){
        
        return mResourceScreenshot;
        
    }
    
    DragAndDropHelper getDragAndDropHelper() {
        
        return mDragonDropper;
    }
    
    public boolean isDroppedOnTarget() {
        
        return mIsDroppedOnTarget;
    }
    
    public AbsoluteLayout getAbsoluteLayout(){
        
        return mLayout;
    }
    
}
