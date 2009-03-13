package com.artcom.y60.infrastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.test.AndroidTestCase;

public class GomNodeTest extends AndroidTestCase {

    // Constants ---------------------------------------------------------

    static final String NAME = "node";
    
    static final String PATH = GomTestConstants.FIXTURES+"gom_node_test/"+NAME;
    
    static final String ATTR_NAME = "attribute";
    
    static final String ATTR_VALUE = "value";
    
    static final String ATTR_PATH = PATH+":"+ATTR_NAME;
    
    static final String CHILD_NAME = "child";
    
    static final String CHILD_PATH = PATH+"/"+CHILD_NAME;
    

    
    // Instance Variables ------------------------------------------------

    private GomNode mTestNode;
    
    
    
    
    // Public Instance Methods -------------------------------------------

    public void setUp() {
        
        GomRepository repos = new GomRepository(GomTestConstants.TEST_REPOSITORY_URI);
        mTestNode = (GomNode)repos.getEntry(PATH);
    }
    
    
    public void testGetName() {
        
        assertEquals(NAME, mTestNode.getName());
    }
    
    
    public void testGetPath() {
        
        assertEquals(PATH, mTestNode.getPath());
    }
    
    
    public void testGetChildNode() {
        
        GomEntry entry = mTestNode.getNode(CHILD_NAME);
        
        assertNotNull(entry);
        assertEquals(CHILD_NAME, entry.getName());
        assertEquals(CHILD_PATH, entry.getPath());
        assertTrue(entry instanceof GomNode);
    }
    
    
    public void testGetAttribute() {
     
        GomEntry entry = mTestNode.getEntry(ATTR_NAME);
        
        assertNotNull(entry);
        assertEquals(ATTR_NAME, entry.getName());
        assertEquals(ATTR_PATH, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals(ATTR_VALUE, ((GomAttribute)entry).getValue());
    }
    
    
    public void testGetEntry() {
        
        GomEntry entry = mTestNode.getEntry(CHILD_NAME);
        assertNotNull(entry);
        assertEquals(CHILD_NAME, entry.getName());
        assertEquals(CHILD_PATH, entry.getPath());
        assertTrue(entry instanceof GomNode);
        
        entry = mTestNode.getEntry(ATTR_NAME);
        assertNotNull(entry);
        assertEquals(ATTR_NAME, entry.getName());
        assertEquals(ATTR_PATH, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals(ATTR_VALUE, ((GomAttribute)entry).getValue());
    }
    
    
    public void testAttributes() {
     
        Set<GomAttribute> attrs = mTestNode.attributes();
        
        Map<String, String> expected = new HashMap<String, String>();
        expected.put(ATTR_NAME, ATTR_VALUE);
        
        assertEquals(expected.size(), attrs.size());
        
        for (GomAttribute attr: attrs) {
            
            assertTrue(expected.containsKey(attr.getName()));
            assertEquals(expected.get(attr.getName()), attr.getValue());
        }
    }
    
    
    public void testEntries() {
     
        Set<GomEntry> entries = mTestNode.entries();
        
        Map<String, String> expAtts = new HashMap<String, String>();
        expAtts.put(ATTR_NAME, ATTR_VALUE);
        Set<String> expNodes = new HashSet<String>();
        expNodes.add(CHILD_NAME);
        
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
        expected.add(ATTR_NAME);
        expected.add(CHILD_NAME);
        
        assertEquals(expected.size(), keys.size());
        
        for (String key: keys) {
            
            assertTrue(expected.contains(key));
        }
    }
    
    
    public void testNodes() {
     
        Set<GomNode> nodes = mTestNode.nodes();
        
        Set<String> expected = new HashSet<String>();
        expected.add(CHILD_NAME);
        
        assertEquals(expected.size(), nodes.size());
        
        for (GomNode node: nodes) {
            
            assertTrue(expected.contains(node.getName()));
        }
    }
}
