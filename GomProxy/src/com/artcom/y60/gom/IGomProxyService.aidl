package com.artcom.y60.gom;

// import com.artcom.y60.gom.GomAttribute;
// import com.artcom.y60.gom.GomEntry;
// import com.artcom.y60.gom.GomNode;

import com.artcom.y60.RpcStatus;

interface IGomProxyService {

    void getNodeData(in String pPath, out List<String> pSubNodeNames, out List<String> pAttributeNames, out RpcStatus status);
    
    String getAttributeValue(in String pPath, out RpcStatus pStatus);

    String getCachedAttributeValue(in String pPath, out RpcStatus pStatus);
    
    void refreshEntry(String pPath, out RpcStatus pStatus);
    
    String getBaseUri(out RpcStatus status);
    
    void saveAttribute(in String pPath, in String pValue, out RpcStatus status);
    
    void saveNode(in String pPath, out List<String> pSubNodeNames, out List<String> pAttributeNames, out RpcStatus status);
    
    void deleteEntry(String pPath, out RpcStatus pStatus);
    
    boolean hasInCache(String pPath, out RpcStatus pStatus);
    
    void clear(out RpcStatus pStatus);
}
