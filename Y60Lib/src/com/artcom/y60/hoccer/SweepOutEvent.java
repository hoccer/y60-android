package com.artcom.y60.hoccer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        return mDataUploader.isRequestCompleted();
    }

    @Override
    protected void updateStatusFromJson(JSONObject status) throws JSONException, IOException {
        super.updateStatusFromJson(status);
        if (status.has("upload_uri") && mDataUploader == null) {
            uploadDataTo(status.getString("upload_uri"));
        }

    }

    @Override
    /**
     * @return true if link is ready and download has started 
     */
    protected boolean wasSuccessful() {
        return super.wasSuccessful() && hasDataBeenUploaded();
    }

    private void uploadDataTo(String uri) throws JSONException, IOException {
        Logger.v(LOG_TAG, "starting upload of '", mOutgoingData, "' to " + uri);
        mDataUploader = new AsyncHttpPut(uri);
        MultipartHttpEntity multipart = new MultipartHttpEntity();
        multipart.addPart("upload[attachment]", mOutgoingData);
        mDataUploader.setBody(multipart);

        mDataUploader.registerResponseHandler(new HttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, StreamableContent body) {
                SweepOutEvent.this.tryForSuccess();
            }

            @Override
            public void onReceiving(double progress) {
            }

            @Override
            public void onError(int statusCode, StreamableContent body) {
                Logger.e(LOG_TAG, "onError: ", body, " with status code: ", statusCode);
                SweepOutEvent.this.onError(new HocEventException("upload failed with status code "
                        + statusCode, "failed", "<unknown uri>"));
            }

            @Override
            public void onHeaderAvailable(HashMap<String, String> headers) {
            }

            @Override
            public void onError(Exception e) {
                SweepOutEvent.this.onError(new HocEventException(e));
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
