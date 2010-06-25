package com.artcom.y60;

import junit.framework.TestCase;

import com.artcom.y60.http.HttpHelper;

public class IoHelperTest extends TestCase {

    private static final String LOG_TAG = "IoHelperTest";
    private static final String FILE_1  = "/sdcard/test_file_"
                                                + String.valueOf(System.currentTimeMillis());
    private static final String FILE_2  = "/sdcard/test_file_"
                                                + String.valueOf(System.currentTimeMillis());
    private static final String RES_URI = "http://www.artcom.de/templates/artcom/css/images/artcom_rgb_screen_193x22.png";

    public void testWriteWebByteArrayToFile() throws Exception {

        HttpHelper.fetchUriToFile(RES_URI, FILE_1);
        byte[] file1Bytes = IoHelper.getByteArrayFromFile(FILE_1);

        byte[] file2BytesTmp = HttpHelper.getAsByteArray(RES_URI);
        IoHelper.writeByteArrayToFile(file2BytesTmp, FILE_2);
        byte[] file2Bytes = IoHelper.getByteArrayFromFile(FILE_2);

        assertTrue(
                "directly written file and downloaded as byte and than stored file should be equal",
                IoHelper.areWeEqual(file1Bytes, file2Bytes));
    }
}
