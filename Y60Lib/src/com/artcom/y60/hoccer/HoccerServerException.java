package com.artcom.y60.hoccer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.http.HttpHelper;

public class HoccerServerException extends Exception {
    
    private static final long   serialVersionUID = 1L;
    
    private static final String LOG_TAG          = "HoccerServerException";
    
    private String              mState;
    private String              mMessage;
    private final String        mUrl;
    
    private final int           mStatusCode;
    
    public HoccerServerException(Exception e) {
        super(e);
        mUrl = "";
        mStatusCode = 500;
        mMessage = e.getMessage();
        // not correctly implemented right now
    }
    
    public HoccerServerException(String pUrl, HttpResponse pResponse) {
        mUrl = pUrl;
        mStatusCode = pResponse.getStatusLine().getStatusCode();
        
        JSONObject json;
        try {
            
            String content = HttpHelper.extractBodyAsString(pResponse.getEntity());
            
            File file = new File("/sdcard/HoccerServerException.xml");
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.write(content.getBytes());
            
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
    
    public int getStatusCode() {
        return mStatusCode;
    }
}
