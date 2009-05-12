package com.artcom.y60;

import android.test.TouchUtils;
import android.view.View;

public class SlotTestActivityTest extends Y60ActivityInstrumentationTest<SlotTestActivity> {

    // Constructors ------------------------------------------------------

    public SlotTestActivityTest() {
        
        super("com.artcom.y60", SlotTestActivity.class);
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void setUp() throws Exception {

        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {

        super.tearDown();
    }
    
    public void testSlotLaunch() {
        
        final ReferenceTo<Boolean> launched = new ReferenceTo<Boolean>(false);
        
        clickToLaunch(new SlotLauncher() {

            @Override
            public void launch() {
                
                Logger.d(tag(), "launch!");
                launched.setValue(true);
            }
        });
        
        assertTrue("slot wasn't launched", launched.getValue());
    }
    
    
    public void testLayoutInvalidationViaLaunch() {
        
        clickToLaunch(new SlotLauncher() {

            @Override
            public void launch() {
                
                Logger.d(tag(), "launch!");
                getSlot().invalidate();
            }
        });
        
        assertTrue("holder wasn't invalidated", getActivity().wasInvalidated());
    }
    
    private void clickToLaunch(SlotLauncher pLauncher) {
        
        SlotTestActivity act = getActivity();
        act.setLauncher(pLauncher);
        getInstrumentation().waitForIdleSync();
        
        View view = act.getViewer().view();
        TouchUtils.clickView(SlotTestActivityTest.this, view);
        getInstrumentation().waitForIdleSync();
    }
    
 }
