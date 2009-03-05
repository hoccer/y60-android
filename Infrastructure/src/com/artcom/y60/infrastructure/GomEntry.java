package com.artcom.y60.infrastructure;

import java.net.URI;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 * If needed, this class might be enriched e.g. by adding
 *  - getUri to retrieve the GOM URI of this entry
 *  - getParent to retrieve the parent node
 *  - getRepository to retrieve this entry's repository
 * 
 * @author arne
 *
 */
public abstract class GomEntry {

    // Instance Variables ------------------------------------------------

    private String mPath;
    
    private String mName;
    
    private URI mUri;
    
    private GomRepository mRepos;
    
    
    
    // Static Methods ----------------------------------------------------

    static GomEntry fromJson(JSONObject pRoot, GomRepository pRepos) throws JSONException {
        
        JSONObject content = JsonHelper.getMemberOrSelf(pRoot, GomKeywords.ATTRIBUTE);

        if (content != pRoot) {
            
            GomAttribute attr = GomAttribute.fromJson(content, pRepos); 
            Log.v("GomEntry", "GomEntry.fromJson is returning attribute '"+attr.getPath()+"' = '"+attr.getValue()+"'");
            return attr;
            
        } else {
         
            Log.v("GomEntry", "GomEntry.fromJson returning a node");
            return GomNode.fromJson(pRoot, pRepos);
        }
    }
    

    
    // Constructors ------------------------------------------------------

    GomEntry(String pName, String pPath, GomRepository pRepos) {
        
        if (pName == null) {
            throw new IllegalArgumentException("Name can't be null!");
        }
        if (pPath == null) {
            throw new IllegalArgumentException("Path can't be null!");
        }
        if (pRepos == null) {
            throw new IllegalArgumentException("Path can't be null!");
        }
        
        mName  = pName;
        mPath  = pPath;
        mRepos = pRepos;
        mUri   = pRepos.getBaseUri().resolve(mPath);
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public String getName() {
        
        return mName;
    }
    
    
    public String getPath() {
        
        return mPath;
    }
    
    
    public URI getUri() {
        
        return mUri;
    }
    
    
    public GomRepository getRepository() {
        
        return mRepos;
    }
    
    
    public GomAttribute forceAttributeOrException() {
        
        if (this instanceof GomAttribute) {
            
            return (GomAttribute)this;
            
        } else {
            
            throw new GomEntryTypeMismatchException("Entry '"+mPath+"' of repository '"+mRepos.getBaseUri()+"' is not an attribute!");
        }
    }
    

    public GomNode forceNodeOrException() {
        
        if (this instanceof GomNode) {
            
            return (GomNode)this;
            
        } else {
            
            throw new GomEntryTypeMismatchException("Entry '"+mPath+"' of repository '"+mRepos.getBaseUri()+"' is not a node!");
        }
    }
    

    
    // Protected Instance Methods ----------------------------------------

    protected void setPath(String pPath) {
        
        mPath = pPath;
    }
}
