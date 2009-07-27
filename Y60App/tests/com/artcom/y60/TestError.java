package com.artcom.y60;

import java.io.File;
import java.io.FileReader;

import android.test.AssertionFailedError;

public class TestError extends Y60ActivityInstrumentationTest<ErrorPresentationActivity> {

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

        try {
            assertNoErrorLogOnSdcard();
            fail();
        } catch (AssertionFailedError afe) {
        }

        File f = new File("/sdcarc/error_log.txt");
        assertFalse("there should be no error log on sdcard", f.exists());
    }

    public void testMultipleSignalError() throws Exception {
        assertNotNull(getActivity());

        File f = new File("/sdcard/error_log.txt");

        Exception e = new Exception("Don't panic");
        ErrorHandling.signalError(this.getClass().getSimpleName(), e, getActivity(),
                ErrorHandling.Category.UNSPECIFIED);

        Exception e2 = new Exception("Do pancakes");
        ErrorHandling.signalError(this.getClass().getSimpleName(), e2, getActivity(),
                ErrorHandling.Category.UNSPECIFIED);

        FileReader fr = new FileReader(f);
        char[] inputBuffer = new char[(int) f.length()];
        fr.read(inputBuffer);
        String errorOutput = new String(inputBuffer);
        fr.close();

        assertTrue("first error is not saved on sdcard", errorOutput.contains("Don't panic"));
        assertTrue("second error is not saved on sdcard", errorOutput.contains("Do pancakes"));

        assertTrue("error file should be deleted", f.delete());
        assertFalse("there should be no error log on sdcard", f.exists());
    }
}
