package com.artcom.y60.infrastructure.http;

interface IHttpProxyService {

    
    byte[] get(String pUri);
    
    byte[] fetchFromCache(String pUri);
}