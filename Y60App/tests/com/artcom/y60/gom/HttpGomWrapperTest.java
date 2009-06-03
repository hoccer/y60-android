package com.artcom.y60.gom;

import junit.framework.TestCase;
import android.net.Uri;

import com.artcom.y60.Constants;

public class HttpGomWrapperTest extends TestCase {

    private static final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/gom_http_wrapper_test";
    
    public void testGetAttributeValue() {
        
        String attrPath = TEST_BASE_PATH+":show_me_the_value";
        Uri    attrUrl  = Uri.parse(Constants.Gom.URI + attrPath);
        String value    = GomHttpWrapper.getAttributeValue(attrUrl);
        
        assertEquals("attribute value wasn't as expected", "lalala", value);
    }
}
