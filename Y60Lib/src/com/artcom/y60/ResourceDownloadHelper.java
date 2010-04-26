package com.artcom.y60;

import java.io.IOException;
import java.util.UUID;

import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpHelper;
import com.artcom.y60.http.HttpProxyConstants;
import com.artcom.y60.http.HttpServerException;

import android.net.Uri;
import android.os.Bundle;

public class ResourceDownloadHelper {

    static final String LOG_TAG = ResourceDownloadHelper.class.getName();

    public static Bundle downloadAndCreateResourceBundle(String pBasePath, String pUri)
            throws IOException, HttpClientException, HttpServerException {
        long size = HttpHelper.getSize(pUri);
        Bundle newContent = new Bundle(2);
        newContent.putLong(HttpProxyConstants.SIZE_TAG, size);

        if (size > HttpProxyConstants.MAX_IN_MEMORY_SIZE) {
            String localResourcePath = pBasePath + UUID.randomUUID();
            Logger.v(LOG_TAG, "before writing to sdcard: ", localResourcePath);
            HttpHelper.fetchUriToFile(pUri, localResourcePath);
            newContent.putString(HttpProxyConstants.LOCAL_RESOURCE_PATH_TAG, localResourcePath);
            Logger.v(LOG_TAG, "after writing to sdcard: ", localResourcePath);
        } else {
            byte[] array = HttpHelper.getAsByteArray(Uri.parse(pUri));
            newContent.putByteArray(HttpProxyConstants.BYTE_ARRAY_TAG, array);
        }
        return newContent;
    }

}
