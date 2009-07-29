package com.artcom.y60.gom;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.IoHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpException;
import com.artcom.y60.http.HttpServerException;

// surprise: i was not written by a.h.
public class HelperForServerSideScript {

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

    public static JSONObject executeServerScriptWithGet(InputStream pScript, String pScriptName,
            Map<String, String> pParams) throws JSONException, HttpException, IOException {

        String params = HttpHelper.urlEncode(pParams);
        String uri = Constants.Gom.SCRIPT_RUNNER_URI + Constants.Gom.SCRIPT_BASE_PATH + ":"
                + pScriptName + "?" + params;

        try {
            return new JSONObject(HttpHelper.getAsString(uri));
        } catch (HttpException e) {
            storeScriptedViewInGom(pScript, pScriptName);
            return new JSONObject(HttpHelper.getAsString(uri));
        } catch (IOException e) {
            storeScriptedViewInGom(pScript, pScriptName);
            return new JSONObject(HttpHelper.getAsString(uri));
        }
    }

    public static void storeScriptedViewInGom(InputStream jsStream, String pScriptName)
            throws IOException, HttpException {
        String script = IoHelper.readStringFromStream(jsStream);
        HelperForServerSideScript.putScriptToGom(Constants.Gom.URI + Constants.Gom.SCRIPT_BASE_PATH
                + ":" + pScriptName, script);
    }

    private static void putScriptToGom(String pAttrUri, String pScript) throws HttpException,
            IOException {

        Logger.v(LOG_TAG, "posting script to gom to: ", pAttrUri);

        GomHttpWrapper.updateOrCreateAttribute(Uri.parse(pAttrUri), pScript);

    }

}
