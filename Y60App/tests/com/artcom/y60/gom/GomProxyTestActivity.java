package com.artcom.y60.gom;

import com.artcom.y60.Y60Activity;

public class GomProxyTestActivity extends Y60Activity {

    @Override
    public boolean hasBackendAvailableBeenCalled() {
        throw new AssertionError("not yet implemented");
    }

    @Override
    public boolean hasResumeWithBackendBeenCalled() {
        throw new AssertionError("not yet implemented");
    }

}
