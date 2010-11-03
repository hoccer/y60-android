/*
 * Copyright (C) 1993-2008, ART+COM AG Berlin, Germany <www.artcom.de> These coded instructions,
 * statements, and computer programs contain proprietary information of ART+COM AG Berlin, and are
 * copy protected by law. They may be used, modified and redistributed under the terms of GNU
 * General Public License referenced below. Alternative licensing without the obligations of the GPL
 * is available upon request. GPL v3 Licensing: This file is part of the ART+COM Y60 Platform.
 * ART+COM Y60 is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. ART+COM Y60 is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with ART+COM Y60. If not, see <http:
 * * www.gnu.org/licenses/>.
 */

package com.artcom.y60;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Y60 extends Activity {

    private static final String                LOG_TAG  = "Y60";

    private EditText                           mDeviceIdEdit;
    private Button                             mSetDeviceIdButton;
    private Button                             mInitButton;
    private Button                             mPreloadButton;
    private Button                             mStopDcButton;
    private Button                             mWifiCfgButton;

    private Spinner                            mChooseHomeButtonTarget;
    private ArrayAdapter<ComponentInformation> mCompInfoArrayAdapter;

    private Spinner                            mChooseLogLevel;
    private ArrayAdapter<String>               mLogLevelArrayAdapter;

    public static final String                 INIT_APP = "tgallery.intent.INIT_APP";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.y60_layout);

        mDeviceIdEdit = (EditText) findViewById(R.id.device_path_edit);
        mDeviceIdEdit.setText(DeviceConfiguration.load().getDevicePath());

        mSetDeviceIdButton = (Button) findViewById(R.id.set_device_path_button);
        mSetDeviceIdButton.setOnClickListener(new RenameClickListener());

        mWifiCfgButton = (Button) findViewById(R.id.wifi_config_button);
        mWifiCfgButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent
                        .setClassName("com.android.settings",
                                "com.android.settings.WirelessSettings");
                startActivity(intent);
            }
        });

        mInitButton = (Button) findViewById(R.id.init_y60_button);
        mInitButton.setOnClickListener(new OnClickListener() {
            // @Override
            public void onClick(View v) {
                startService(new Intent("tgallery.intent.TG_INIT_SERVICE"));
            }
        });

        mPreloadButton = (Button) findViewById(R.id.preload_cache_button);
        mPreloadButton.setOnClickListener(new OnClickListener() {
            // @Override
            public void onClick(View v) {
                Intent intent = new Intent(Y60Action.PRELOAD_CACHE);
                sendBroadcast(intent);
            }
        });

        mStopDcButton = (Button) findViewById(R.id.stop_dc_button);
        mStopDcButton.setOnClickListener(new OnClickListener() {
            // @Override
            public void onClick(View v) {
                Intent stopDcIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
                stopService(stopDcIntent);
                Intent stopSwIntent = new Intent("y60.intent.SERVICE_STATUS_WATCHER");
                stopService(stopSwIntent);
            }
        });

        mChooseHomeButtonTarget = (Spinner) findViewById(R.id.home_target_chooser);
        // display possible components:
        ComponentInformation[] components = IntentHelper.getPossibleComponents(Intent.ACTION_MAIN,
                this, Intent.CATEGORY_HOME, Intent.CATEGORY_DEFAULT);
        mCompInfoArrayAdapter = new ArrayAdapter<ComponentInformation>(this,
                android.R.layout.simple_spinner_dropdown_item, components);
        mChooseHomeButtonTarget.setAdapter(mCompInfoArrayAdapter);

        mChooseHomeButtonTarget.setSelection(0);
        mChooseHomeButtonTarget.setOnItemSelectedListener(new ActivitySelectionListener());

        mChooseLogLevel = (Spinner) findViewById(R.id.log_level_chooser);
        List<Logger.Level> logLevels = Logger.Level.getLogLevels();
        String[] logLevelNames = new String[logLevels.size()];
        int selectedLevelIndex = -1;
        for (int i = 0; i < logLevels.size(); i += 1) {

            logLevelNames[i] = logLevels.get(i).name();

            if (logLevels.get(i) == DeviceConfiguration.load().getLogLevel()) {
                selectedLevelIndex = i;
            }
        }

        if (selectedLevelIndex == -1) {
            selectedLevelIndex = 0;
            String selectedLevelName = logLevels.get(selectedLevelIndex).name();
            Logger.w(LOG_TAG,
                    "apparantly no log level is configured in the device configuration, using ",
                    selectedLevelName);

        } else {

            Logger.v(LOG_TAG, "log level ", logLevelNames[selectedLevelIndex], " selected");
        }

        mLogLevelArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, logLevelNames);
        mChooseLogLevel.setAdapter(mLogLevelArrayAdapter);
        mChooseLogLevel.setSelection(selectedLevelIndex);
        mChooseLogLevel.setOnItemSelectedListener(new LogLevelSelectionListener());

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(Constants.Device.DEPLOYED_VERSION_FILE));
            setTitle("Y60 " + br.readLine());
            br.close();
        } catch (Exception e) {
            // Ok, no version available
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return true;
    }

    private void launchEntryPoint() {

        Intent intent = new Intent("y60.intent.SHOW_LAUNCHERS");
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class RenameClickListener implements OnClickListener {

        // @Override
        public void onClick(View v) {

            String configFile = getResources().getString(R.string.configFile);
            JSONObject configuration = null;
            try {

                FileReader fr = new FileReader(configFile);
                char[] inputBuffer = new char[255];
                fr.read(inputBuffer);
                configuration = new JSONObject(new String(inputBuffer));
                fr.close();

                FileWriter fw = new FileWriter(configFile);
                configuration.put("device-path", mDeviceIdEdit.getText().toString());
                fw.write(configuration.toString());
                fw.close();

                Toast.makeText(Y60.this,
                        "Device name changed to " + mDeviceIdEdit.getText().toString(),
                        Toast.LENGTH_SHORT).show();

            } catch (FileNotFoundException e) {
                Logger.e(LOG_TAG, "Could not find configuration file ", configFile);
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                Logger.e(LOG_TAG, "Configuration file ", configFile, " uses unsupported encoding");
                throw new RuntimeException(e);
            } catch (IOException e) {
                Logger.e(LOG_TAG, "Error while reading configuration file ", configFile);
                throw new RuntimeException(e);
            } catch (JSONException e) {
                Logger.e(LOG_TAG, "Error while parsing configuration file ", configFile);
                throw new RuntimeException(e);
            }

        }

    }

    class ActivitySelectionListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int pPos, long arg3) {

            ComponentInformation[] componentNames = IntentHelper.getPossibleComponents(
                    Intent.ACTION_MAIN, Y60.this, Intent.CATEGORY_HOME, Intent.CATEGORY_DEFAULT);

            IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
            filter.addCategory(Intent.CATEGORY_HOME);
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            ComponentName[] componentNameArray = new ComponentName[componentNames.length];

            PackageManager pm = getPackageManager();
            int bestScore = 0;
            int i = 0;

            for (ComponentInformation currentComponentInformation : componentNames) {
                if (currentComponentInformation.match > bestScore) {
                    bestScore = currentComponentInformation.match;
                }
                componentNameArray[i] = currentComponentInformation.componentName;
                i += 1;

                pm.clearPackagePreferredActivities(currentComponentInformation.componentName
                        .getPackageName());
            }

            ComponentInformation preferredComponent = mCompInfoArrayAdapter.getItem(pPos);
            Toast.makeText(Y60.this, "selected item is: " + preferredComponent, Toast.LENGTH_SHORT)
                    .show();

            pm.addPreferredActivity(filter, preferredComponent.match, componentNameArray,
                    preferredComponent.componentName);

        }

        public void onNothingSelected(AdapterView<?> arg0) {

            // don't care
        }
    }

    class LogLevelSelectionListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int pPos, long arg3) {

            DeviceConfiguration config = DeviceConfiguration.load();
            String logLevelStr = mLogLevelArrayAdapter.getItem(pPos).toString();
            Logger.Level logLevel = Logger.Level.fromString(logLevelStr);

            Logger.v(LOG_TAG, "saving log level as ", logLevelStr);

            Logger.setFilterLevel(logLevel);
            config.saveLogLevel(logLevel);

            Toast.makeText(Y60.this, "Log Level changed to " + logLevelStr, Toast.LENGTH_SHORT)
                    .show();

        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // don't care
        }
    }
}
