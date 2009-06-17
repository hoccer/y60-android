package com.artcom.y60.gom;

// import com.artcom.y60.gom.GomAttribute;
// import com.artcom.y60.gom.GomEntry;
// import com.artcom.y60.gom.GomNode;

import com.artcom.y60.RpcStatus;

interface IGomProxyService {

    void getNodeData(in String pPath, out List<String> pSubNodeNames, out List<String> pAttributeNames);
    
    String getAttributeValue(in String pPath, out RpcStatus pStatus);

    String getCachedAttributeValue(in String pPath, out RpcStatus pStatus);
    
    void refreshEntry(String pPath, out RpcStatus pStatus);
    
    String getBaseUri();
    
    void saveAttribute(in String pPath, in String pValue);
    
    void saveNode(in String pPath, out List<String> pSubNodeNames, out List<String> pAttributeNames);
    
    void deleteEntry(String pPath, out RpcStatus pStatus);
    
    boolean hasInCache(String pPath);
    
}