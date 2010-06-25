package com.artcom.y60.hoccer;

import java.util.Map;

import com.artcom.y60.Logger;

import android.content.Context;
import android.os.Build;

public class Peer10 extends Peer {

    private static final String LOG_TAG = "Peer10";

    public Peer10(String clientName, String remoteServer, Context context) {
        super(clientName, remoteServer, context);
        Logger.v(LOG_TAG, "is created");
    }

    public Map<String, String> getEventDnaParameters() {
        Map<String, String> parameters = super.getEventDnaParameters();
        parameters.put("event[" + Parameter.VERSION_SDK + "]", String.valueOf(Build.VERSION.SDK));

        // parameters.put("event[" + Parameter.MANUFACTURER + "]", Build.MANUFACTURER);
        // parameters.put("event[" + Parameter.VERSION_SDK + "]", String
        // .valueOf(Build.VERSION.SDK_INT));

        Logger.v(LOG_TAG, parameters);

        return parameters;
    }
}
