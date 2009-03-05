package com.artcom.y60.infrastructure;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class GomAttribute extends GomEntry {

    // Constants ---------------------------------------------------------

    private final static String TAG = "GomAttribute";
    
    
    

    // Instance Variables ------------------------------------------------

    private String mValue;
    
    
    
    // Static Methods ----------------------------------------------------

    static GomAttribute fromJson(JSONObject pJson, GomRepository pRepos) throws JSONException {
        
        JSONObject jAttr = JsonHelper.getMemberOrSelf(pJson, GomKeywords.NODE); 
        
        String name  = jAttr.getString(GomKeywords.NAME);
        String path  = jAttr.getString(GomKeywords.NODE) + ":" + name;
        String value = jAttr.getString(GomKeywords.VALUE);
        
        return new GomAttribute(name, value, path, pRepos);
    }
    
    

    // Constructors ------------------------------------------------------

    GomAttribute(String pName, String pValue, String pPath, GomRepository pRepos) {
        
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
    
    
    public GomEntry resolveReference() throws GomResolutionFailedException {
        
        GomEntry entry = getRepository().getEntry(mValue);
        
        Log.v(TAG, "resolved "+mValue+" to "+entry.toString());
        
        return entry;
    }
}
