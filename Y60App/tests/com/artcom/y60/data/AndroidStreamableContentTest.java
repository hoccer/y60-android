package com.artcom.y60.data;

import android.test.AndroidTestCase;

public class AndroidStreamableContentTest extends AndroidTestCase {

    private static final String LOG_TAG = "AndroidStreamableContentTest";

    public void testCreatingEmptyObject() throws Exception {
        GenericAndroidStreamableContent streamableContent = new GenericAndroidStreamableContent(
                getContext().getContentResolver());

        assertNotNull(streamableContent);
    }

    public void testSettingFileScheme() throws Exception {
        GenericAndroidStreamableContent streamableContent = new GenericAndroidStreamableContent(
                getContext().getContentResolver());

        assertNotNull(streamableContent);
    }
}
