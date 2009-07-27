package com.artcom.y60;

import java.io.File;
import java.io.FileReader;

import android.test.ActivityInstrumentationTestCase;

public class TestError extends ActivityInstrumentationTestCase<ErrorPresentationActivity> {

    public TestError() {
        super("com.artcom.y60", ErrorPresentationActivity.class);

    }

    public void testUserError() throws Exception {
        assertNotNull(getActivity());

        Exception e = new Exception("Don't panic");
        ErrorHandling.signalError(this.getClass().getSimpleName(), e, getActivity(),
                ErrorHandling.Category.UNSPECIFIED);

        String errorFileName = "/sdcard/error_log.txt";
        FileReader fr = new FileReader(errorFileName);
        char[] inputBuffer = new char[255];
        fr.read(inputBuffer);
        String errorOutput = new String(inputBuffer);

        assertTrue("error message is stored on sdcard", errorOutput.contains("Don't panic"));
        fr.close();
        assertTrue("error file should be deleted", (new File(errorFileName)).delete());
    }
}
