package com.artcom.y60;

import com.artcom.y60.gom.Y60GomService;

import android.content.Intent;
import android.os.IBinder;

public class DemoY60GomService extends Y60GomService {

    @Override
    public IBinder onBind(Intent pIntent) {
        // no binding allowed for this service
        return null;
    }

    @Override
    protected void bindProxys() {
    }
}
