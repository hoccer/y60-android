package com.artcom.y60.hoccer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.data.StreamableContent;
import com.artcom.y60.http.AsyncHttpPut;
import com.artcom.y60.http.HttpResponseHandler;
import com.artcom.y60.http.MultipartHttpEntity;

public class SweepOutEvent extends HocEvent {

    private static final String     LOG_TAG       = "SweepOutEvent";
    private final StreamableContent mOutgoingData;
    AsyncHttpPut                    mDataUploader = null;

    SweepOutEvent(HocLocation pLocation, StreamableContent pOutgoingData,
            DefaultHttpClient pHttpClient) {
        super(pLocation, pHttpClient);
        mOutgoingData = pOutgoingData;
    }

    public boolean hasDataBeenUploaded() {
        if (mDataUploader == null) {
            return false;
        }
        return mDataUploader.wasSuccessful();
    }

    @Override
    protected void updateStatusFromJson(JSONObject status) throws JSONException, IOException {
        super.updateStatusFromJson(status);
        if (status.has("upload_uri") && mDataUploader == null) {
            uploadDataTo(status.getString("upload_uri"));
        }

    }

    private void uploadDataTo(String uri) throws JSONException, IOException {
        Logger.v(LOG_TAG, "starting upload to " + uri);
        mDataUploader = new AsyncHttpPut(uri);
        MultipartHttpEntity multipart = new MultipartHttpEntity();
        multipart.addPart("upload[attachment]", "somefilename.txt", "text/plain", mOutgoingData);
        mDataUploader.setBody(multipart);

        mDataUploader.registerResponseHandler(new HttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, StreamableContent body) {
                Logger.v(LOG_TAG, "onSuccess with body: ", body);
                SweepOutEvent.this.onSuccess();
            }

            @Override
            public void onReceiving(double progress) {
                Logger.v(LOG_TAG, "onReceiving progress: ", progress);
            }

            @Override
            public void onError(int statusCode, StreamableContent body) {
                Logger.e(LOG_TAG, "onError: ", body, " with status code: ", statusCode);
                SweepOutEvent.this.onError();
            }

            @Override
            public void onHeaderAvailable(Header[] pHeaders) {
            }

        });
        mDataUploader.start();
    }

    @Override
    protected Map<String, String> getEventParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepOut");
        return eventParams;
    }

    @Override
    public StreamableContent getData() {
        return mOutgoingData;
    }
}