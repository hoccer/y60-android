package com.artcom.y60.gom;

import java.util.Comparator;

import org.json.JSONObject;

import android.net.Uri;

/**
 * Represents the state of a GOM resource, i.e. a node or an attribute.
 * 
 * @author arne
 * 
 */
public abstract class GomEntry {

    // Constants ---------------------------------------------------------

    private static Comparator<GomEntry> BY_NAME_COMPARATOR = new Comparator<GomEntry>() {

                                                               public int compare(GomEntry pEntry1,
                                                                       GomEntry pEntry2) {

                                                                   return pEntry1
                                                                           .getName()
                                                                           .compareTo(
                                                                                   pEntry2
                                                                                           .getName());
                                                               }
                                                           };

    // Instance Variables ------------------------------------------------

    /** This resources path relative to the repository base URI */
    private String                      mPath;

    /** The name of this resource (the last element of the path) */
    private String                      mName;

    /** The complete URI of this resource */
    private Uri                         mUri;

    /** The GOM proxy helper this resource was loaded from */
    private GomProxyHelper              mProxyHelper;

    // Static Methods ----------------------------------------------------

    public static Comparator<GomEntry> byNameComparator() {

        return BY_NAME_COMPARATOR;
    }

    // Constructors ------------------------------------------------------

    protected GomEntry(String pName, String pPath, GomProxyHelper pProxy) {

        if (pName == null) {
            throw new IllegalArgumentException("Name can't be null!");
        }
        if (pPath == null) {
            throw new IllegalArgumentException("Path can't be null!");
        }
        if (pProxy == null) {
            throw new IllegalArgumentException("Repository can't be null!");
        }

        mName = pName;
        mPath = pPath;
        mProxyHelper = pProxy;
        mUri = Uri.withAppendedPath(pProxy.getBaseUri(), mPath);
    }

    // Public Instance Methods -------------------------------------------

    public abstract JSONObject toJson() throws GomEntryTypeMismatchException, GomEntryNotFoundException;

    public String getName() {

        return mName;
    }

    public String toString() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public Uri getUri() {

        return mUri;
    }

    public GomProxyHelper getGomProxyHelper() {

        return mProxyHelper;
    }

    /**
     * Helper method for assuring this resource is an attribute resp. to get a
     * meaningful error message otherwise.
     * 
     * @return
     * @throws GomEntryTypeMismatchException
     */
    public GomAttribute forceAttributeOrException() throws GomEntryTypeMismatchException {

        if (this instanceof GomAttribute) {

            return (GomAttribute) this;

        } else {

            throw new GomEntryTypeMismatchException("Entry '" + mPath + "' of repository '"
                    + mProxyHelper.getBaseUri() + "' is not an attribute!");
        }
    }

    /**
     * Helper method for assuring this resource is a node resp. to get a
     * meaningful error message otherwise.
     * 
     * @return
     * @throws GomEntryTypeMismatchException
     */
    public GomNode forceNodeOrException() throws GomEntryTypeMismatchException {

        if (this instanceof GomNode) {

            return (GomNode) this;

        } else {

            throw new GomEntryTypeMismatchException("Entry '" + mPath + "' of repository '"
                    + mProxyHelper.getBaseUri() + "' is not a node!");
        }
    }

    public boolean equals(Object pObj) {

        if (pObj != null && pObj instanceof GomEntry) {

            GomEntry other = (GomEntry) pObj;
            return mPath.equals(other.mPath) && mName.equals(other.mName)
                    && mUri.equals(other.mUri);
        } else {

            return false;
        }
    }

    @Override
    public int hashCode() {

        return mUri.hashCode();
    }
}
