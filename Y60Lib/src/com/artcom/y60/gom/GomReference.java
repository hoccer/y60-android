package com.artcom.y60.gom;

import android.net.Uri;

import com.artcom.y60.Logger;
import com.artcom.y60.UriHelper;

/**
 * Instances of this class should mainly be used for testing since it's not
 * optimized with regard to object creation.
 * 
 * @author arne
 * 
 */
public class GomReference {

    private static final String LOG_TAG = "GomReference";

    // Static Methods ----------------------------------------------------

    public static boolean isSelfOrAncestorOf(String pAncestorPath, String pDescendantPath) {

        return pDescendantPath.startsWith(pAncestorPath + ":")
                || pDescendantPath.startsWith(pAncestorPath + "/")
                || pDescendantPath.equals(pAncestorPath);
    }

    public static String lastSegment(String pPath) {

        int idx = pPath.lastIndexOf(":");
        if (idx == -1) {
            idx = pPath.lastIndexOf("/");
        }

        return pPath.substring(idx + 1);
    }

    public static String parentPath(String pPath) {

        return pPath.substring(0, pPath.length() - lastSegment(pPath).length() - 1);
    }

    // Instance Variables ------------------------------------------------

    private String  mName;

    private Uri     mUrl;

    private String  mParentPath;

    private Uri     mBaseUrl;

    private boolean mIsAttr;

    // Constructors ------------------------------------------------------

    public GomReference(String pEntryUrlStr) {

        Logger.v(LOG_TAG, "creating gom reference from string '", pEntryUrlStr, "'");

        mUrl = Uri.parse(pEntryUrlStr);

        int colonIdx = pEntryUrlStr.indexOf(":");
        int nonSlashIdx = colonIdx + 1;
        while (pEntryUrlStr.charAt(nonSlashIdx) == '/') {

            nonSlashIdx += 1;
        }

        int pathStartIdx = pEntryUrlStr.indexOf("/", nonSlashIdx);
        mBaseUrl = Uri.parse(pEntryUrlStr.substring(0, pathStartIdx));

        mIsAttr = false;

        Logger.v(LOG_TAG, "pathStartIdx = ", pathStartIdx, ", string length = ", pEntryUrlStr
                .length());

        if (pathStartIdx == -1 || pathStartIdx == pEntryUrlStr.length() - 1) {

            // it's the root node
            mParentPath = null;
            mName = "/";

        } else {

            int afterParentEndIdx = pEntryUrlStr.lastIndexOf("/");
            if (pEntryUrlStr.indexOf(":", afterParentEndIdx) > -1) {

                afterParentEndIdx = pEntryUrlStr.lastIndexOf(":");
                mIsAttr = true;
            }

            mParentPath = pEntryUrlStr.substring(pathStartIdx, afterParentEndIdx);
            if ("".equals(mParentPath)) {
                mParentPath = "/";
            }
            mName = pEntryUrlStr.substring(afterParentEndIdx + 1);
        }

        Logger.v(LOG_TAG, "name: ", mName);
        Logger.v(LOG_TAG, "base url: ", mBaseUrl);
        Logger.v(LOG_TAG, "is attribute?: ", mIsAttr);
        Logger.v(LOG_TAG, "parent path: ", mParentPath);
        Logger.v(LOG_TAG, "url: ", mUrl);
    }

    public GomReference(Uri pEntryUrl) {

        this(pEntryUrl.toString());
    }

    public GomReference(Uri pBaseUri, String... pPathSegments) {

        this(UriHelper.join(pBaseUri.toString(), pPathSegments));
    }

    public GomReference(String pBaseUriStr, String... pPathSegments) {

        this(UriHelper.join(pBaseUriStr, pPathSegments));
    }

    // Public Instance Methods -------------------------------------------

    public String name() {

        return mName;
    }

    public String path() {

        if (isRoot()) {
            return "/";
        } else {
            if (mIsAttr) {
                return mParentPath + ":" + mName;

            } else {

                return mParentPath + (mParentPath.endsWith("/") ? "" : "/") + mName;
            }
        }
    }

    public Uri url() {

        return mUrl;
    }

    public Uri baseUrl() {

        return mBaseUrl;
    }

    public GomReference parent() {

        if (isRoot()) {
            return null;
        } else {
            return new GomReference(mBaseUrl + mParentPath);
        }
    }

    public boolean isRoot() {

        return mParentPath == null;
    }

    public boolean isAttribute() {

        return mIsAttr;
    }

    public boolean isNode() {

        return !mIsAttr;
    }

    public GomReference subNode(String pName) {

        if (isAttribute()) {
            throw new RuntimeException("Can't create reference to sub node " + pName
                    + " of attribute " + path());
        }

        return new GomReference(url(), pName);
    }

    @Override
    public String toString() {

        return mUrl.toString();
    }

    @Override
    public int hashCode() {

        return mUrl.hashCode();
    }

    @Override
    public boolean equals(Object pObj) {

        return (pObj != null) && (pObj instanceof GomReference)
                && ((GomReference) pObj).mUrl.equals(mUrl);
    }

}
