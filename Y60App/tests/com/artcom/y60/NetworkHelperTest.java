package com.artcom.y60;

import junit.framework.TestCase;

public class NetworkHelperTest extends TestCase {

    public void testGettingStagingIp() throws Exception {

        assertTrue("Device should have a valid staging ip", NetworkHelper.getStagingIp().toString()
                .contains("192.168.9."));
    }

}
