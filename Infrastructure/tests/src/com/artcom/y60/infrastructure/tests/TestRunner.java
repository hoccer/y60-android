package com.artcom.y60.infrastructure.tests;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

public class TestRunner extends InstrumentationTestRunner {
	
	@Override
    public TestSuite getAllTests() {
        InstrumentationTestSuite suite = new InstrumentationTestSuite(this);

        suite.addTestSuite(GOMClientTest.class);
     
        return suite;
    }

    @Override
    public ClassLoader getLoader() {
        return TestRunner.class.getClassLoader();
    }


}
