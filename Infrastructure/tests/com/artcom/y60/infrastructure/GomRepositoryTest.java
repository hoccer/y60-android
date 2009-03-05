package com.artcom.y60.infrastructure;

import android.test.AndroidTestCase;

public class GomRepositoryTest extends AndroidTestCase {

    // Instance Variables ------------------------------------------------

    private GomRepository mRepos;
    
    

    // Public Instance Methods -------------------------------------------

    public void setUp() {
        
        mRepos = new GomRepository(GomTestConstants.TEST_REPOSITORY_URI);
    }
    
    
    public void testGetBaseUri() throws Exception {
        
        assertEquals(GomTestConstants.TEST_REPOSITORY_URI, mRepos.getBaseUri());
    }
    
    
    public void testGetATopLevelNode() {
        
        GomEntry entry = mRepos.getEntry("/users");
        
        assertNotNull(entry);
        assertEquals("users", entry.getName());
        assertEquals("/users", entry.getPath());
        assertTrue(entry instanceof GomNode);
    }
    
    
    public void testGetAttribute() {
        
        GomEntry entry = mRepos.getEntry("/users/orange/configuration:background");
        
        assertNotNull(entry);
        assertEquals("background", entry.getName());
        assertEquals("/users/orange/configuration:background", entry.getPath());
        assertTrue(entry instanceof GomAttribute);
    }
    
    
    public void testGetLowerLevelNode() {
        
        GomEntry entry = mRepos.getEntry("/users/orange/configuration");
        
        assertNotNull(entry);
        assertEquals("configuration", entry.getName());
        assertEquals("/users/orange/configuration", entry.getPath());
        assertTrue(entry instanceof GomNode);
    }

}
