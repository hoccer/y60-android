package com.artcom.y60.infrastructure.http;

public interface HttpProxyServiceBindingListener {

    public void bound(HttpProxyHelper pHelper);
    
    public void unbound(HttpProxyHelper pHelper);
}
