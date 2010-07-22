package com.artcom.y60.data;

import android.test.AndroidTestCase;

import com.artcom.y60.FileTestHelper;
import com.artcom.y60.FileTestHelper.TestFile;

public class AndroidStreamableContentTest extends AndroidTestCase {

    private static final String LOG_TAG = "AndroidStreamableContentTest";

    public void testCreatingEmptyObject() throws Exception {
        GenericAndroidStreamableContent streamableContent = new GenericAndroidStreamableContent(
                getContext().getContentResolver());

        assertNotNull(streamableContent);
    }

    public void testSettingFileScheme() throws Exception {
        TestFile testfile = new FileTestHelper().new TextFile("mannoman".getBytes(), "text/plain",
                "txt");

        GenericAndroidStreamableContent streamableContent = new GenericAndroidStreamableContent(
                getContext().getContentResolver());
        streamableContent.setDataUri(testfile.getFileSchemeUri());

        assertEquals(testfile.getFileSchemeUri(), streamableContent.getDataUri());
        assertEquals(testfile.mContent.length, streamableContent.getStreamLength());
        assertEquals(testfile.mContentType, streamableContent.getContentType());

    }
}
