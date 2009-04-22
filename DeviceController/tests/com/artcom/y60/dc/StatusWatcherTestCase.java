package com.artcom.y60.dc;

import android.test.ServiceTestCase;

public class StatusWatcherTestCase extends ServiceTestCase<StatusWatcher> {

    public StatusWatcherTestCase(Class<StatusWatcher> pServiceClass) {
        super(pServiceClass);
        // TODO Auto-generated constructor stub
    }
    
    // Verifies that the service correctly updates the device's
    // history log in the GOM
    public void testHistoryUpdates() {
        // TODO
    }
    
    // Verifies that the service correctly updates the device's
    // "last updated" entry in the GOM
    public void testLastAliveUpdates() {
        // TODO
    }
    
    // Verifies that the service correctly updates the GOM if the
    // device's screen is turned on
    public void testScreenOnUpdate() {
        // TODO
    }
    
    // Verifies that the service correctly updates the GOM if the
    // device's screen is turned off
    public void testScreenOffUpdate() {
        // TODO
    }
}
