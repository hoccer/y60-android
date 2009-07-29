package com.artcom.y60;

import android.content.Intent;
import android.test.ServiceTestCase;

public class Y60GomServiceTest extends ServiceTestCase<DemoY60GomService> {

    public Y60GomServiceTest() {
        super(DemoY60GomService.class);
    }

    public void testBindingToGom() {
        startService(new Intent("com.artcom.y60.DemoY60GomService"));
        assertNotNull(getService());

        getService().blockUntilBoundToGom();

        assertTrue("service should have bounded to gom", getService().isBoundToGom());
    }
}
