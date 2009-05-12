package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.StatusLine;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.RemoteException;

import com.artcom.y60.HTTPHelper;
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

    // Instance Variables ------------------------------------------------

    /** The attribute value */
    private String mValue;

    private String mNodePath;

    // Constructors ------------------------------------------------------

    /**
     * Used internally only. Use the methods of GomRepository to load resource
     * states.
     */
    protected GomAttribute(String pPath, GomProxyHelper pProxy) throws RemoteException {

        super(extractNameFromPath(pPath), pPath, pProxy);

        mValue = getProxy().getAttributeValue(pPath);
        mNodePath = pPath.substring(0, pPath.lastIndexOf(":"));
    }

    // Public Instance Methods -------------------------------------------

    public void refresh() throws RemoteException {
        getProxy().refreshEntry(getPath());
        mValue = getProxy().getAttributeValue(getPath());
    }

    public String getValue() {

        return mValue;
    }

    public GomNode getNode() {

        return getGomProxyHelper().getNode(mNodePath);
    }

    public void putValue(String pValue) {

        putOrCreateValue(getUri(), pValue);
        // update my data
        try {
            refresh();
        } catch (RemoteException e) {
            throw new RuntimeException("could not refresh gom attribute " + this.toString());
        }
    }

    public static void putOrCreateValue(Uri pUri, String pValue) {
        try {

            Map<String, String> formData = new HashMap<String, String>();
            formData.put(GomKeywords.ATTRIBUTE, pValue);

            StatusLine statusLine = HTTPHelper.putUrlEncoded(pUri.toString(), formData);

            //Logger.v(LOG_TAG, "PUT ", pUri, " with ", formData,"result code: ", statusLine.getStatusCode());

            if (statusLine.getStatusCode() >= 300) {

                // not want!
                throw new RuntimeException("HTTP server returned status code "
                        + statusLine.getStatusCode() + "!");
            }
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
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
            json.put(GomKeywords.ATTRIBUTE, attr);

            attr.put(GomKeywords.NAME, getName());
            attr.put(GomKeywords.NODE, mNodePath);
            attr.put(GomKeywords.VALUE, getValue());
            attr.put(GomKeywords.TYPE, "string");

            return json;

        } catch (JSONException e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Dereferences this attribute, if it contains a path to another resource.
     * 
     * @return the referenced resource, if resolution was successful
     * @throws GomResolutionFailedException
     *             if the resolution failed, e.g. because this attribute didn't
     *             point a resource
     */
    public GomEntry resolveReference() throws GomResolutionFailedException {

        GomEntry entry = getGomProxyHelper().getEntry(mValue);

        Logger.v(LOG_TAG, "resolved ", mValue, " to ", entry);

        return entry;
    }
}
