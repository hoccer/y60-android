package com.artcom.y60.gom;

import org.json.JSONObject;

public interface GomObserver {

    public void onEntryCreated(String pPath, JSONObject pData);
    
    public void onEntryUpdated(String pPath, JSONObject pData);
    
    public void onEntryDeleted(String pPath, JSONObject pData);
}
