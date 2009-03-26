package com.artcom.y60.infrastructure.http;

import java.net.URI;

/**
 * 
 * @author arne
 */
public interface ResourceChangeListener {

    public void onResourceChanged(URI pResourceUri);
}
