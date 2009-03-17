package com.artcom.y60.infrastructure;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
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

    public GomRepository(Uri uri) {
        
        if (uri == null) {
            throw new IllegalArgumentException("Base URI can't be null!");
        }
        
        // TODO refactor me! plzzzz!
        try {
			mBaseUri = new URI(uri.toString());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
    }
    
   public GomRepository(URI uri) {
        
        if (uri == null) {
            throw new IllegalArgumentException("Base URI can't be null!");
        }
        
        // TODO refactor me! plzzzz!

		mBaseUri = uri;

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
    
	public GomNode getNode(Uri uri) {
		return getNode(uri.toString());
	}

    
    public GomAttribute getAttribute(String pPath) throws GomEntryTypeMismatchException {
        
        GomEntry entry = getEntry(pPath);
        
        return entry.forceAttributeOrException();
    }
    
    
    public URI getBaseUri() {
        
        return mBaseUri;
    }
    
    protected void setBaseUri(Uri baseUri) {
        
        try {
			mBaseUri = new URI(baseUri.toString());
		} catch (URISyntaxException e) {
			throw new RuntimeException("error while parsing new base uri");
		}
    }
}
