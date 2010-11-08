package com.artcom.y60;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.test.AndroidTestCase;

import com.artcom.y60.http.HttpHelper;

public class FileTestHelperTest extends AndroidTestCase {

    private static final String IMAGE_WEB_RESOURCE = "http://artcom.de/templates/artcom/css/images/artcom_rgb_screen_193x22.png";
    @SuppressWarnings("unused")
	private static final String LOG_TAG            = "HoccerTestHelperTest";

    public void testWritingImageStreamToContentResolver() throws Exception {

        byte[] originalData = HttpHelper.getAsByteArray(IMAGE_WEB_RESOURCE);

        ContentValues values = new ContentValues(3);
        values.put(Media.MIME_TYPE, "image/png");
        values.put(Media.DESCRIPTION, "Hocced!");
        values.put(Media.DISPLAY_NAME, "test.png");

        Uri contentUri = (getContext().getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
                values));
        OutputStream outStream = getContext().getContentResolver().openOutputStream(contentUri);
        IoHelper.writeDataToStream(originalData, outStream);

        InputStream inStream = getContext().getContentResolver().openInputStream(contentUri);
        TestHelper.assertInputStreamEquals("data should be the same", new ByteArrayInputStream(
                originalData), inStream);
    }

}
