package com.artcom.y60.infrastructure;

import android.test.AndroidTestCase;
import android.util.Log;

public class GomAttributeTest extends AndroidTestCase {

    // Constants ---------------------------------------------------------

    static final String NAME = "attribute";
    
    static final String PATH = GomTestConstants.FIXTURES+"gom_attribute_test:"+NAME;
    
    static final String VALUE = PATH; 
    

    
    // Instance Variables ------------------------------------------------

    private GomAttribute mTestAttr;
    
    
    
    
    // Public Instance Methods -------------------------------------------

    public void setUp() {
        
        GomRepository repos = new GomRepository(GomTestConstants.TEST_REPOSITORY_URI);
        mTestAttr = (GomAttribute)repos.getEntry(PATH);
    }

    
    public void testGetValue() {

        assertEquals(VALUE, mTestAttr.getValue());
    }
    
    
    public void testGetPath() {
        
        assertEquals(PATH, mTestAttr.getPath());
    }
    
    
    public void testGetName() {
        
        assertEquals(NAME, mTestAttr.getName());
    }
    
    
    public void testResolveReference() {
        
        GomEntry entry = mTestAttr.resolveReference();
        
        assertNotNull(entry);
        
        Log.v("GomAttributeTest", "resolveReference got entry with path "+entry.getPath());
        
        assertEquals(VALUE, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals(VALUE, ((GomAttribute)entry).getValue());
    }
}
