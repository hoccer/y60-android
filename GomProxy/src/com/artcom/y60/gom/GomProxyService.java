package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.Constants;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.JsonHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.RpcStatus;
import com.artcom.y60.Y60Action;
import com.artcom.y60.Y60Service;
import com.artcom.y60.http.HttpClientException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

public class GomProxyService extends Y60Service {

    // Constants --- ------------------------------------------------------

    private static final String         LOG_TAG = "GomProxyService";

    // Instance Variables ------------------------------------------------

    private GomProxyRemote              mRemote;

    private final Map<String, NodeData> mNodes;

    private final Map<String, String>   mAttributes;

    private final Uri                   mBaseUri;

    private BroadcastReceiver           mResetReceiver;

    // Constructors ------------------------------------------------------

    public GomProxyService() {

        mNodes = new HashMap<String, NodeData>();
        mAttributes = new HashMap<String, String>();
        Logger.v(LOG_TAG, "GomProxyService instantiated");
        mBaseUri = Uri.parse(Constants.Gom.URI);
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        Logger.i(LOG_TAG, "GomProxyService.onCreate");
        mRemote = new GomProxyRemote();

        IntentFilter filter = new IntentFilter(Y60Action.RESET_BC);
        mResetReceiver = new ResetReceiver();
        registerReceiver(mResetReceiver, filter);

        super.onCreate();

        Logger.i(LOG_TAG, "<<< GomProxyService.onCreate");
    }

    @Override
    public void onStart(Intent pIntent, int startId) {
        Logger.v(LOG_TAG, "onStart: threadid: ", Thread.currentThread().getId());
        sendBroadcast(new Intent(Y60Action.SERVICE_GOM_PROXY_READY));
        super.onStart(pIntent, startId);
    }

    @Override
    public void onDestroy() {
        Logger.i(LOG_TAG, "onDestroy");
        unregisterReceiver(mResetReceiver);
        sendBroadcast(new Intent(Y60Action.SERVICE_GOM_PROXY_DOWN));
        super.onDestroy();
    }

    @Override
    protected void kill() {
        // do not kill me upon shutdown services bc
    }

    @Override
    public IBinder onBind(Intent pIntent) {
        sendBroadcast(new Intent(Y60Action.SERVICE_GOM_PROXY_READY));
        return mRemote;
    }

    // Package Protected Instance Methods --------------------------------

    void getNodeData(String pPath, List<String> pSubNodeNames, List<String> pAttributeNames)
            throws JSONException, GomEntryNotFoundException, GomProxyException {

        // Logger.v(tag(), "getNodeData("+pPath+")");

        NodeData node = null;
        synchronized (mNodes) {

            if (!hasNodeInCache(pPath)) {

                Logger.v(LOG_TAG, "node not in cache, load from gom");
                loadNode(pPath);

            } else {

                Logger.v(LOG_TAG, "ok, node's in cache");
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

    void getCachedNodeData(String pPath, List<String> pSubNodeNames, List<String> pAttributeNames)
            throws GomProxyException {

        Logger.v(LOG_TAG, "getCachedNodeData(", pPath, ")");

        synchronized (mAttributes) {

            if (!hasNodeInCache(pPath)) {
                throw new GomProxyException("Node '" + pPath + "' not found in cache");
            }

            NodeData data = mNodes.get(pPath);
            synchronized (data) {
                pSubNodeNames.addAll(data.subNodeNames);
                pAttributeNames.addAll(data.attributeNames);
            }
        }

    }

    String getAttributeValue(String pPath) throws JSONException, GomEntryNotFoundException,
            GomProxyException {

        Logger.v(LOG_TAG, "getAttributeValue(", pPath, ")");

        synchronized (mAttributes) {

            if (!hasAttributeInCache(pPath)) {

                Logger.v(LOG_TAG, "attribute not in cache, load from gom");
                loadAttribute(pPath);

            } else {

                Logger.v(LOG_TAG, "ok, attribute's in cache");
            }

            String value = mAttributes.get(pPath);
            return value;
        }
    }

    String getCachedAttributeValue(String pPath) throws GomProxyException {

        Logger.v(LOG_TAG, "getAttributeValue(", pPath, ")");

        synchronized (mAttributes) {

            if (!hasAttributeInCache(pPath)) {
                throw new GomProxyException("Attribute '" + pPath + "' not found in cache");
            }

            String value = mAttributes.get(pPath);
            return value;
        }

    }

    void refreshEntry(String pPath) throws JSONException, GomEntryNotFoundException,
            GomProxyException {
        Logger.v(LOG_TAG, "refreshEntry(", pPath, ")");
        String lastSegment = pPath.substring(pPath.lastIndexOf("/") + 1);
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

    void saveAttribute(String pPath, String pValue) {

        synchronized (mAttributes) {

            mAttributes.put(pPath, pValue);
        }
    }

    void saveNode(String pNodePath, List<String> pSubNodeNames, List<String> pAttributeNames) {

        synchronized (mNodes) {
            NodeData data = mNodes.get(pNodePath);
            if (data == null) {
                data = new NodeData();
                mNodes.put(pNodePath, data);
            }

            synchronized (data) {
                data.attributeNames = pAttributeNames;
                data.subNodeNames = pSubNodeNames;
            }
        }

    }

    void clear() {

        synchronized (mAttributes) {
            synchronized (mNodes) {
                mAttributes.clear();
                mNodes.clear();
                sendBroadcast(new Intent(Y60Action.SERVICE_GOM_PROXY_CLEARED));
            }
        }

    }

    // Private Instance Methods ------------------------------------------

    private void loadNode(String pPath) throws JSONException, GomEntryNotFoundException,
            GomProxyException {

        Logger.v(LOG_TAG, "loadNode(", pPath, ")");

        String uri = Uri.withAppendedPath(mBaseUri, pPath).toString();
        JSONObject jsob;
        try {
            jsob = HttpHelper.getJson(uri);
        } catch (HttpClientException ex) {
            if (ex.getStatusCode() == 404) {
                throw new GomEntryNotFoundException(ex);
            }
            throw new GomProxyException(ex);
        } catch (Exception ex) {

            throw new GomProxyException(ex);
        }
        updateNodeFromJson(pPath, jsob);

    }

    void updateEntry(String pPath, String pJsonData) throws JSONException {
        JSONObject jo = new JSONObject(pJsonData);
        if (!jo.has(Constants.Gom.Keywords.ATTRIBUTE)) {
            updateNodeFromJson(pPath, jo);
        } else {
            updateAttributeFromJson(pPath, jo);
        }
    }

    public void createEntry(String pPath, String pJsonData) throws JSONException {
        JSONObject jo = new JSONObject(pJsonData);

        Logger.v(LOG_TAG, "in create entry");
        updateEntry(pPath, pJsonData);

        String parentPath = GomReference.parentPath(pPath);
        String entryName = GomReference.lastSegment(pPath);

        Logger.v(LOG_TAG, "parentPath: ", parentPath);
        Logger.v(LOG_TAG, "entryName: ", entryName);

        if (jo.has(Constants.Gom.Keywords.ATTRIBUTE)) {

            Logger.v(LOG_TAG, "it's an attribute");

            if (hasNodeInCache(parentPath)) {

                Logger.v(LOG_TAG, "updating parent node");

                synchronized (mNodes) {
                    NodeData parentNodeData = mNodes.get(parentPath);
                    synchronized (parentNodeData) {
                        parentNodeData.attributeNames.add(entryName);
                    }
                }
            }

        } else {

            Logger.v(LOG_TAG, "it's a node");

            if (pPath.lastIndexOf("/") == pPath.length() - 1) {
                pPath = pPath.substring(0, pPath.length() - 1);
            }

            if (hasNodeInCache(parentPath)) {

                Logger.v(LOG_TAG, "updating parent node");

                NodeData parentNodeData = mNodes.get(parentPath);
                synchronized (parentNodeData) {
                    parentNodeData.subNodeNames.add(entryName);
                }
            }
        }
    }

    /**
     * @param pPath
     * @param jsob
     * @throws JSONException
     */
    private void updateNodeFromJson(String pPath, JSONObject jsob) throws JSONException {

        JSONObject jsNode = JsonHelper.getMemberOrSelf(jsob, Constants.Gom.Keywords.NODE);
        NodeData node = new NodeData(new LinkedList<String>(), new LinkedList<String>());

        synchronized (node) {
            JSONArray children = jsNode.getJSONArray(Constants.Gom.Keywords.ENTRIES);
            for (int i = 0; i < children.length(); i++) {

                JSONObject jsChild = children.getJSONObject(i);

                // try subnode
                if (jsChild.has(Constants.Gom.Keywords.NODE)) {

                    String subNodePath = jsChild.getString(Constants.Gom.Keywords.NODE);
                    String subNodeName = subNodePath.substring(subNodePath.lastIndexOf("/") + 1);
                    node.subNodeNames.add(subNodeName);
                    continue;
                }

                // try attribute
                if (jsChild.has(Constants.Gom.Keywords.ATTRIBUTE)) {

                    JSONObject jsAttr = jsChild.getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
                    String attrName = jsAttr.getString(Constants.Gom.Keywords.NAME);
                    node.attributeNames.add(attrName);
                    continue;
                }

                // huh?!
                Logger.w(LOG_TAG, "got entry as child of a GOM node which I can't decode: ",
                        jsChild);
            }
        }
        synchronized (mNodes) {

            mNodes.put(pPath, node);
        }
    }

    private void loadAttribute(String pPath) throws JSONException, GomEntryNotFoundException,
            GomProxyException {

        Logger.v(LOG_TAG, "loadAttribute(", pPath, ")");

        String uri = Uri.withAppendedPath(mBaseUri, pPath).toString();
        JSONObject jsob;
        try {
            jsob = HttpHelper.getJson(uri);
        } catch (HttpClientException ex) {
            if (ex.getStatusCode() == 404) {
                throw new GomEntryNotFoundException(ex);
            }
            throw new GomProxyException(ex);
        } catch (Exception ex) {
            throw new GomProxyException(ex);
        }

        updateAttributeFromJson(pPath, jsob);
    }

    /**
     * @param pPath
     * @param attr
     * @throws JSONException
     */
    private void updateAttributeFromJson(String pPath, JSONObject pJso) throws JSONException {

        JSONObject attr = pJso.getJSONObject(Constants.Gom.Keywords.ATTRIBUTE);
        Logger.v(LOG_TAG, "updating attribute ", pPath, " using data ", attr);

        String value = attr.getString(Constants.Gom.Keywords.VALUE);

        synchronized (mAttributes) {
            mAttributes.put(pPath, value);
        }
    }

    private void deleteAttribute(String pPath) {

        mAttributes.remove(pPath);
        String nodePath = GomReference.parentPath(pPath);
        synchronized (mNodes) {
            if (hasNodeInCache(nodePath)) {

                NodeData data = mNodes.get(nodePath);
                synchronized (data) {
                    data.attributeNames.remove(GomReference.lastSegment(pPath));
                }
            }
        }
        Logger.v(LOG_TAG, "deleted attribute: ", pPath, " size: ", mAttributes.size(),
                " has in cache: ", hasAttributeInCache(pPath));
    }

    private void deleteNode(String pPath) {

        synchronized (mNodes) {
            NodeData nodeData = mNodes.remove(pPath);

            if (nodeData == null) {
                return;
            }

            synchronized (nodeData) {
                List<String> attrList = nodeData.attributeNames;
                List<String> nodeList = nodeData.subNodeNames;
                for (String anAttr : attrList) {
                    Logger.v(LOG_TAG, "delete attribute ", anAttr);
                    deleteAttribute(pPath + ":" + anAttr);
                }
                for (String aNode : nodeList) {
                    Logger.v(LOG_TAG, "delete node ", aNode);

                    deleteNode(pPath + "/" + aNode);
                }
            }
            Logger.v(LOG_TAG, "deleted node ", pPath, " size: ", mNodes.size(), " has in cache: ",
                    hasNodeInCache(pPath));
        }
    }

    public void deleteEntry(String pPath) {
        String lastSegment = pPath.substring(pPath.lastIndexOf("/") + 1);
        if (lastSegment.contains(":")) {
            Logger.v(LOG_TAG, "delete attribute " + pPath);
            deleteAttribute(pPath);
        } else {
            Logger.v(LOG_TAG, "delete node ", pPath);
            deleteNode(pPath);
        }
    }

    // Inner Classes -----------------------------------------------------

    class NodeData {

        List<String> subNodeNames;
        List<String> attributeNames;

        NodeData(List<String> pSubNodeNames, List<String> pAttributeNames) {

            subNodeNames = pSubNodeNames;
            attributeNames = pAttributeNames;
        }

        NodeData() {

        }
    }

    class GomProxyRemote extends IGomProxyService.Stub {

        public String getAttributeValue(String path, RpcStatus pStatus) throws RemoteException {

            try {
                return GomProxyService.this.getAttributeValue(path);

            } catch (Exception ex) {
                pStatus.setError(ex);
                return null;
            }
        }

        @Override
        public String getCachedAttributeValue(String pPath, RpcStatus pStatus) {
            try {
                return GomProxyService.this.getCachedAttributeValue(pPath);
            } catch (Exception ex) {

                pStatus.setError(ex);
                return null;
            }
        }

        public void getNodeData(String path, List<String> subNodeNames,
                List<String> attributeNames, RpcStatus pStatus) throws RemoteException {
            try {
                GomProxyService.this.getNodeData(path, subNodeNames, attributeNames);
            } catch (Exception ex) {

                pStatus.setError(ex);
            }
        }

        @Override
        public void getCachedNodeData(String pPath, List<String> pSubNodeNames,
                List<String> pAttributeNames, RpcStatus pStatus) throws RemoteException {

            try {
                GomProxyService.this.getCachedNodeData(pPath, pSubNodeNames, pAttributeNames);
            } catch (Exception ex) {

                pStatus.setError(ex);
            }

        }

        public void refreshEntry(String path, RpcStatus pStatus) throws RemoteException {

            try {
                GomProxyService.this.refreshEntry(path);
            } catch (Exception ex) {
                pStatus.setError(ex);
            }

        }

        public String getBaseUri(RpcStatus pStatus) throws RemoteException {
            try {
                return GomProxyService.this.getBaseUri();
            } catch (Exception ex) {

                pStatus.setError(ex);
                return null;
            }
        }

        @Override
        public boolean hasInCache(String pPath, RpcStatus pStatus) throws RemoteException {
            try {
                return GomProxyService.this.hasAttributeInCache(pPath)
                        || GomProxyService.this.hasNodeInCache(pPath);
            } catch (Exception ex) {
                pStatus.setError(ex);
                return false;
            }
        }

        @Override
        public void saveAttribute(String pPath, String pValue, RpcStatus pStatus)
                throws RemoteException {
            try {
                GomProxyService.this.saveAttribute(pPath, pValue);
            } catch (Exception ex) {

                pStatus.setError(ex);
            }
        }

        @Override
        public void saveNode(String pPath, List<String> pSubNodeNames,
                List<String> pAttributeNames, RpcStatus pStatus) throws RemoteException {
            try {
                GomProxyService.this.saveNode(pPath, pSubNodeNames, pAttributeNames);
            } catch (Exception ex) {
                pStatus.setError(ex);
            }

        }

        @Override
        public void deleteEntry(String pPath, RpcStatus pStatus) {
            try {
                GomProxyService.this.deleteEntry(pPath);
            } catch (Exception ex) {

                pStatus.setError(ex);
            }
        }

        @Override
        public void clear(RpcStatus pStatus) {
            try {
                GomProxyService.this.clear();
            } catch (Exception ex) {
                pStatus.setError(ex);
            }
        }

        @Override
        public void updateEntry(String pPath, String pJsonData, RpcStatus pStatus)
                throws RemoteException {
            try {
                GomProxyService.this.updateEntry(pPath, pJsonData);
            } catch (Exception ex) {
                pStatus.setError(ex);
            }
        }

        @Override
        public void createEntry(String pPath, String pJsonData, RpcStatus pStatus)
                throws RemoteException {
            try {
                GomProxyService.this.createEntry(pPath, pJsonData);
            } catch (Exception ex) {
                pStatus.setError(ex);
            }

        }
    }

    class ResetReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context pContext, Intent pIntent) {

            Logger.d(LOG_TAG, "clearing GOM cache");
            GomProxyService.this.clear();
            Logger.d(LOG_TAG, "GOM cache cleared");
        }

    }
}
