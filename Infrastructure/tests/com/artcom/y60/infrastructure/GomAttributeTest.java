package com.artcom.y60.infrastructure;

import android.test.AndroidTestCase;
import android.util.Log;

public class GomAttributeTest extends AndroidTestCase {

    // Instance Variables ------------------------------------------------

    private GomAttribute mTestAttr;
    
    
    
    
    // Public Instance Methods -------------------------------------------

    public void setUp() {
        
        GomRepository repos = new GomRepository(GomTestConstants.TEST_REPOSITORY_URI);
        mTestAttr = (GomAttribute)repos.getEntry("/users/orange:active_mood");
    }

    
    public void testGetValue() {

        assertEquals("/users/orange/personal_storage:mood_002", mTestAttr.getValue());
    }
    
    
    public void testGetPath() {
        
        assertEquals("/users/orange:active_mood", mTestAttr.getPath());
    }
    
    
    public void testGetName() {
        
        assertEquals("active_mood", mTestAttr.getName());
    }
    
    
    public void testResolveReference() {
        
        GomEntry entry = mTestAttr.resolveReference();
        
        assertNotNull(entry);
        
        Log.v("GomAttributeTest", "resolveReference got entry with path "+entry.getPath());
        
        assertEquals("/users/orange/personal_storage:mood_002", entry.getPath());
        assertTrue(entry instanceof GomAttribute);
        assertEquals("http://storage.service.t-gallery.act/moods/italian.xml",
                     ((GomAttribute)entry).getValue());
    }
}
