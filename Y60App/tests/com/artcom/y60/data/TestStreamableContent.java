package com.artcom.y60.data;

import java.io.ByteArrayInputStream;

import android.test.AndroidTestCase;

import com.artcom.y60.TestHelper;

public class TestStreamableContent extends AndroidTestCase {

    private static final String LOG_TAG = "TestStreamableContent";

    public void testReadingDynamicSteamableContent() throws Exception {

        DynamicStreamableContent data = new DynamicStreamableContent();
        data.setContentType("text/html");
        byte[] content = "<br>".getBytes();
        data.write(content, 0, content.length);

        assertEquals("<br>", data.toString());
        TestHelper.assertInputStreamEquals("Getting stream once", new ByteArrayInputStream("<br>"
                .getBytes()), data.getStream());
        TestHelper.assertInputStreamEquals("Getting stream a second time",
                new ByteArrayInputStream("<br>".getBytes()), data.getStream());
    }

    public void testCreatingDynamicStreamableContent() throws Exception {
        DynamicStreamableContent data = new DynamicStreamableContent();
        data.setContentType("text/html");
        byte[] content = "<br>".getBytes();
        data.write(content, 0, content.length);

        assertEquals("Length should be as written data", 4, data.getStreamLength());
    }
}
