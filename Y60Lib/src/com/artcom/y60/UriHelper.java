package com.artcom.y60;

import android.net.Uri;

public class UriHelper {
    
    public static String join(String pBaseUri, String... pSegments) {
        
        Uri uri = Uri.parse(pBaseUri);
        for (String segment: pSegments) {
            
            uri = Uri.withAppendedPath(uri, segment);
        }
        
        return uri.toString();
    }

}
