package com.artcom.y60.hoccer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class SweepOutEvent extends HocEvent {
    
    private static final String LOG_TAG = "SweepOutEvent";
    
    SweepOutEvent(HttpClient pHttpClient) {
        super(pHttpClient);
        
        // try {
        // HttpResponse response;
        // response = HttpHelper.postUrlEncoded(getRemoteServer() + "/events", eventParams);
        //            
        // Logger.v(LOG_TAG, "response header is: \n", HttpHelper.getHeadersAsString(response
        // .getAllHeaders()));
        //            
        // String body = HttpHelper.extractBodyAsString(response.getEntity());
        // Logger.v(LOG_TAG, "response body is: \n", body);
        //            
        // JSONObject jsonBody = new JSONObject(body);
        // setState(jsonBody.getString("state"));
        // setLiftime(Double.parseDouble(jsonBody.getString("expires")));
        //            
        // } catch (HttpClientException e) {
        // throw new RuntimeException(e);
        // } catch (HttpServerException e) {
        // throw new RuntimeException(e);
        // } catch (IOException e) {
        // throw new RuntimeException(e);
        // } catch (JSONException e) {
        // throw new RuntimeException(e);
        // }
    }
    
    @Override
    protected Map<String, String> getHttpParameters() {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("event[type]", "SweepOut");
        eventParams.put("event[latitude]", "23");
        eventParams.put("event[longitude]", "34");
        return eventParams;
    }
    
    @Override
    protected void updateStatusFromJson(JSONObject status) throws JSONException {
        if (status.has("state")) {
            setState(status.getString("state"));
        }
        if (status.has("expires")) {
            setLiftime(Double.parseDouble(status.getString("expires")));
        }
    }
}
