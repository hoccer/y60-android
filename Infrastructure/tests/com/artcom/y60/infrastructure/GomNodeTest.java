package com.artcom.y60.infrastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.test.AndroidTestCase;

public class GomNodeTest extends AndroidTestCase {

    // Instance Variables ------------------------------------------------

    private GomNode mTestNode;
    
    
    
    
    // Public Instance Methods -------------------------------------------

    public void setUp() {
        
        GomRepository repos = new GomRepository(GomTestConstants.TEST_REPOSITORY_URI);
        mTestNode = (GomNode)repos.getEntry("/tours/development/users/orange");
    }
    
    
    public void testGetName() {
        
        assertEquals("orange", mTestNode.getName());
    }
    
    
    public void testGetPath() {
        
        assertEquals("/users/orange", mTestNode.getPath());
    }
    
    
    public void testGetChildNode() {
        
        GomEntry entry = mTestNode.getNode("configuration");
        
        assertNotNull(entry);
        assertEquals("configuration", entry.getName());
        assertEquals("/users/orange/configuration", entry.getPath());
        assertTrue(entry instanceof GomNode);
    }
    
    
    public void testGetAttribute() {
     
        GomEntry entry = mTestNode.getEntry("colour_code");
        
        assertNotNull(entry);
        assertEquals("colour_code", entry.getName());
        assertEquals("/users/orange:colour_code", entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals("orange", ((GomAttribute)entry).getValue());
    }
    
    
    public void testGetEntry() {
        
        GomEntry entry = mTestNode.getEntry("inbox");
        
        assertNotNull(entry);
        assertEquals("inbox", entry.getName());
        assertEquals("/users/orange/inbox", entry.getPath());
        assertTrue(entry instanceof GomNode);
        
        entry = mTestNode.getEntry("active_profile");
        
        assertNotNull(entry);
        assertEquals("active_profile", entry.getName());
        assertEquals("/users/orange:active_profile", entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals("home", ((GomAttribute)entry).getValue());
    }
    
    
    public void testAttributes() {
     
        Set<GomAttribute> attrs = mTestNode.attributes();
        
        Map<String, String> expected = new HashMap<String, String>();
        
        // test data...?!
        expected.put("colour_code", "orange");
        expected.put("active_mood", "/users/orange/personal_storage:mood_001");
        expected.put("party", "http://storage.service.t-gallery.act/parties/01.xml");
        expected.put("active_profile", "home");
        
        assertEquals(expected.size(), attrs.size());
        
        for (GomAttribute attr: attrs) {
            
            assertTrue(expected.containsKey(attr.getName()));
            assertEquals(expected.get(attr.getName()), attr.getValue());
        }
    }
    
    
    public void testEntries() {
     
        Set<GomEntry> entries = mTestNode.entries();
        
        Map<String, String> expAtts = new HashMap<String, String>();
        expAtts.put("colour_code", "orange");
        expAtts.put("active_mood", "/users/orange/personal_storage:mood_001");
        expAtts.put("party", "http://storage.service.t-gallery.act/parties/01.xml");
        expAtts.put("active_profile", "home");
        Set<String> expNodes = new HashSet<String>();
        expNodes.add("personal_storage");
        expNodes.add("inbox");
        expNodes.add("configuration");
        expNodes.add("address_book");
        
        assertEquals(expAtts.size()+expNodes.size(), entries.size());
        
        for (GomEntry entry: entries) {
            
            if (entry instanceof GomAttribute) {
                
                GomAttribute attr = (GomAttribute)entry;
                assertTrue(expAtts.containsKey(attr.getName()));
                assertEquals(expAtts.get(attr.getName()), attr.getValue());
            }
        }
        
        for (GomEntry entry: entries) {
            
            if (entry instanceof GomNode) {
            
                GomNode node = (GomNode)entry;
                assertTrue(expNodes.contains(node.getName()));
            }
        }
    }
    
    
    public void testKeys() {
     
        Set<String> keys = mTestNode.entryNames();
        
        Set<String> expected = new HashSet<String>();
        expected.add("colour_code");
        expected.add("active_mood");
        expected.add("party");
        expected.add("active_profile");
        expected.add("personal_storage");
        expected.add("inbox");
        expected.add("configuration");
        expected.add("address_book");
        
        assertEquals(expected.size(), keys.size());
        
        for (String key: keys) {
            
            assertTrue(expected.contains(key));
        }
    }
    
    
    public void testNodes() {
     
        Set<GomNode> nodes = mTestNode.nodes();
        
        Set<String> expected = new HashSet<String>();
        
        // test data...?!
        expected.add("personal_storage");
        expected.add("inbox");
        expected.add("configuration");
        expected.add("address_book");
        
        assertEquals(expected.size(), nodes.size());
        
        for (GomNode node: nodes) {
            
            assertTrue(expected.contains(node.getName()));
        }
    }
}
