package com.artcom.y60.hoccer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Logger;
import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpHelper;
import com.artcom.y60.http.HttpServerException;

public class SweepOutEvent extends HocEvent {
    
    private static final String LOG_TAG = "SweepOutEvent";
    
    public SweepOutEvent() {
        
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepOut");
        eventParams.put("event[latitude]", "23");
        eventParams.put("event[longitude]", "34");
        
        try {
            HttpResponse response;
            response = HttpHelper.postUrlEncoded(getRemoteServer() + "/events", eventParams);
            
            Logger.v(LOG_TAG, "response header is: \n", HttpHelper.getHeadersAsString(response
                    .getAllHeaders()));
            
            String body = HttpHelper.extractBodyAsString(response.getEntity());
            Logger.v(LOG_TAG, "response body is: \n", body);
            
            JSONObject jsonBody = new JSONObject(body);
            setState(jsonBody.getString("state"));
            setLiftime(Double.parseDouble(jsonBody.getString("expires")));
            
        } catch (HttpClientException e) {
            throw new RuntimeException(e);
        } catch (HttpServerException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
