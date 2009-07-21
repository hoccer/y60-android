package com.artcom.y60;

import android.app.Instrumentation.ActivityMonitor;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase;
import android.test.suitebuilder.annotation.Suppress;

public class TestError extends ActivityInstrumentationTestCase<ErrorPresentationActivity> {

    public TestError() {
        super("com.artcom.y60", ErrorPresentationActivity.class);

    }

    // currently we show errors as a notification with a "link" to the error
    // presentation
    @Suppress
    public void testUserError() {
        assertNotNull(getActivity());

        IntentFilter filter = new IntentFilter("y60.intent.ERROR_PRESENTATION");
        ActivityMonitor monitor = new ActivityMonitor(filter, null, true);
        getInstrumentation().addMonitor(monitor);

        Exception e = new Exception("Don't panic");
        ErrorHandling.signalError(this.getClass().getSimpleName(), e, getActivity(),
                ErrorHandling.Category.UNSPECIFIED);

        assertEquals("no activity launced", 1, monitor.getHits());
        getInstrumentation().removeMonitor(monitor);
    }

}
