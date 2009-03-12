package com.artcom.y60.infrastructure;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 * Represents the state of an attribute resource in the GOM. Some attributes contain references
 * (i.e. paths) to other resources which can be dereferenced by calling resolveReference() on an
 * attribute.
 * 
 * @author arne
 */
public class GomAttribute extends GomEntry {

    // Constants ---------------------------------------------------------

    private final static String TAG = "GomAttribute";
    
    
    

    // Instance Variables ------------------------------------------------

    /** The attribute value */
    private String mValue;
    
    
    
    // Static Methods ----------------------------------------------------

    /** Constructs an attribute from a JSON representation */
    static GomAttribute fromJson(JSONObject pJson, GomRepository pRepos) throws JSONException {
        
        JSONObject jAttr = JsonHelper.getMemberOrSelf(pJson, GomKeywords.NODE); 
        
        String name  = jAttr.getString(GomKeywords.NAME);
        String path  = jAttr.getString(GomKeywords.NODE) + ":" + name;
        String value = jAttr.getString(GomKeywords.VALUE);
        
        return new GomAttribute(name, value, path, pRepos);
    }
    
    

    // Constructors ------------------------------------------------------

    /**
     * Used internally only. Use the methods of GomRepository to load resource
     * states.
     */
    protected GomAttribute(String pName, String pValue, String pPath, GomRepository pRepos) {
        
        super(pName, pPath, pRepos);
        
        if (pValue == null) {
            throw new IllegalArgumentException("Value can't be null!");
        }
        mValue = pValue;
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public String getValue() {
        
        return mValue;
    }
    
    
    /** 
     * Dereferences this attribute, if it contains a path to another resource.
     * 
     * @return the referenced resource, if resolution was successful
     * @throws GomResolutionFailedException if the resolution failed, e.g. because
     *                                      this attribute didn't point a resource
     */
    public GomEntry resolveReference() throws GomResolutionFailedException {
        
        GomEntry entry = getRepository().getEntry(mValue);
        
        Log.v(TAG, "resolved "+mValue+" to "+entry.toString());
        
        return entry;
    }
}
