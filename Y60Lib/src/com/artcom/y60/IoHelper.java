package com.artcom.y60;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IoHelper {

    public static String readStringFromStream(InputStream pInStream) throws IOException {

        InputStreamReader isr = new InputStreamReader(pInStream);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            builder.append(line + "\n");
        }

        br.close();
        return builder.toString();
    }

    public static byte[] readByteArrayFromStream(InputStream pInStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int in;
        while ((in = pInStream.read()) != -1) {
            baos.write(in);
        }
        return baos.toByteArray();
    }

    public static void writeInputStreamToFile(InputStream pInputStream, String pFilename)
            throws IOException {
        File tmpFile = new File(pFilename);
        FileOutputStream fos = new FileOutputStream(tmpFile);

        byte[] buffer = new byte[0xFFFF];
        for (int len; (len = pInputStream.read(buffer)) != -1;) {
            fos.write(buffer, 0, len);
        }
        fos.close();
    }

}
