package com.artcom.y60.hoccer;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;

public class PickableItem extends HashMap<String, String> {

    private static final long   serialVersionUID = 4009536136213288289L;
    private static final String LOG_TAG          = "PickableItem";

    public PickableItem(JSONObject jsonPickable) throws JSONException {
        put("uri", jsonPickable.getString("uri"));
        put("content_type", jsonPickable.getString("content_type"));
        put("filename", jsonPickable.getString("filename"));
    }

    public String getUri() {
        return get("uri");
    }

    public String getFilename() {
        return get("filename");
    }

    public String getContentType() {
        return get("content_type");
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
