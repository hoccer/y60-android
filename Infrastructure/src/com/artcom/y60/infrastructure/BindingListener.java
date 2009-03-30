package com.artcom.y60.infrastructure;

public interface BindingListener<T> {

    public void bound(T pHelper);
    
    public void unbound(T pHelper);
}
