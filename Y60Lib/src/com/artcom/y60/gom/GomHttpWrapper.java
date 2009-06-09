package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.HTTPHelper;

public class GomHttpWrapper {

    public static void updateOrCreateAttribute(Uri pUri, String pValue) {
        try {

            Map<String, String> formData = new HashMap<String, String>();
            formData.put(Constants.Gom.Keywords.ATTRIBUTE, pValue);

            StatusLine statusLine = HTTPHelper.putUrlEncoded(pUri.toString(), formData)
                    .getStatusLine();

            // Logger.v(LOG_TAG, "PUT ", pUri, " with ",
            // formData,"result code: ", statusLine.getStatusCode());

            if (statusLine.getStatusCode() >= 300) {

                // not want!
                throw new RuntimeException("HTTP server returned status code "
                        + statusLine.getStatusCode() + "!");
            }
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    public static HttpResponse deleteAttribute(Uri pUri) {

        return HTTPHelper.delete(pUri);
    }

    public static HttpResponse deleteNode(Uri pUri) {

        return HTTPHelper.delete(pUri);
    }

    public static String getAttributeValue(Uri pAttrUrl) {

        try {
            JSONObject wrapper = HTTPHelper.getJson(pAttrUrl.toString());
            JSONObject attr = wrapper.getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
            String value = attr.getString(Constants.Gom.Keywords.VALUE);

            return value;

        } catch (JSONException jsx) {

            throw new RuntimeException(jsx);
        }
    }
}
