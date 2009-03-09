package com.artcom.y60.infrastructure;

import android.test.AndroidTestCase;
import android.util.Log;

public class GomAttributeTest extends AndroidTestCase {

    // Constants ---------------------------------------------------------

    static final String PATH = "/users/orange:active_mood";
    
    static final String VALUE = "/users/orange/personal_storage:mood_001"; 
    

    
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
        
        assertEquals("active_mood", mTestAttr.getName());
    }
    
    
    public void testResolveReference() {
        
        GomEntry entry = mTestAttr.resolveReference();
        
        assertNotNull(entry);
        
        Log.v("GomAttributeTest", "resolveReference got entry with path "+entry.getPath());
        
        assertEquals(VALUE, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals("http://storage.service.t-gallery.act/moods/cinema.xml",
                     ((GomAttribute)entry).getValue());
    }
}
