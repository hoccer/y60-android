package com.artcom.y60;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class IoHelper {
    private static final String LOG_TAG = "IoHelper";

    public static String encodeUrl(String pString) {
        int separate = pString.lastIndexOf("/");
        String lastPart = pString.substring(separate + 1);
        try {
            lastPart = URLEncoder.encode(lastPart, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        lastPart = lastPart.replaceAll("\\+", "%20");
        return pString.substring(0, separate + 1) + lastPart;
    }

    public static String cutString(String pString, int pMaxWidth) {
        pMaxWidth -= 3;
        if (pString.length() > pMaxWidth) {
            return pString.substring(0, pMaxWidth) + "...";
        }
        return pString;
    }

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

    public static void deleteDir(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                deleteDir(new File(file, children[i]));
            }
        }
        file.delete();
    }

    public static String getValue(JSONObject jo, String name) throws JSONException {
        if (jo.has(name)) {
            return jo.getString(name);
        }
        return "";
    }

}
