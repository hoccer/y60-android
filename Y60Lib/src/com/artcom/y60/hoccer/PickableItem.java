package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;

public class PickableItem {

    private static final String LOG_TAG = "PickableItem";
    private Map<String, String> mPickableItem;

    public PickableItem(JSONObject jsonPickable) throws JSONException {
        mPickableItem = new HashMap<String, String>();
        mPickableItem.put("uri", jsonPickable.getString("uri"));
        mPickableItem.put("content_type", jsonPickable.getString("content_type"));
        mPickableItem.put("filename", jsonPickable.getString("filename"));
    }

    public String getUri() {
        return mPickableItem.get("uri");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + "uri: " + getUri();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PickableItem)) {
            return false;
        }
        Logger.v(LOG_TAG, "comparing: " + o.toString() + " " + toString());
        return ((PickableItem) o).getUri().equals(this.getUri());
    }
}
