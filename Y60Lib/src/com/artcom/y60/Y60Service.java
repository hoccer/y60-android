package com.artcom.y60;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class Y60Service extends Service {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "Y60Service";

    private BroadcastReceiver   mShutdownReceiver;

    // Public Instance Methods -------------------------------------------

    @Override
    public void onCreate() {

        writeMyLifecycleOnSdcard();

        mShutdownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {
                Logger.v(LOG_TAG, "stop self");
                kill();
            }
        };
        registerReceiver(mShutdownReceiver, new IntentFilter(Y60Action.SHUTDOWN_SERVICES_BC));

        Logger.v(LOG_TAG, "------------ onCreate called for service ", getClass());

        super.onCreate();
    }

    boolean monitorMyLifecycleOnSdcard() {
        return true;
    }

    private void writeMyLifecycleOnSdcard() {
        if (monitorMyLifecycleOnSdcard()) {
            FileWriter fw;
            try {
                fw = new FileWriter(Constants.Device.ALIVE_SERVICES_PATH + "/"
                        + getClass().getName());
                fw.close();
                Logger.v(LOG_TAG, "Wrote: ", Constants.Device.ALIVE_SERVICES_PATH + "/"
                        + getClass().getName(), " on sdcard");
            } catch (IOException e) {
                ErrorHandling.signalIOError(LOG_TAG, e, this);
            }
        }
    }

    private void deleteMyLifecycleFromSdcard() {
        boolean deletedMySelf = false;
        if (monitorMyLifecycleOnSdcard()) {
            String aliveServicesDirectory = Constants.Device.ALIVE_SERVICES_PATH;

            File dir = new File(aliveServicesDirectory);
            String[] children = dir.list();
            if (children == null) {
                ErrorHandling.signalServiceError(LOG_TAG, new Exception(
                        "No services at all listed in alive services on sdcard"), this);
            } else {
                for (String filename : children) {
                    Logger.v(LOG_TAG, "deleteMyLifecycleFromSdcard: ", filename, ", i am: ",
                            getClass().getName());

                    if (filename.equals(getClass().getName())) {
                        File myClassName = new File(aliveServicesDirectory + "/" + filename);
                        myClassName.delete();
                        Logger.v(LOG_TAG, "deleted: ", filename);
                        deletedMySelf = true;
                    }
                }
            }
        }
        if (deletedMySelf) {
            return;
        }
        ErrorHandling.signalServiceError(LOG_TAG, new Exception(
                "No services at all listed in alive services on sdcard"), this);
    }

    @Override
    public void onDestroy() {

        if (mShutdownReceiver != null) {
            unregisterReceiver(mShutdownReceiver);
        }
        Logger.d(LOG_TAG, "------------ onDestroy called for service ", getClass());

        deleteMyLifecycleFromSdcard();
        super.onDestroy();
    }

    protected void kill() {
        Logger.i(LOG_TAG, "stopping service: ", getClass().getName());
        stopSelf();
    }

}
