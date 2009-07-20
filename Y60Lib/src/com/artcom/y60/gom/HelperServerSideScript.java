package com.artcom.y60.gom;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpServerException;

// surprise: i was not written by a.h.
public class HelperServerSideScript {

    private static final String LOG_TAG = "HelperServerSideScript";

    public static JSONObject executeServerScript(String pJsStr, Map<String, String> pParams)
            throws JSONException, IOException, HttpClientException, HttpServerException {

        String params = HttpHelper.urlEncode(pParams);
        String uri = Constants.Gom.SCRIPT_RUNNER_URI + "?" + params;
        HttpResponse response = HttpHelper.post(uri, pJsStr, "text/javascript", "text/json",
                30 * 1000);
        String jsonStr = HttpHelper.extractBodyAsString(response.getEntity());

        return new JSONObject(jsonStr);
    }

    public static JSONObject executeServerScriptWithGet(String pScriptAttrName,
            Map<String, String> pParams) throws JSONException, IOException, HttpClientException,
            HttpServerException {

        String params = HttpHelper.urlEncode(pParams);
        String uri = Constants.Gom.SCRIPT_RUNNER_URI + Constants.Gom.SCRIPT_BASE_PATH + ":"
                + pScriptAttrName + "?" + params;
        Logger
                .v(
                        LOG_TAG,
                        "UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU\n\n",
                        uri);

        return new JSONObject(HttpHelper.getAsString(uri));
    }

    public static void postScriptToGom(String pAttrUri, String pScript, Context pContext) {

        Logger.v(LOG_TAG, "posting script to gom to: ", pAttrUri);

        try {
            GomHttpWrapper.updateOrCreateAttribute(Uri.parse(pAttrUri), pScript);

        } catch (HttpClientException e) {
            ErrorHandling.signalGomError(LOG_TAG, e, pContext);
        } catch (HttpServerException e) {
            ErrorHandling.signalGomError(LOG_TAG, e, pContext);
        } catch (IOException e) {
            ErrorHandling.signalGomError(LOG_TAG, e, pContext);
        }

    }

}
