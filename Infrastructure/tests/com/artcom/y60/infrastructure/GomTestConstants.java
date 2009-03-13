package com.artcom.y60.infrastructure;

import java.net.URI;

public abstract class GomTestConstants {

    // Constants ---------------------------------------------------------

    static final URI TEST_REPOSITORY_URI;
    
    static final String FIXTURES = "/test/android/y60/infrastructure/";
    
    static {
        
        try {
            TEST_REPOSITORY_URI = new URI("http://t-gom.service.t-gallery.act");
            
        } catch (Exception e) {
            
            throw new ExceptionInInitializerError(e);
        }
    }

}
