package com.artcom.y60;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import android.net.Uri;
import android.os.Bundle;

import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpProxyConstants;
import com.artcom.y60.http.HttpServerException;

public class ResourceBundleHelper {

    private static final String LOG_TAG = ResourceBundleHelper.class.getName();

    public static byte[] convertResourceBundleToByteArray(Bundle resourceDescription) {

        String resourcePath = resourceDescription
                .getString(HttpProxyConstants.LOCAL_RESOURCE_PATH_TAG);
        if (resourcePath == null) {
            return resourceDescription.getByteArray(HttpProxyConstants.BYTE_ARRAY_TAG);
        }

        byte[] buffer;
        try {
            File file = new File(resourcePath);
            FileInputStream stream = new FileInputStream(file);
            if (file.length() > Integer.MAX_VALUE) {
                throw new RuntimeException("file '" + file + "' is to big");
            }
            buffer = new byte[(int) file.length()];
            stream.read(buffer);
        } catch (IOException e) {
            Logger.e(LOG_TAG, "io error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return buffer;
    }

    public static Bundle createResourceBundle(String pBasePath, String pUri) throws IOException,
            HttpClientException, HttpServerException {
        long size = HttpHelper.getSize(pUri);
        Bundle newContent = new Bundle(2);
        newContent.putLong(HttpProxyConstants.SIZE_TAG, size);

        if (size > HttpProxyConstants.MAX_IN_MEMORY_SIZE) {
            String localResourcePath = pBasePath + UUID.randomUUID();

            HttpHelper.fetchUriToFile(pUri, localResourcePath);
            newContent.putString(HttpProxyConstants.LOCAL_RESOURCE_PATH_TAG, localResourcePath);
        } else {
            byte[] array = HttpHelper.getAsByteArray(Uri.parse(pUri));
            newContent.putByteArray(HttpProxyConstants.BYTE_ARRAY_TAG, array);
        }
        return newContent;
    }

}
