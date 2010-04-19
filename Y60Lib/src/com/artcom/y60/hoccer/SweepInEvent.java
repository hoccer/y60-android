package com.artcom.y60.hoccer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.http.AsyncHttpGet;
import com.artcom.y60.http.HttpResponseHandler;

public class SweepInEvent extends HocEvent {

    private static String LOG_TAG         = "SweepInEvent";
    AsyncHttpGet          mDataDownloader = null;

    SweepInEvent(HocLocation pLocation, DefaultHttpClient pHttpClient) {
        super(pLocation, pHttpClient);
    }

    @Override
    protected void updateStatusFromJson(JSONObject status) throws JSONException, IOException {
        super.updateStatusFromJson(status);
        if (status.has("uploads") && mDataDownloader == null) {
            JSONArray uris = status.getJSONArray("uploads");
            if (uris.length() > 0) {
                downloadDataFrom(uris.getJSONObject(0).getString("uri"));
            }
        }
    }

    private void downloadDataFrom(String uri) throws JSONException, IOException {
        mDataDownloader = new AsyncHttpGet(uri);
        mDataDownloader.registerResponseHandler(new HttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, StreamableContent body) {
                Logger.v(LOG_TAG, "upload successful with: ", body);
                SweepInEvent.this.onSuccess();

            }

            @Override
            public void onReceiving(double progress) {
            }

            @Override
            public void onError(int statusCode, StreamableContent body) {
                Logger.e(LOG_TAG, "upload failed with: ", body);
                SweepInEvent.this.onError();
            }

            @Override
            public void onHeaderAvailable(Header[] pHeaders) {

            }
        });
        mDataDownloader.start();
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepIn");
        return eventParams;
    }

    public boolean hasDataBeenDownloaded() {
        if (mDataDownloader == null) {
            return false;
        }
        return mDataDownloader.wasSuccessful();
    }

    public StreamableContent getData() {
        if (mDataDownloader == null) {
            return null;
        }
        return mDataDownloader.getBodyAsStreamableContent();
    }
}
