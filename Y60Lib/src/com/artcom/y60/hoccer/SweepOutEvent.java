package com.artcom.y60.hoccer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.data.Streamable;
import com.artcom.y60.http.AsyncHttpPut;
import com.artcom.y60.http.HttpResponseHandler;
import com.artcom.y60.http.MultipartHttpEntity;

public class SweepOutEvent extends HocEvent {
    
    private static final String LOG_TAG       = "SweepOutEvent";
    private final Streamable    mOutgoingData;
    AsyncHttpPut                mDataUploader = null;
    
    SweepOutEvent(HocLocation pLocation, Streamable pOutgoingData, DefaultHttpClient pHttpClient) {
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
            public void onSuccess(int statusCode, OutputStream body) {
                Logger.v(LOG_TAG, "upload successful with: ", body);
            }
            
            @Override
            public void onReceiving(double progress) {
            }
            
            @Override
            public void onError(int statusCode, OutputStream body) {
                Logger.e(LOG_TAG, "upload failed with: ", body);
            }
            
            @Override
            public void onConnecting() {
                // TODO Auto-generated method stub
                
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
}
