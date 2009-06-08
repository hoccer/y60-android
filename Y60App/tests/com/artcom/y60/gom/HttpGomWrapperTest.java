package com.artcom.y60.gom;

import junit.framework.TestCase;
import android.net.Uri;

import com.artcom.y60.Constants;
import com.artcom.y60.HTTPHelper;

public class HttpGomWrapperTest extends TestCase {

    private static final String TEST_BASE_PATH = "/test/android/y60/infrastructure_gom/gom_http_wrapper_test";
    
    private static final String ATTR_URL = Constants.Gom.URI + TEST_BASE_PATH + ":show_me_the_value";
    
    private static final String ATTR_VALUE = "tralala";
    
    public HttpGomWrapperTest() {
        
        HTTPHelper.putXML(ATTR_URL, "<attribute>"+ATTR_VALUE+"</attribute>");
    }
    
    public void testGetAttributeValue() {
        
        String value = GomHttpWrapper.getAttributeValue(Uri.parse(ATTR_URL));
        
        assertEquals("attribute value wasn't as expected", ATTR_VALUE, value);
    }
}
