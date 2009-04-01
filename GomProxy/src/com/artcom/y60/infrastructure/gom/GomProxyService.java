package com.artcom.y60.infrastructure.gom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import com.artcom.y60.conf.DeviceConfiguration;
import com.artcom.y60.infrastructure.HTTPHelper;
import com.artcom.y60.infrastructure.JsonHelper;
import com.artcom.y60.logging.Logger;

public class GomProxyService extends Service {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG;

    
    
    // Static Initializer ------------------------------------------------
    
    static {
        
        LOG_TAG = "GomProxyService[class "+System.currentTimeMillis()+"]";
    }


    
    // Instance Variables ------------------------------------------------

    private String mId;
    
    private GomProxyRemote mRemote;
    
    private Map<String, NodeData> mNodes;
    
    private Map<String, String> mAttributes;
    
    private Uri mBaseUri;

    

    // Constructors ------------------------------------------------------

    public GomProxyService() {
        
        mId = String.valueOf(System.currentTimeMillis());
        mNodes      = new HashMap<String, NodeData>();
        mAttributes = new HashMap<String, String>();
        Logger.v(tag(), "HttpProxyService instantiated");
        
        DeviceConfiguration conf = DeviceConfiguration.load();
        mBaseUri = Uri.parse(conf.getGomUrl());
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    public void onCreate() {
        
        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());
        
        Logger.i(tag(), "GomProxyService.onCreate");
        
        super.onCreate();
        
        mRemote = new GomProxyRemote();
    }


    public void onStart(Intent intent, int startId) {

        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());
        
        Logger.i(tag(), "GomProxyService.onStart");
        
        super.onStart(intent, startId);
    }


    public void onDestroy() {
        
        Logger.i(tag(), "HttpProxyService.onDestroy");
        
        super.onDestroy();
    }


    public IBinder onBind(Intent pIntent) {
        
        return mRemote;
    }
    
    
    
    // Package Protected Instance Methods --------------------------------

    void getNodeData(String pPath, List<String> pSubNodeNames, List<String> pAttributeNames) {
        
        //Logger.v(tag(), "getNodeData("+pPath+")");
        
        NodeData node = null;
        synchronized (mNodes) {
            
            if (!hasNodeInCache(pPath)) {
                
                Logger.v(tag(), "node not in cache, load from gom");
                loadNode(pPath);
                
            } else {
                
                Logger.v(tag(), "ok, node's in cache");
            }
            
            node = mNodes.get(pPath);
        }
        
        synchronized (node) {
            
            pSubNodeNames.clear();
            pSubNodeNames.addAll(node.subNodeNames);
            
            pAttributeNames.clear();
            pAttributeNames.addAll(node.attributeNames);
        }
    }
    
    
    String getAttributeValue(String pPath) {
        
        Logger.v(tag(), "getAttributeValue(", pPath, ")");
        
        synchronized (mAttributes) {
            
            if (!hasAttributeInCache(pPath)) {
                
                Logger.v(tag(), "attribute not in cache, load from gom");
                loadAttribute(pPath);
                
            } else {
                
                Logger.v(tag(), "ok, attribute's in cache");
            }
            
            String value = mAttributes.get(pPath);
            //Logger.v(tag(), "attribute value: "+value);
            return value;
        }
    }
    
    
    void refreshEntry(String pPath) {
        
        Logger.v(tag(), "refreshEntry(", pPath, ")");
        String lastSegment = pPath.substring(pPath.lastIndexOf("/")+1);
        if (lastSegment.contains(":")) {
            
            loadAttribute(pPath);
            
        } else {
            
            loadNode(pPath);
        }
    }
    
    
    String getBaseUri() {
        
        return mBaseUri.toString();
    }
    
    
    boolean hasAttributeInCache(String pPath) {
        
        return mAttributes.containsKey(pPath);
    }
    
    
    boolean hasNodeInCache(String pPath) {
        
        return mNodes.containsKey(pPath);
    }
    
    
    
    // Private Instance Methods ------------------------------------------
    
    private void loadNode(String pPath) {
        
        Logger.v(tag(), "loadNode(", pPath, ")");
        
        NodeData node = new NodeData(new LinkedList<String>(), new LinkedList<String>());
            
        try {
                
            String     uri    = Uri.withAppendedPath(mBaseUri, pPath).toString();
            JSONObject jsob   = HTTPHelper.getJson(uri);
            JSONObject jsNode = JsonHelper.getMemberOrSelf(jsob, GomKeywords.NODE);
            
            JSONArray children = jsNode.getJSONArray(GomKeywords.ENTRIES);
            for (int i=0; i<children.length(); i++) {
                
                JSONObject jsChild = children.getJSONObject(i);
                
                // try subnode
                if (jsChild.has(GomKeywords.NODE)) {
                    
                    String subNodePath = jsChild.getString(GomKeywords.NODE);
                    String subNodeName = subNodePath.substring(subNodePath.lastIndexOf("/")+1);
                    node.subNodeNames.add(subNodeName);
                    continue;
                }
                
                // try attribute
                if (jsChild.has(GomKeywords.ATTRIBUTE)) {
                    
                    JSONObject jsAttr = jsChild.getJSONObject(GomKeywords.ATTRIBUTE);
                    String attrName = jsAttr.getString(GomKeywords.NAME);
                    node.attributeNames.add(attrName);
                    continue;
                }
                
                // huh?!
                Logger.w(tag(), "got entry as child of a GOM node which I can't decode: ", jsChild);
            }
            
            synchronized (mNodes) {
                
                mNodes.put(pPath, node);
            }
            
        } catch (JSONException x) {
            
            Logger.e(tag(), "loading node for path ", pPath, " failed", x);
            
            synchronized (mNodes) {
             
                if (!mNodes.containsKey(pPath)) {
                    
                    throw new RuntimeException("loading node for path "+pPath+" failed", x);
                    
                } else {
                    
                    Logger.v(tag(), "previous value is in cache, so I don't throw an exception");
                }
            }
        }
    }
    
    
    private void loadAttribute(String pPath) {
        
        Logger.v(tag(), "loadAttribute(", pPath, ")");
        
        try {
            
            String     uri   = Uri.withAppendedPath(mBaseUri, pPath).toString();
            JSONObject jsob  = HTTPHelper.getJson(uri);
            JSONObject attr  = jsob.getJSONObject(GomKeywords.ATTRIBUTE);
            String     value = attr.getString(GomKeywords.VALUE);
            
            synchronized (mAttributes) {
                
                mAttributes.put(pPath, value);
            }
            
        } catch (JSONException x) {
            
            Logger.e(tag(), "loading attribute for path ", pPath, " failed", x);
            
            synchronized (mAttributes) {
             
                if (!mAttributes.containsKey(pPath)) {
                    
                    throw new RuntimeException("loading attribute for path "+pPath+" failed", x);
                    
                } else {
                    
                    Logger.v(tag(), "previous value is in cache, so I don't throw an exception");
                }
            }
        }
    }

    
    private String tag() {
        
        return LOG_TAG+"[instance "+mId+"]";
    }
    
    
    
    // Inner Classes -----------------------------------------------------

    class NodeData {
        
        List<String> subNodeNames;
        List<String> attributeNames;
        
        NodeData(List<String> pSubNodeNames, List<String> pAttributeNames) {
            
            subNodeNames = pSubNodeNames;
            attributeNames = pAttributeNames;
        }
    }

    
    class GomProxyRemote extends IGomProxyService.Stub {

        public String getAttributeValue(String path) throws RemoteException {
            
            return GomProxyService.this.getAttributeValue(path);
        }

        public void getNodeData(String path, List<String> subNodeNames,
                List<String> attributeNames) throws RemoteException {

            GomProxyService.this.getNodeData(path, subNodeNames, attributeNames);
        }

        public void refreshEntry(String path) throws RemoteException {
            
            GomProxyService.this.refreshEntry(path);
        }
        
        public String getBaseUri() throws RemoteException {
            
            return GomProxyService.this.getBaseUri();
        }
    }

}
