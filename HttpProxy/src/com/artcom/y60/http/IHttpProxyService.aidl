package com.artcom.y60.http;

interface IHttpProxyService {

    
    Bundle get(String pUri);
    
    Bundle fetchFromCache(String pUri);
}