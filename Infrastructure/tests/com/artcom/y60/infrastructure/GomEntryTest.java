package com.artcom.y60.infrastructure;

import java.net.URI;

import org.json.JSONObject;

import android.test.AndroidTestCase;

public class GomEntryTest extends AndroidTestCase {

    // Public Instance Methods -------------------------------------------

    public void testAttrFromJson() throws Exception {
        
        JSONObject    jsAttr = HTTPHelper.getJson("http://t-gom.service.t-gallery.act/users/orange:active_mood");
        GomRepository repos  = new GomRepository(new URI("http://t-gom.service.t-gallery.act"));
        GomEntry      entry  = GomEntry.fromJson(jsAttr, repos);
        
        assertNotNull(entry);
        assertTrue(entry instanceof GomAttribute);
        assertEquals("/users/orange:active_mood", entry.getPath());
        assertEquals("active_mood", entry.getName());
    }
    
    
    public void testNodeFromJson() throws Exception {
        
        JSONObject    jsAttr = HTTPHelper.getJson("http://t-gom.service.t-gallery.act/users/orange");
        GomRepository repos  = new GomRepository(new URI("http://t-gom.service.t-gallery.act"));
        GomEntry      entry  = GomEntry.fromJson(jsAttr, repos);
        
        assertNotNull(entry);
        assertTrue(entry instanceof GomNode);
        assertEquals("/users/orange", entry.getPath());
        assertEquals("orange", entry.getName());
    }
}
