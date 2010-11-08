package com.artcom.y60.data;

import java.io.ByteArrayInputStream;

import android.test.AndroidTestCase;

import com.artcom.y60.TestHelper;

public class TestStreamableContent extends AndroidTestCase {
    
    @SuppressWarnings("unused")
	private static final String LOG_TAG = "TestStreamableContent";
    
    public void testReadingDynamicSteamableContent() throws Exception {
        
        GenericStreamableContent data = new GenericStreamableContent();
        data.setContentType("text/html");
        byte[] content = "<br>".getBytes();
        data.openOutputStream().write(content);
        
        assertEquals("<br>", data.toString());
        TestHelper.assertInputStreamEquals("Getting stream once", new ByteArrayInputStream("<br>"
                .getBytes()), data.openInputStream());
        TestHelper.assertInputStreamEquals("Getting stream a second time",
                new ByteArrayInputStream("<br>".getBytes()), data.openInputStream());
    }
    
    public void testCreatingDynamicStreamableContent() throws Exception {
        GenericStreamableContent data = new GenericStreamableContent();
        data.setContentType("text/html");
        byte[] content = "<br>".getBytes();
        data.openOutputStream().write(content);
        
        assertEquals("Length should be as written data", 4, data.getStreamLength());
    }
}
