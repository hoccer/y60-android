/*
 *  Copyright (C) 1993-2008, ART+COM AG Berlin, Germany <www.artcom.de>
 * 
 *  These coded instructions, statements, and computer programs contain
 *  proprietary information of ART+COM AG Berlin, and are copy protected
 *  by law. They may be used, modified and redistributed under the terms
 *  of GNU General Public License referenced below. 
 *     
 *  Alternative licensing without the obligations of the GPL is
 *  available upon request.
 * 
 *  GPL v3 Licensing:
 * 
 *  This file is part of the ART+COM Y60 Platform.
 * 
 *  ART+COM Y60 is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ART+COM Y60 is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ART+COM Y60.  If not, see <http: * www.gnu.org/licenses/>.
 */

package com.artcom.y60;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Y60 extends Activity {

    private static final String                LOG_TAG = "Y60";

    private EditText                           mDeviceIdEdit;
    private Button                             mSetDeviceIdButton;
    private Button                             mInitButton;
    private Button                             mStopDcButton;
    private Button                             mWifiCfgButton;

    private Spinner                            mChooseHomeButtonTarget;
    private ArrayAdapter<ComponentInformation> mCompInfoArrayAdapter;

    private Spinner                            mChooseLogLevel;
    private ArrayAdapter<String>               mLogLevelArrayAdapter;

    /** Called when the activity is first created. */
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
                // hide status bar
                Window win = getWindow();
                win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

                Intent intent = new Intent(Y60Action.INIT_Y60_BC);
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
        ComponentInformation[] components = getPossibleComponents(Intent.ACTION_MAIN,
                Intent.CATEGORY_HOME, Intent.CATEGORY_DEFAULT);
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

    private ComponentInformation[] getPossibleComponents(String pAction, String... pCategories) {

        // create Intent from params
        Intent homeIntent = new Intent(pAction);
        for (String cat : pCategories) {

            homeIntent.addCategory(cat);
        }

        HashMap<String, ComponentInformation> activityInfos = queryIntentActivitiesAsMap(homeIntent);
        ArrayList<ComponentInformation> resultList = new ArrayList<ComponentInformation>(
                activityInfos.size());
        try {
            ComponentInformation chosen = getPreferredActivity(pAction, activityInfos);
            activityInfos.remove(chosen.componentName.getClassName());
            resultList.add(chosen); // default activity should be the 1st
            // element
        } catch (NoSuchElementException e) {
            Logger.e(LOG_TAG, "No preferred activity found");
        }

        resultList.addAll(activityInfos.values());

        // possible activities to array
        return resultList.toArray(new ComponentInformation[activityInfos.size()]);
    }

    private HashMap<String, ComponentInformation> queryIntentActivitiesAsMap(Intent pIntent) {

        // ResolveInfo: Information that is returned from resolving an intent
        // against an IntentFilter
        List<ResolveInfo> homeResolveInfos = getPackageManager().queryIntentActivities(pIntent, 0);

        // Possible Activities
        HashMap<String, ComponentInformation> activityInfos = new HashMap<String, ComponentInformation>(
                homeResolveInfos.size());
        // Fills ComponentNames (=activites) for homeResolveInfos and determines
        // bestScore
        for (ResolveInfo currentResolveInfo : homeResolveInfos) {

            ActivityInfo activityInfo = currentResolveInfo.activityInfo;
            // create ComponentName from current activity.
            ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName,
                    activityInfo.name);

            activityInfos.put(name.getClassName(), new ComponentInformation(name,
                    currentResolveInfo.match));
            Logger.v(LOG_TAG, "rival activity: ", name + "for (intent-)action: ", pIntent
                    .getAction());
        }

        return activityInfos;
    }

    private ComponentInformation getPreferredActivity(String pAction,
            HashMap<String, ComponentInformation> pComponents) {

        ArrayList<IntentFilter> filters = new ArrayList<IntentFilter>();
        ArrayList<ComponentName> activityNames = new ArrayList<ComponentName>();
        getPackageManager().getPreferredActivities(filters, activityNames, null);
        Logger.v(LOG_TAG, "found ", filters.size(), "preferred activities");

        ComponentInformation chosen = null;

        for (int i = 0; i < filters.size(); i++) {

            IntentFilter filter = filters.get(i);
            if (filter.getAction(0).equals(pAction)) {

                String className = activityNames.get(i).getClassName();
                chosen = pComponents.get(className);
                break;
            }
        }

        if (chosen == null) {
            throw new NoSuchElementException("No preferred activity found for action '" + pAction
                    + "'!");
        }

        return chosen;
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

    class ComponentInformation {

        public ComponentName componentName;
        public int           match;

        public ComponentInformation(ComponentName pComponentName, int pMatch) {
            componentName = pComponentName;
            match = pMatch;
        }

        @Override
        public String toString() {
            return componentName.getClassName();
        }

    }

    class ActivitySelectionListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int pPos, long arg3) {

            ComponentInformation[] componentNames = getPossibleComponents(Intent.ACTION_MAIN,
                    Intent.CATEGORY_HOME, Intent.CATEGORY_DEFAULT);

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
