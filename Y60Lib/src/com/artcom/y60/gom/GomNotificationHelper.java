package com.artcom.y60.gom;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.StatusLine;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.Constants;
import com.artcom.y60.HTTPHelper;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.Logger;
import com.artcom.y60.NetworkHelper;

public class GomNotificationHelper {
    
    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "GomNotificationHelper";
    

    
    // Static Methods ----------------------------------------------------

    /**
     * Register a GOM observer for a given path. Filtering options are currently not supported.
     */
    public static BroadcastReceiver registerObserver(String pPath, GomObserver pGomObserver) throws IOException {
        
        postObserverToGom(pPath);
        
        return createBroadcastReceiver(pPath,pGomObserver);
        
    }


    private static BroadcastReceiver createBroadcastReceiver(final String pPath, final GomObserver pGomObserver) {
        // TODO Auto-generated method stub
        
        if(pPath == null){
            throw new IllegalArgumentException("Path cannot be null");
        }
        
        BroadcastReceiver br = new BroadcastReceiver() {
        
            @Override
            public void onReceive(Context pArg0, Intent pArg1) {
                
                Logger.d(LOG_TAG, "onReceive with intent: ", pArg1.toString(), " i-path: ", pArg1.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH));
              
                if (pPath.equals(pArg1.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH))){
                    
                    String jsnStr   = pArg1.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING);
                    JSONObject data;
                    try {
                        data = new JSONObject(jsnStr);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        throw new RuntimeException(e);
                    }

                    if ("create".equals(pArg1.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION))){
                        
                        pGomObserver.onEntryCreated(pPath, data);
                        
                    }else if("update".equals(pArg1.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION))){
                        
                        pGomObserver.onEntryUpdated(pPath, data);
                        
                    }else if("delete".equals(pArg1.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION))){
                        
                        pGomObserver.onEntryDeleted(pPath, data);
                        
                    }
                    
                }
        
            }
        };
        
        
        return br;
    }


    /**
     * @param pPath
     * @throws IOException
     */
    private static void postObserverToGom(String pPath) throws IOException {
        Map<String, String> formData = new HashMap<String, String>();
        
        InetAddress myIp        = NetworkHelper.getStagingIp();
        String      ip          = myIp.getHostAddress();
        String      callbackUrl = "http://"+ip+":"+Constants.Network.DEFAULT_PORT+Constants.Network.GNP_TARGET;
        formData.put("callback_url", callbackUrl);
        formData.put("accept", "application/json");

        String      observerPath = getObserverPathFor(pPath);
        String      observerUri  = Constants.Gom.URI+observerPath;
        
        Logger.d(LOG_TAG, "posting observer for GOM entry "+pPath+" to "+observerUri+" for callback "+callbackUrl);
        
        StatusLine status = HTTPHelper.postUrlEncoded(observerUri, formData);
        if (status.getStatusCode() >= 300) {
            
            throw new IOException("Unexpected HTTP status code: "+status.getStatusCode());
        }
    }
    
    
    /**
     * Convenience Method which returns the path to the node in which all the
     * observers of the given path reside.
     * 
     * @param pGomEntryPath
     * @return
     */
    public static String getObserverPathFor(String pGomEntryPath) {
        
        String base = Constants.Gom.OBSERVER_BASE_PATH; 
        
        int lastSlash = pGomEntryPath.lastIndexOf("/");
        String lastSegment = pGomEntryPath.substring(lastSlash);
        
        if (lastSegment.contains(":")) {
            
            // it's an attribute
            int colon = pGomEntryPath.lastIndexOf(":");
            String parentNodePath = pGomEntryPath.substring(0, colon);
            String attrName       = pGomEntryPath.substring(colon+1);
            
            return base+parentNodePath+"/"+attrName;
            
        } else {
            
            // it's a node
            return base+pGomEntryPath;
        }
    }

}
