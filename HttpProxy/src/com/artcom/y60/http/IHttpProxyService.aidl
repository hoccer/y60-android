package com.artcom.y60.http;

import com.artcom.y60.RpcStatus;

interface IHttpProxyService {

    
    Bundle get(String pUri, out RpcStatus status);
    
    Bundle fetchFromCache(String pUri, out RpcStatus status);
    
    boolean isInCache(String pUri, out RpcStatus status);
    
    void removeFromCache(String pUri, out RpcStatus status);
    
    Bundle getDataSyncronously(String pUri, out RpcStatus status);
}