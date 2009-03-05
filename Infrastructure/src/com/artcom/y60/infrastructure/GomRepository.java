package com.artcom.y60.infrastructure;

import java.net.URI;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Encapsulates a GOM repository and can be used by clients to retrieve entries from the repositories.
 * Entries are always a temporary snapshot of a resource and are not updated if the resource changes.
 * 
 * @author arne
 */
public class GomRepository {

    // Constants ---------------------------------------------------------

    private static final String TAG = "GomRepository";
    
    
    
    // Instance Variables ------------------------------------------------

    private URI mBaseUri;
    
    
    
    // Constructors ------------------------------------------------------

    public GomRepository(URI pBaseUri) {
        
        if (pBaseUri == null) {
            throw new IllegalArgumentException("Base URI can't be null!");
        }
        
        mBaseUri = pBaseUri;
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public GomEntry getEntry(String pPath) {
        
        try {
            Log.v(TAG, "getEntry('"+pPath+"')");
            
            URI url = mBaseUri.resolve(pPath);
            Log.v(TAG, "complete url: "+url);
            
            JSONObject jsob  = HTTPHelper.getJson(url.toString());
            Log.v(TAG, "got json result:"+jsob.toString());
            
            return GomEntry.fromJson(jsob, this);
            
        } catch (JSONException jx) {
            
            Log.v(TAG, "parsing GOM data from JSON failed", jx);
            throw new RuntimeException(jx);
            
        }
    }
    
    
    public GomNode getNode(String pPath) throws GomEntryTypeMismatchException {
        
        GomEntry entry = getEntry(pPath);
        
        return entry.forceNodeOrException();
    }
    
    
    public GomAttribute getAttribute(String pPath) throws GomEntryTypeMismatchException {
        
        GomEntry entry = getEntry(pPath);
        
        return entry.forceAttributeOrException();
    }
    
    
    public URI getBaseUri() {
        
        return mBaseUri;
    }
}
