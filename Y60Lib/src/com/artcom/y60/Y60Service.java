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

    protected boolean monitorMyLifecycleOnSdcard() {
        return true;
    }

    private void writeMyLifecycleOnSdcard() {
        if (monitorMyLifecycleOnSdcard()) {

            File f = new File(Constants.Device.ALIVE_SERVICES_PATH);
            if (f.exists() == false) {
                f.mkdirs();
            }

            FileWriter fw;
            try {
                fw = new FileWriter(Constants.Device.ALIVE_SERVICES_PATH + "/"
                        + getClass().getName());
                fw.write("");
                fw.flush();
                fw.close();
                Logger.v(LOG_TAG, "____ Wrote: ", Constants.Device.ALIVE_SERVICES_PATH + "/"
                        + getClass().getName(), " on sdcard");
            } catch (IOException e) {
                ErrorHandling.signalIOError(LOG_TAG, e, this);
            }
        }
    }

    protected void deleteMyLifecycleFromSdcard() {
        boolean deletedMySelf = false;
        if (monitorMyLifecycleOnSdcard()) {
            String aliveServicesDirectory = Constants.Device.ALIVE_SERVICES_PATH;

            File dir = new File(aliveServicesDirectory);
            String[] children = dir.list();
            if (children == null) {
                Logger.e(LOG_TAG, "No services at all listed in alive services on sdcard");
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
            if (deletedMySelf) {
                return;
            }
            Logger.e(LOG_TAG, "I am not listed in alive services on sdcard");
        }
        Logger.e(LOG_TAG, "Not monitoring: ", getClass().getName());
    }

    @Override
    public void onDestroy() {
        Logger.d(LOG_TAG, "------------ onDestroy called for service ", getClass(),
                " which is monitored? ", monitorMyLifecycleOnSdcard());
        if (mShutdownReceiver != null) {
            unregisterReceiver(mShutdownReceiver);
        }
        deleteMyLifecycleFromSdcard();
        super.onDestroy();
    }

    protected void kill() {
        Logger.i(LOG_TAG, "stopping service: ", getClass().getName());
        stopSelf();
    }

}
