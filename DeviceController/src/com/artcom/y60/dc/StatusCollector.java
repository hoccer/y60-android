package com.artcom.y60.dc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artcom.y60.DeviceConfiguration;
import com.artcom.y60.Logger;
import com.artcom.y60.dc.StatusWatcher.ScreenState;
import com.artcom.y60.gom.GomAttribute;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.GomProxyHelper;

class StatusCollector extends BroadcastReceiver {
    private static final String LOG_TAG = "StatusCollector";

    private ScreenState         m_screenState;

    private GomProxyHelper      mGom;

    private DeviceConfiguration mDeviceConfiguration;

    public StatusCollector(GomProxyHelper pGom, DeviceConfiguration pDeviceConfiguration) {
        mGom = pGom;
        mDeviceConfiguration = pDeviceConfiguration;
        m_screenState = ScreenState.UNKNOWN;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        try {
            GomNode device = mGom.getNode(mDeviceConfiguration.getDevicePath());
            GomAttribute screenAttribute = device.getOrCreateAttribute("screen_status");

            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                m_screenState = ScreenState.ON;
                screenAttribute.putValue("on");
                Logger.i(LOG_TAG, "Received ScreenOn event");
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                m_screenState = ScreenState.OFF;
                screenAttribute.putValue("off");
                Logger.i(LOG_TAG, "Received ScreenOff event");
            }
        } catch (Exception e) {
            // TODO this is rather ugly and will remain so until the refactoring
            // of the
            // scattered RuntimeExceptions throughout is complete.

            // we write something to the logfile, but otherwise ignore this.
            // it's most
            // likely a transient network error

            Logger.w(LOG_TAG, "Could not update status entries in GOM");
        }
    }

    public ScreenState getScreenState() {
        return m_screenState;
    }
}