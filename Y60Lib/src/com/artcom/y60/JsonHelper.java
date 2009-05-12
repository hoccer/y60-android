package com.artcom.y60;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonHelper {
    
    // Static Methods ----------------------------------------------------

    /**
     * Convenience method to navigate to a down a path on a JSON tree.
     */
    public static JSONObject get(JSONObject pObj, String pPath) throws JSONException {
        
        String[] keys = pPath.split("/");
        JSONObject cur = pObj;
        
        for (String key: keys) {
            
            cur = cur.getJSONObject(key);
        }
        
        return cur;
    }
    
    
    /**
     * Peels an object which the caller is actually interested in from a wrapper object (similar
     * to stripping the containing tag from a complex type in XML), e.g. if you HTTP-GET a customer
     * object as <code>{ "customer ": { "name": "John Doe" } }</code>, a 
     * <code>getMemberOrSelf(customer, "customer")</code> would return
     * <code>{ "name": "John Doe" }</code> and strip the "customer" object.
     * If the given object doesn't have a member for the given key which is a JSONObject, the given
     * object itself is returned. Thus invoking getMemberOrSelf in the above example on a result, again
     * using "customer" as key, would return that same object, i.e. it's idempotent if the key is always
     * the same. 
     * 
     * @param pObj the JSONOBject, possibly a wrapper
     * @param pKey the key of the object the caller is actually interested in
     * 
     * @return 
     * @throws JSONException if something went wrong while processing the JSON data
     */
    public static JSONObject getMemberOrSelf(JSONObject pObj, String pKey) throws JSONException {
        
        if (pObj.has(pKey)) {
            
            Object inner = pObj.get(pKey);
            if (inner instanceof JSONObject) {
             
                return (JSONObject)inner;
            }
        }
            
        return pObj;
    }

}
