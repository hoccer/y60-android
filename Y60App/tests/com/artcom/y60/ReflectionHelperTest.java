package com.artcom.y60;

import junit.framework.TestCase;

public class ReflectionHelperTest extends TestCase {

    public void testThisMethodName() throws Exception {

        assertEquals("testThisMethodName", ReflectionHelper.thisMethodName());
    }

    public void testCallingMethodName() throws Exception {

        helper();
    }

    private void helper() throws Exception {

        assertEquals("testCallingMethodName", ReflectionHelper.callingMethodName());
    }
}
