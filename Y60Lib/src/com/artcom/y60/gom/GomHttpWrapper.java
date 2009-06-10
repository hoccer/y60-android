package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;

public class GomHttpWrapper {

    public static void updateOrCreateAttribute(Uri pUri, String pValue) {
        try {

            Map<String, String> formData = new HashMap<String, String>();
            formData.put(Constants.Gom.Keywords.ATTRIBUTE, pValue);

            StatusLine statusLine = HttpHelper.putUrlEncoded(pUri.toString(), formData)
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

        return HttpHelper.delete(pUri);
    }

    public static HttpResponse deleteNode(Uri pUri) {

        return HttpHelper.delete(pUri);
    }

    public static String getAttributeValue(Uri pAttrUrl) {

        try {
            JSONObject wrapper = HttpHelper.getJson(pAttrUrl.toString());
            JSONObject attr = wrapper.getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
            String value = attr.getString(Constants.Gom.Keywords.VALUE);

            return value;

        } catch (JSONException jsx) {

            throw new RuntimeException(jsx);
        }
    }

    public static String createNode(String pNodeUrl) {

        return HttpHelper.putXML(pNodeUrl, "<node/>");
    }

    public static HttpResponse putNodeWithAttributes(String pNodeUrl,
            HashMap<String, String> pAttrs) {

        HashMap<String, String> formData = new HashMap<String, String>(pAttrs.size());
        for (String attrName : pAttrs.keySet()) {

            formData.put("attributes[" + attrName + "]", pAttrs.get(attrName));
        }
        return HttpHelper.putUrlEncoded(pNodeUrl, formData);

    }

}
