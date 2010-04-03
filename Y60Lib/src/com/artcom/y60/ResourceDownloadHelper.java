package com.artcom.y60;

import java.io.IOException;
import java.util.UUID;

import android.net.Uri;
import android.os.Bundle;

import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpHelper;
import com.artcom.y60.http.HttpProxyConstants;
import com.artcom.y60.http.HttpServerException;

public class ResourceDownloadHelper {
    
    static final String LOG_TAG = ResourceDownloadHelper.class.getName();
    
    public static Bundle downloadAndCreateResourceBundle(String pBasePath, String pUri)
            throws IOException, HttpClientException, HttpServerException {
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
