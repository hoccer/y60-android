package com.artcom.y60.gom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import com.artcom.y60.Constants;
import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.IntentExtraKeys;
import com.artcom.y60.JsonHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.RpcStatus;
import com.artcom.y60.Y60Service;
import com.artcom.y60.http.HttpClientException;

public class GomProxyService extends Y60Service {

    // Constants ---------------------------------------------------------

    private static final String   LOG_TAG = "GomProxyService";

    // Instance Variables ------------------------------------------------

    private GomProxyRemote        mRemote;

    private Map<String, NodeData> mNodes;

    private Map<String, String>   mAttributes;

    private Uri                   mBaseUri;

    // Constructors ------------------------------------------------------

    public GomProxyService() {

        mNodes = new HashMap<String, NodeData>();
        mAttributes = new HashMap<String, String>();
        Logger.v(LOG_TAG, "HttpProxyService instantiated");

        mBaseUri = Uri.parse(Constants.Gom.URI);
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        DeviceConfiguration conf = DeviceConfiguration.load();
        Logger.setFilterLevel(conf.getLogLevel());

        Logger.i(LOG_TAG, "GomProxyService.onCreate");

        super.onCreate();

        mRemote = new GomProxyRemote();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        Logger.i(LOG_TAG, "onStart");

        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {

        Logger.i(LOG_TAG, "onDestroy");

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent pIntent) {

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

    private void getCachedNodeData(String pPath, List<String> pSubNodeNames,
            List<String> pAttributeNames) throws GomProxyException {

        Logger.v(LOG_TAG, "getCachedNodeData(", pPath, ")");

        synchronized (mAttributes) {

            if (!hasNodeInCache(pPath)) {
                throw new GomProxyException("Node '" + pPath + "' not found in cache");
            }

            NodeData data = mNodes.get(pPath);
            pSubNodeNames = data.subNodeNames;
            pAttributeNames = data.attributeNames;

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
            data.attributeNames = pAttributeNames;
            data.subNodeNames = pSubNodeNames;
        }

    }

    void clear() {

        synchronized (mAttributes) {
            synchronized (mNodes) {

                mAttributes.clear();
                mNodes.clear();
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

    /**
     * @param pPath
     * @param jsob
     * @throws JSONException
     */
    private void updateNodeFromJson(String pPath, JSONObject jsob) throws JSONException {

        JSONObject jsNode = JsonHelper.getMemberOrSelf(jsob, Constants.Gom.Keywords.NODE);
        NodeData node = new NodeData(new LinkedList<String>(), new LinkedList<String>());

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
            Logger.w(LOG_TAG, "got entry as child of a GOM node which I can't decode: ", jsChild);
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
        Logger.v(LOG_TAG, "loaded attribute from gom: ", attr);

        String value = attr.getString(Constants.Gom.Keywords.VALUE);

        synchronized (mAttributes) {
            mAttributes.put(pPath, value);
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
    }

    class GomNotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context pContext, Intent pIntent) {

            try {
                String operation = pIntent.getStringExtra(
                        IntentExtraKeys.KEY_NOTIFICATION_OPERATION).toLowerCase();
                String path = pIntent.getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH);
                String dataStr = pIntent
                        .getStringExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING);

                Logger.d(LOG_TAG, "GOM receiver for GomProxyService received notification: ",
                        operation.toUpperCase(), " ", path);

                synchronized (mAttributes) {
                    synchronized (mNodes) {

                        if (path.contains(":")) {

                            handleAttributeTransformation(operation, path, dataStr);

                        } else {

                            handleNodeTransformation(operation, path);
                        }
                    }
                }
            } catch (JSONException jsx) {
                ErrorHandling.signalJsonError(LOG_TAG, jsx, GomProxyService.this);
            }
        }

        private void handleAttributeTransformation(String operation, String path, String dataStr)
                throws JSONException {
            if ("update".equals(operation) && mAttributes.containsKey(path)) {
                handleUpdateAttributeOnNotification(path, dataStr);
                return;
            }

            int colonIdx = path.lastIndexOf(":");
            String nodePath = path.substring(0, colonIdx);
            String attrName = path.substring(colonIdx + 1);

            Logger.d(LOG_TAG, "node path: ", nodePath);

            if ("create".equals(operation)) {
                handleCreateAttributeOnNotification(path, nodePath, attrName);
            }
            if ("delete".equals(operation)) {
                handleRemoveAttributeOnNotification(path, nodePath, attrName);
            }
        }

        private void handleNodeTransformation(String operation, String path) {
            int slashIdx = path.lastIndexOf("/");
            String parentPath = path.substring(0, slashIdx);
            String subNodeName = path.substring(slashIdx + 1);

            Logger.d(LOG_TAG, "node path: ", parentPath);

            if ("create".equals(operation)) {
                handleCreateNodeOnNotification(path, parentPath, subNodeName);
            }
            if ("delete".equals(operation)) {
                handleDeleteNodeOnNotification(path, parentPath, subNodeName);
            }
        }

        private void handleDeleteNodeOnNotification(String path, String parentPath,
                String subNodeName) {
            if (mNodes.containsKey(parentPath)) {

                Logger.d(LOG_TAG, "delete sub node on node in cache: ", path);

                NodeData nodeData = mNodes.get(parentPath);
                nodeData.subNodeNames.remove(subNodeName);
            }

            if (mNodes.containsKey(path)) {

                mNodes.remove(path);

            }

            synchronized (mAttributes) {

                for (String attributePath : mAttributes.keySet()) {
                    if (attributePath.startsWith(path + ":")) {
                        mAttributes.remove(attributePath);
                    }
                }

            }

            for (String nodePath : mNodes.keySet()) {
                if (nodePath.startsWith(path + "/")) {
                    mNodes.remove(nodePath);
                }
            }
        }

        private void handleCreateNodeOnNotification(String path, String parentPath,
                String subNodeName) {
            if (mNodes.containsKey(parentPath)) {

                Logger.d(LOG_TAG, "create sub node on node in cache: ", path);

                NodeData nodeData = mNodes.get(parentPath);
                nodeData.subNodeNames.add(subNodeName);
            }
        }

        private void handleRemoveAttributeOnNotification(String path, String nodePath,
                String attrName) {
            if (mNodes.containsKey(nodePath)) {

                Logger.d(LOG_TAG, "delete attribute on node in cache: ", path);

                NodeData nodeData = mNodes.get(nodePath);
                nodeData.attributeNames.remove(attrName);
            }

            synchronized (mAttributes) {

                if (mAttributes.containsKey(path)) {

                    mAttributes.remove(path);

                }
            }
        }

        private void handleCreateAttributeOnNotification(String path, String nodePath,
                String attrName) {
            if (mNodes.containsKey(nodePath)) {

                Logger.d(LOG_TAG, "create attribute on node in cache: ", path);

                NodeData nodeData = mNodes.get(nodePath);
                nodeData.attributeNames.add(attrName);
            }
        }

        private void handleUpdateAttributeOnNotification(String path, String dataStr)
                throws JSONException {
            Logger.d(LOG_TAG, "notification affects attribute in cache: ", path);

            updateAttributeFromJson(path, new JSONObject(dataStr));
        }

    }

    private void deleteAttribute(String pPath) {

        Logger.v(LOG_TAG, mAttributes.keySet().toString(), "\n\n\n\ndelete attribute ", pPath,
                " size: ", mAttributes.size(), " has in cacche: ", hasAttributeInCache(pPath));
        mAttributes.remove(pPath);
        Logger.v(LOG_TAG, "delete attribute ", pPath, " size: ", mAttributes.size(),
                " has in cacche: ", hasAttributeInCache(pPath));
    }

    private void deleteNode(String pPath) {
        NodeData nodeData = mNodes.get(pPath);
        if (nodeData == null) {
            return;
        }
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
        Logger.v(LOG_TAG, "delete node ", pPath, " size: ", mNodes.size(), " has in cacche: ",
                hasNodeInCache(pPath));
        mNodes.remove(pPath);
        Logger.v(LOG_TAG, "delete node ", pPath, " size: ", mNodes.size(), " has in cacche: ",
                hasNodeInCache(pPath));
    }

    public void deleteEntry(String pPath) {
        Logger.v(LOG_TAG, pPath);
        String lastSegment = pPath.substring(pPath.lastIndexOf("/") + 1);
        if (lastSegment.contains(":")) {
            Logger.v(LOG_TAG, "delete attribute " + pPath);
            deleteAttribute(pPath);
        } else {
            Logger.v(LOG_TAG, "delete node ", pPath);
            deleteNode(pPath);
        }
    }

}
