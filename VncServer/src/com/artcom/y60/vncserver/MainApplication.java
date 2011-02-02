package com.artcom.y60.vncserver;

import java.io.IOException;
import java.io.OutputStream;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.IoHelper;
import com.artcom.y60.Logger;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

public class MainApplication extends Application {

    private static String LOG_TAG = "VNCServerMainApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();

        if (firstRun()) {
            Logger.v(LOG_TAG, "firstRun is true");
            createBinary();
        } else {
            Logger.v(LOG_TAG, "firstRun is false");
        }
    }

    public boolean firstRun() {
        Logger.v(LOG_TAG, "firstRun");
        int versionCode = 0;
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_META_DATA).versionCode;
        } catch (NameNotFoundException e) {
            Logger.e(LOG_TAG,
                    "Package not found... Odd, since we're in that package...",
                    e);
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        int lastFirstRun = prefs.getInt("last_run", 0);

        if (lastFirstRun >= versionCode) {
            Logger.d(LOG_TAG, "Not first run");
            return false;
        }
        Logger.d(LOG_TAG, "First run for version " + versionCode);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("last_run", versionCode);
        editor.commit();
        return true;
    }

    public void createBinary() {
        try {
            IoHelper.copyRawResourceToPath(R.raw.androidvncserver, getFilesDir().getAbsolutePath()
                + "/androidvncserver", getResources());
            IoHelper.copyRawResourceToPath(R.raw.vncviewer, getFilesDir().getAbsolutePath()
                + "/VncViewer.jar", getResources());
            IoHelper.copyRawResourceToPath(R.raw.indexvnc, getFilesDir().getAbsolutePath()
                + "/index.vnc", getResources());

            Process sh;
            sh = Runtime.getRuntime().exec("su");
            OutputStream os = sh.getOutputStream();
            IoHelper.writeCommand(os, "killall androidvncserver");
            IoHelper.writeCommand(os, "killall -KILL androidvncserver");
            // chmod 777 SHOULD exist
            IoHelper.writeCommand(os, "chmod 777 " + getFilesDir().getAbsolutePath() + "/androidvncserver");
            os.close();
        } catch (IOException e) {
            ErrorHandling.signalIOError(LOG_TAG, e, this);
        } catch (Exception e) {
            ErrorHandling.signalError(LOG_TAG, e, this, ErrorHandling.Category.COMMAND_EXECUTION);
        }
    }

}
