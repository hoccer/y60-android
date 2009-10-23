package com.artcom.y60;

import junit.framework.TestCase;

public class NetworkHelperTest extends TestCase {

    public void testGettingStagingIp() throws Exception {

        assertTrue(
                "Device should not have local address as staging ip, but was " + NetworkHelper.getStagingIp(),
                !NetworkHelper.getStagingIp().toString().contains("172.0.0.1"));

        assertTrue(
                "Device should not have an emulator address as staging ip, but was " + NetworkHelper.getStagingIp(),
                !NetworkHelper.getStagingIp().toString().contains("10.0.2"));

    }

}
