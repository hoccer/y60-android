package com.artcom.y60.infrastructure.gom;

// import com.artcom.y60.infrastructure.gom.GomAttribute;
// import com.artcom.y60.infrastructure.gom.GomEntry;
// import com.artcom.y60.infrastructure.gom.GomNode;

interface IGomProxyService {

    void getNodeData(in String pPath, out List<String> pSubNodeNames, out List<String> pAttributeNames);
    
    String getAttributeValue(in String pPath);
    
    void refreshEntry(String pPath);
    
    String getBaseUri();
}