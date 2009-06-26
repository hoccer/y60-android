package com.artcom.y60.gom;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.RemoteException;

import com.artcom.y60.Constants;
import com.artcom.y60.Logger;

/**
 * Represents the state of an attribute resource in the GOM. Some attributes
 * contain references (i.e. paths) to other resources which can be dereferenced
 * by calling resolveReference() on an attribute.
 * 
 * @author arne
 */
public class GomAttribute extends GomEntry {

    // Constants ---------------------------------------------------------

    private final static String LOG_TAG = "GomAttribute";

    // Static Methods ----------------------------------------------------

    public static String extractNameFromPath(String pPath) {

        return pPath.substring(pPath.lastIndexOf(":") + 1);
    }

    /** The attribute value */
    private String mValue;

    private String mNodePath;

    // Constructors ------------------------------------------------------

    /**
     * Used internally only. Use the methods of GomRepository to load resource
     * states.
     */
    protected GomAttribute(String pPath, GomProxyHelper pProxy, String pValue)
            throws RemoteException {

        super(extractNameFromPath(pPath), pPath, pProxy);

        mNodePath = pPath.substring(0, pPath.lastIndexOf(":"));
        mValue = pValue;
    }

    // Public Instance Methods -------------------------------------------

    public String getValue() {

        return mValue;
    }

    public GomNode getNode() throws GomEntryTypeMismatchException {

        return getGomProxyHelper().getNode(mNodePath);
    }

    public void putValue(String pValue) {

        GomHttpWrapper.updateOrCreateAttribute(getUri(), pValue);
        mValue = pValue;
    }

    public JSONObject toJson() {

        // { "attribute": {
        // "name": <name>,
        // "node": <node-path>,
        // "value": <value>,
        // "type": "string"
        // } }

        try {

            JSONObject json = new JSONObject();

            JSONObject attr = new JSONObject();
            json.put(Constants.Gom.Keywords.ATTRIBUTE, attr);

            attr.put(Constants.Gom.Keywords.NAME, getName());
            attr.put(Constants.Gom.Keywords.NODE, mNodePath);
            attr.put(Constants.Gom.Keywords.VALUE, getValue());
            attr.put(Constants.Gom.Keywords.TYPE, "string");

            return json;

        } catch (JSONException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object pObj) {

        if ((pObj != null) && (pObj instanceof GomAttribute) && super.equals(pObj)) {

            GomAttribute other = (GomAttribute) pObj;
            return mValue.equals(other.mValue);

        } else {

            return false;
        }
    }

    /**
     * Dereferences this attribute, if it contains a path to another resource.
     * 
     * @return the referenced resource, if resolution was successful
     * @throws GomResolutionFailedException
     *             if the resolution failed, e.g. because this attribute didn't
     *             point a resource
     * @throws GomEntryNotFoundException
     * @throws GomEntryTypeMismatchException
     */
    public GomEntry resolveReference() throws GomResolutionFailedException,
            GomEntryTypeMismatchException, GomEntryNotFoundException {

        GomEntry entry = getGomProxyHelper().getEntry(mValue);

        Logger.v(LOG_TAG, "resolved ", mValue, " to ", entry);

        return entry;
    }
}
