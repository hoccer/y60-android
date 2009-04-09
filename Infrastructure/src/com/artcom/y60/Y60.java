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
import java.util.List;

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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Y60 extends Activity {

    private static final String LOG_TAG = "Y60 Stargate 108";

    private EditText mEditText;
    private Button mSetNameButton;
    private Button mStartY60Button;
    private Button mStopY60Button;
    private TextView mHomeTargetTextView;
    private Spinner mChooseHomeButtonTarget;
    private ArrayAdapter<ComponentInformation> mArrayAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.y60_layout);

        mEditText = (EditText) findViewById(R.id.mEditText);
        // get device id and display in EditText
        JSONObject configuration = null;
        String configFile = getResources().getString(R.string.configFile);
        String labelDeviceId = "device ID not found";
        try {
            FileReader fr = new FileReader(configFile);
            char[] inputBuffer = new char[255];
            fr.read(inputBuffer);
            configuration = new JSONObject(new String(inputBuffer));
            fr.close();

            labelDeviceId = configuration.getString("device-path");
            labelDeviceId = labelDeviceId.replaceFirst("devices/mobile/", "");

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
        mEditText.setText(labelDeviceId);

        mSetNameButton = (Button) findViewById(R.id.mSetNameButton);
        mSetNameButton.setOnClickListener(new RenameClickListener());

        mStartY60Button = (Button) findViewById(R.id.mStartY60Button);
        mStartY60Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide status bar
                Window win = getWindow();
                win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                launchEntryPoint();
            }
        });

        mStopY60Button = (Button) findViewById(R.id.mStopY60Button);
        mStopY60Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mHomeTargetTextView = (TextView) findViewById(R.id.mHomeTargetTextView);

        mChooseHomeButtonTarget = (Spinner) findViewById(R.id.mChooseHomeButtonTarget);

        ComponentInformation[] componentNames = getPossibleComponents(Intent.ACTION_MAIN,
                Intent.CATEGORY_HOME, Intent.CATEGORY_DEFAULT);

        mArrayAdapter = new ArrayAdapter<ComponentInformation>(this,
                android.R.layout.simple_spinner_dropdown_item, componentNames);
        mChooseHomeButtonTarget.setAdapter(mArrayAdapter);

        mChooseHomeButtonTarget.setOnItemSelectedListener(new ActivitySelectionListener());

        // choose t as default home activity - suppress chooser dialog
        // registerAsPreferredActivity(Intent.ACTION_MAIN, Intent.CATEGORY_HOME,
        // Intent.CATEGORY_DEFAULT);

    }

    public void onResume() {
        super.onResume();
    }

    public boolean onTouchEvent(MotionEvent event) {

        return true;
    }

    private void launchEntryPoint() {

        Intent intent = new Intent("y60.intent.SHOW_LAUNCHERS");
        startActivity(intent);

    }

    private void registerAsPreferredActivity(String pAction, String... pCategories) {

        Logger.v(LOG_TAG, "register as preferred activity for action ", pAction);

        // TODO: link to HomeScreen
        // define our preferred activity
        ComponentName preferredActivity = new ComponentName(this, Y60.class);

        IntentFilter filter = new IntentFilter(pAction);
        for (String cat : pCategories) {

            filter.addCategory(cat);
            // Logger.v(LOG_TAG, "including category: ", cat);
        }

        Logger.v(LOG_TAG, "registering as preferred activity:", filter);

        // pm.addPreferredActivity(filter, bestScore,
        // getPossibleComponents(pAction, pCategories), preferredActivity);

    }

    private ComponentInformation[] getPossibleComponents(String pAction, String... pCategories) {

        PackageManager pm = getPackageManager();

        // create Intent from params
        Intent homeIntent = new Intent(pAction);
        for (String cat : pCategories) {

            homeIntent.addCategory(cat);
        }

        // ResolveInfo: Information that is returned from resolving an intent
        // against an IntentFilter
        List<ResolveInfo> homeResolveInfos = pm.queryIntentActivities(homeIntent, 0);

        // Possible Activities
        List<ComponentInformation> activityNames = new ArrayList<ComponentInformation>(
                homeResolveInfos.size());
        int bestScore = 0;

        // Fills ComponentNames (=activites) for homeResolveInfos and determines
        // bestScore
        for (ResolveInfo currentResolveInfo : homeResolveInfos) {

            if (currentResolveInfo.match > bestScore) {
                bestScore = currentResolveInfo.match;
            }

            ActivityInfo activityInfo = currentResolveInfo.activityInfo;
            // create ComponentName from current activity.
            ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName,
                    activityInfo.name);

            activityNames.add(new ComponentInformation(name, currentResolveInfo.match));
            Logger.v(LOG_TAG, "rival activity: ", name + "for (intent-)action: ", pAction);
        }

        // possible activities to array
        return activityNames.toArray(new ComponentInformation[activityNames.size()]);

    }

    class RenameClickListener implements OnClickListener {

        @Override
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
                configuration
                        .put("device-path", "devices/mobile/" + mEditText.getText().toString());
                fw.write(configuration.toString());
                fw.close();

                Toast.makeText(Y60.this,
                        "Device name changed to " + mEditText.getText().toString(),
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
        public int match;

        public ComponentInformation(ComponentName pComponentName, int pMatch) {
            componentName = pComponentName;
            match = pMatch;
        }

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
            
            ComponentName[] componentNameArray= new ComponentName[componentNames.length];
            
            PackageManager pm= getPackageManager();
            int bestScore= 0;
            int i=0;

            for (ComponentInformation currentComponentInformation : componentNames) {
                if (currentComponentInformation.match > bestScore) {
                    bestScore = currentComponentInformation.match;
                }
                componentNameArray[i++]= currentComponentInformation.componentName;

                pm.clearPackagePreferredActivities( currentComponentInformation.componentName.getPackageName());
            }
                        
            ComponentInformation preferredComponent= mArrayAdapter.getItem(pPos);
            Toast.makeText(Y60.this, "selected item is: " + preferredComponent, Toast.LENGTH_SHORT).show();
                                  
            pm.addPreferredActivity(filter, preferredComponent.match, componentNameArray, preferredComponent.componentName);

            
                      
        }

        public void onNothingSelected(AdapterView<?> arg0) {

            // don't care
        }
    }

}