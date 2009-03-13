package com.artcom.y60.infrastructure;

import java.net.URI;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 * Represents the state of a GOM resource, i.e. a node or an attribute.
 * 
 * @author arne
 *
 */
public abstract class GomEntry {

    // Constants ---------------------------------------------------------

    private static Comparator<GomEntry> BY_NAME_COMPARATOR = new Comparator<GomEntry>() {

        public int compare(GomEntry pEntry1, GomEntry pEntry2) {

            return pEntry1.getName().compareTo(pEntry2.getName());
        }
    };
    
    

    // Instance Variables ------------------------------------------------

    /** This resources path relative to the repository base URI */
    private String mPath;
    
    /** The name of this resource (the last element of the path) */
    private String mName;
    
    /** The complete URI of this resource */
    private URI mUri;
    
    /** The repository this resource was loaded from */
    private GomRepository mRepos;
    
    
    
    // Static Methods ----------------------------------------------------

    public static Comparator<GomEntry> byNameComparator() {
        
        return BY_NAME_COMPARATOR;
    }
    
    /**
     * Constructs a new GomEntry from a JSON representation. Used internally only. Use the
     * methods of class GomRepository to retrieve GOM entries.
     */
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

    protected GomEntry(String pName, String pPath, GomRepository pRepos) {
        
        if (pName == null) {
            throw new IllegalArgumentException("Name can't be null!");
        }
        if (pPath == null) {
            throw new IllegalArgumentException("Path can't be null!");
        }
        if (pRepos == null) {
            throw new IllegalArgumentException("Repository can't be null!");
        }
        
        mName  = pName;
        mPath  = pPath;
        mRepos = pRepos;
        mUri   = pRepos.getBaseUri().resolve(mPath);
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public abstract JSONObject toJson();
    
    public String getName() {
        
        return mName;
    }
    
    public String toString() {
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
    
    
    /**
     * Helper method for assuring this resource is an attribute resp. to get
     * a meaningful error message otherwise.
     * 
     * @return
     */
    public GomAttribute forceAttributeOrException() {
        
        if (this instanceof GomAttribute) {
            
            return (GomAttribute)this;
            
        } else {
            
            throw new GomEntryTypeMismatchException("Entry '"+mPath+"' of repository '"+mRepos.getBaseUri()+"' is not an attribute!");
        }
    }
    

    /**
     * Helper method for assuring this resource is a node resp. to get
     * a meaningful error message otherwise.
     * 
     * @return
     */
    public GomNode forceNodeOrException() {
        
        if (this instanceof GomNode) {
            
            return (GomNode)this;
            
        } else {
            
            throw new GomEntryTypeMismatchException("Entry '"+mPath+"' of repository '"+mRepos.getBaseUri()+"' is not a node!");
        }
    }
}
