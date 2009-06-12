package com.artcom.y60;

import android.app.Activity;

public abstract class Y60Activity extends Activity {

    public abstract boolean hasBackendAvailableBeenCalled();

    public abstract boolean hasResumeWithBackendBeenCalled();

}
