package com.artcom.y60.hoccer;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.http.HttpHelper;

public class HocEventException extends Exception {

    private static final long   serialVersionUID = 1L;

    private static final String LOG_TAG          = "HocEventException";

    private String              mState;
    private String              mMessage;
    private final String        mUrl;

    public HocEventException(Exception e) {
        super(e);
        mUrl = "";

        if (e instanceof UnknownHostException) {
            mMessage = "Could not connect to server";
        } else {
            mMessage = e.getMessage();
        }
        // not correctly implemented right now
    }

    public HocEventException(String pUrl, HttpResponse pResponse) {
        mUrl = pUrl;

        JSONObject json;
        try {

            String content = HttpHelper.extractBodyAsString(pResponse.getEntity());

            json = new JSONObject(content);
            Logger.v(LOG_TAG, json);

            mState = json.getString("state");
            if (json.has("message")) {
                mMessage = json.getString("message");
            }
        } catch (JSONException e) {
            Logger.v(LOG_TAG, e);
        } catch (IOException e) {
            Logger.v(LOG_TAG, e);
        }

    }

    public HocEventException(String message, String state, String url) {
        mState = state;
        mMessage = message;
        mUrl = url;
    }

    public String getState() {
        return mState;
    }

    @Override
    public String getMessage() {
        return mMessage;
    }

    public String getBestReadableMessage() {
        if (getMessage() != null) {
            return getMessage();
        }

        return getState();
    }

    public String getUrl() {
        return mUrl;
    }
}
