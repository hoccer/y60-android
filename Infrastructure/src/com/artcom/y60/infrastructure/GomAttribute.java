package com.artcom.y60.infrastructure;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.StatusLine;
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
    
    private String mNodePath;
    
    
    
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
        mValue    = pValue;
        mNodePath = pPath.substring(0, pPath.lastIndexOf(":"));
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public String getValue() {
        
        return mValue;
    }
    
    
    public GomNode getNode() {
        
        return getRepository().getNode(mNodePath);
    }
    
    
    public void putValue(String pValue)  {
        
        String oldValue = mValue;
        try {
            
            mValue = pValue;
            String uri = getUri().toString();

            Map<String, String> formData = new HashMap<String, String>();
            formData.put(GomKeywords.ATTRIBUTE, getValue());
            
            StatusLine sline  = HTTPHelper.putUrlEncoded(uri, formData);
            
            Log.v(TAG, "result code: "+sline.getStatusCode());
            
            if (sline.getStatusCode() >= 300) {
                
                // not want!
                throw new RuntimeException("HTTP server returned status code "+sline.getStatusCode()+"!");
            }
            
        } catch (Exception e) {
            
            // roll back
            mValue = oldValue;
            
            throw new RuntimeException(e);
        }
    }
    
    
    public JSONObject toJson() {
        
//      { "attribute": {
//          "name": <name>,
//          "node": <node-path>,
//          "value": <value>,
//          "type": "string"
//      } }
        
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
     * @throws GomResolutionFailedException if the resolution failed, e.g. because
     *                                      this attribute didn't point a resource
     */
    public GomEntry resolveReference() throws GomResolutionFailedException {
        
        GomEntry entry = getRepository().getEntry(mValue);
        
        Log.v(TAG, "resolved "+mValue+" to "+entry.toString());
        
        return entry;
    }
}
