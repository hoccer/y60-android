package com.artcom.y60.gom;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpServerException;

public class GomHttpWrapper {

    public static HttpResponse updateOrCreateAttribute(Uri pUri, String pValue)
                    throws HttpClientException, HttpServerException, IOException {

        Map<String, String> formData = new HashMap<String, String>();
        formData.put(Constants.Gom.Keywords.ATTRIBUTE, pValue);

        return HttpHelper.putUrlEncoded(pUri.toString(), formData);
    }

    public static HttpResponse deleteAttribute(Uri pUri) throws HttpClientException,
                    HttpServerException, IOException {

        return HttpHelper.delete(pUri);
    }

    public static HttpResponse deleteNode(Uri pUri) throws HttpClientException,
                    HttpServerException, IOException {

        return HttpHelper.delete(pUri);
    }

    public static String getAttributeValue(Uri pAttrUrl) throws HttpClientException,
                    HttpServerException, IOException {

        try {
            JSONObject wrapper = HttpHelper.getJson(pAttrUrl.toString());
            JSONObject attr = wrapper.getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
            String value = attr.getString(Constants.Gom.Keywords.VALUE);

            return value;

        } catch (JSONException jsx) {

            throw new RuntimeException(jsx);
        }
    }

    public static HttpResponse createNode(String pNodeUrl) throws HttpClientException,
                    HttpServerException, IOException {

        return HttpHelper.putXML(pNodeUrl, "<node/>");
    }

    public static HttpResponse putNodeWithAttributes(String pNodeUrl, HashMap<String, String> pAttrs)
                    throws HttpClientException, HttpServerException, IOException {

        HashMap<String, String> formData = new HashMap<String, String>(pAttrs.size());
        for (String attrName : pAttrs.keySet()) {

            formData.put("attributes[" + attrName + "]", pAttrs.get(attrName));
        }
        return HttpHelper.putUrlEncoded(pNodeUrl, formData);

    }

}
