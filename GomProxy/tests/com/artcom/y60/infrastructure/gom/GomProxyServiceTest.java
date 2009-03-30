package com.artcom.y60.infrastructure.gom;

import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.test.ServiceTestCase;

public class GomProxyServiceTest extends ServiceTestCase<GomProxyService> {
    
    // Instance Variables ------------------------------------------------

    private Intent mIntent;
    
    
    
    // Constructors ------------------------------------------------------

    public GomProxyServiceTest() {
        
        super(GomProxyService.class);
    }


    
    // Public Instance Methods -------------------------------------------

    public void testGetBaseUri() throws Exception {
        
        startService(mIntent);
        
        GomProxyService service = getService();
        assertNotNull("service must not be null", service);
        
        assertEquals("http://t-gom.service.t-gallery.act", service.getBaseUri());
    }
    
    
    public void testGetAttribute() throws Exception {
        
        startService(mIntent);
        
        GomProxyService service = getService();
        assertNotNull("service must not be null", service);
        
        String attrPath = "/test/android/y60/infrastructure_gom/gom_proxy_service_test:attribute";
        assertEquals("honolulu", service.getAttributeValue(attrPath));
        assertTrue(service.hasAttributeInCache(attrPath));
    }

    
    public void testGetNode() throws Exception {
        
        startService(mIntent);
        
        GomProxyService service = getService();
        assertNotNull("service must not be null", service);
        
        List<String> subNodeNames   = new LinkedList<String>();
        List<String> attributeNames = new LinkedList<String>();
        String nodePath = "/test/android/y60/infrastructure_gom/gom_proxy_service_test/node/";
        service.getNodeData(nodePath, subNodeNames, attributeNames);
        
        assertTrue(service.hasNodeInCache(nodePath));
        
        assertEquals(1, subNodeNames.size());
        assertEquals("a_sub_node", subNodeNames.get(0));
        
        assertEquals(1, attributeNames.size());
        assertEquals("an_attribute", attributeNames.get(0));
        
        assertEquals("honolulu", service.getAttributeValue("/test/android/y60/infrastructure_gom/gom_proxy_service_test:attribute"));
    }
    
    
    
    
    // Protected Instance Methods ----------------------------------------

    protected void setUp() throws Exception {

        super.setUp();
        mIntent = new Intent(getContext(), GomProxyService.class);
    }


    protected void tearDown() throws Exception {

        super.tearDown();
    }

}
