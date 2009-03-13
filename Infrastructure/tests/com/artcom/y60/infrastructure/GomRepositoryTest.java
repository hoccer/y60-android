package com.artcom.y60.infrastructure;

import android.test.AndroidTestCase;

public class GomRepositoryTest extends AndroidTestCase {

    // Constants ---------------------------------------------------------

    private static final String FIXTURES = GomTestConstants.FIXTURES+"gom_repository_test/";
    
    

    // Instance Variables ------------------------------------------------

    private GomRepository mRepos;
    
    

    // Public Instance Methods -------------------------------------------

    public void setUp() {
        
        mRepos = new GomRepository(GomTestConstants.TEST_REPOSITORY_URI);
    }
    
    
    public void testGetBaseUri() throws Exception {
        
        assertEquals(GomTestConstants.TEST_REPOSITORY_URI, mRepos.getBaseUri());
    }
    
    
    public void testGetNode() {
        
        String name = "node";
        String path = FIXTURES+name;
        GomEntry entry = mRepos.getEntry(path);
        
        assertNotNull(entry);
        assertEquals(name, entry.getName());
        assertEquals(path, entry.getPath());
        assertTrue(entry instanceof GomNode);
    }
    
    
    public void testGetAttribute() {
        
        String name = "attribute";
        String path = FIXTURES+"node:"+name;
        GomEntry entry = mRepos.getEntry(path);
        
        assertNotNull(entry);
        assertEquals(name, entry.getName());
        assertEquals(path, entry.getPath());
        assertTrue(entry instanceof GomAttribute);
    }
}
