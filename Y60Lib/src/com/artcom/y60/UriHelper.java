package com.artcom.y60;

import android.net.Uri;

public class UriHelper {

    public static String join(String pBaseUri, String... pSegments) {

        Uri uri = Uri.parse(pBaseUri);
        for (String segment : pSegments) {

            segment = stripSlashes(segment);
            uri = Uri.withAppendedPath(uri, segment);
        }

        return uri.toString();
    }

    public static String stripSlashes(String pStr) {

        // slow but easy
        while (pStr.startsWith("/")) {

            pStr = pStr.substring(1);
        }
        while (pStr.endsWith("/")) {

            pStr = pStr.substring(0, pStr.length() - 1);
        }

        return pStr;
    }

    public static boolean isImageUri(String pUri) {
        return pUri.substring(pUri.lastIndexOf(".") + 1).equals("png")
                || pUri.substring(pUri.lastIndexOf(".") + 1).equals("jpg")
                || pUri.substring(pUri.lastIndexOf(".") + 1).equals("jpeg")
                || pUri.substring(pUri.lastIndexOf(".") + 1).equals("gif");
    }

}
