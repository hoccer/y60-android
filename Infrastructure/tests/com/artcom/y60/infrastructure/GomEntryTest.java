package com.artcom.y60.infrastructure;

import org.json.JSONObject;

import android.test.AndroidTestCase;

public class GomEntryTest extends AndroidTestCase {

    // Constants ---------------------------------------------------------

    static final String FIXTURES = GomTestConstants.FIXTURES+"gom_entry_test";
    
    static final String NODE_NAME = "node";
    
    static final String NODE_PATH = FIXTURES+"/"+NODE_NAME;
    
    static final String ATTR_NAME = "attribute";
    
    static final String ATTR_PATH = FIXTURES+":"+ATTR_NAME;
    
    
    
    // Public Instance Methods -------------------------------------------

    public void testAttrFromJson() throws Exception {
        
        JSONObject    jsAttr = HTTPHelper.getJson(GomTestConstants.TEST_REPOSITORY_URI.resolve(ATTR_PATH).toString());
        GomRepository repos  = new GomRepository(GomTestConstants.TEST_REPOSITORY_URI);
        GomEntry      entry  = GomEntry.fromJson(jsAttr, repos);
        
        assertNotNull(entry);
        assertTrue(entry instanceof GomAttribute);
        assertEquals(ATTR_PATH, entry.getPath());
        assertEquals(ATTR_NAME, entry.getName());
    }
    
    
    public void testNodeFromJson() throws Exception {
        
        JSONObject    jsAttr = HTTPHelper.getJson(GomTestConstants.TEST_REPOSITORY_URI.resolve(NODE_PATH).toString());
        GomRepository repos  = new GomRepository(GomTestConstants.TEST_REPOSITORY_URI);
        GomEntry      entry  = GomEntry.fromJson(jsAttr, repos);
        
        assertNotNull(entry);
        assertTrue(entry instanceof GomNode);
        assertEquals(NODE_PATH, entry.getPath());
        assertEquals(NODE_NAME, entry.getName());
    }
}
