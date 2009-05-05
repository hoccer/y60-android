package com.artcom.y60;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SlotTestActivity extends Activity {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "SlotTestActivity";
    
    
    
    // Instance Variables ------------------------------------------------

    private BasicSlotHolder mHolder;
    
    private SlotViewer mViewer;
    
    private ImageView mImage;
    
    private boolean mWasInvalidated;
    
    
    // Public Instance Methods -------------------------------------------
    
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        mWasInvalidated = false;
        
        mImage = new ImageView(SlotTestActivity.this);
        mImage.setImageResource(R.drawable.smiley);
        
        Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                     WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.slot_test_layout);
        
        mHolder = new TestingHolder();
        mViewer = new TestingViewer();
    }

    
    
    // Package Protected Instance Methods --------------------------------

    void setLauncher(final SlotLauncher pLauncher) {
        
        runOnUiThread(new Runnable() {
            
            public void run() {
                mHolder.addSlot(pLauncher, mViewer);
                mHolder.activateSlots(null);
                
                LinearLayout layout = (LinearLayout)findViewById(R.id.slot_test_layout);
                View view = mViewer.view();
                layout.addView(view);
                layout.invalidate();
            }
        });
    }
    
    
    SlotHolder getHolder() {
        
        return mHolder;
    }
    
    SlotViewer getViewer() {
        
        return mViewer;
    }
    
    boolean wasInvalidated() {
        
        return mWasInvalidated;
    }
    
    class TestingViewer extends SlotViewer {

        @Override
        public View view() {
            
            return mImage;
        }
        
    }
    
    class TestingHolder extends BasicSlotHolder {
        
        public TestingHolder() {
            
            super(SlotTestActivity.this);
        }
        
        public void invalidate() {
            
            LinearLayout layout = (LinearLayout)findViewById(R.id.slot_test_layout);
            layout.invalidate();
            mWasInvalidated = true;
        }
    }
}
