package com.artcom.y60.http;

interface IHttpProxyService {

    
    byte[] get(String pUri);
    
    byte[] fetchFromCache(String pUri);
}