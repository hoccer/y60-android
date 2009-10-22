package com.artcom.y60;

import android.test.AndroidTestCase;

public class UriHelperTest extends AndroidTestCase {

    public void testIsImageUri() throws Exception {

        String testPng = "test.png";
        String testJpg = "http://test-uri.123.fakestreet.name.jpg";
        String testJpeg = "hans_peter_sieht_ein_auto.jpeg";
        String testGif = "hans_peter_sieht@ein-auto.gif";

        assertTrue("png should be detected", UriHelper.isImageUri(testPng));
        assertTrue("jpg should be detected", UriHelper.isImageUri(testJpg));
        assertTrue("jpeg should be detected", UriHelper.isImageUri(testJpeg));
        assertTrue("gif should be detected", UriHelper.isImageUri(testGif));

    }

    public void testIsRawUri() throws Exception {

        String testPng = "test.png";
        String testJpg = "http://test-uri.123.fakestreet.name.mp3";
        String testJpeg = "hans_peter_sieht_ein_auto.mp4";
        String testGif = "hans_peter_sieht@ein-auto.mov";

        assertTrue("png should be detected", UriHelper.isRawUri(testPng));
        assertTrue("jpg should be detected", UriHelper.isRawUri(testJpg));
        assertTrue("jpeg should be detected", UriHelper.isRawUri(testJpeg));
        assertTrue("gif should be detected", UriHelper.isRawUri(testGif));

    }
}
