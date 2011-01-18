package com.artcom.y60;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.Suppress;
import android.widget.EditText;
import android.widget.Spinner;

public class Y60Test extends ActivityInstrumentationTestCase2<Y60> {

    public Y60Test() {

        super("com.artcom.y60", Y60.class);
    }

    @Suppress
    public void testCorrectLauncherIsSelected() {

        ArrayList<ComponentName> names = new ArrayList<ComponentName>();
        ArrayList<IntentFilter> filters = new ArrayList<IntentFilter>();
        getInstrumentation().getContext().getPackageManager().getPreferredActivities(filters,
                names, null);
        ComponentName preferredActivityName = null;
        for (int i = 0; i < filters.size(); i++) {

            IntentFilter filter = filters.get(i);
            if (filter.hasCategory(Intent.CATEGORY_HOME)) {

                preferredActivityName = names.get(i);
                break;
            }
        }

        assertNotNull("test premise not satisfied: you need a preferred home "
                + "activity on your device in order to run this test", preferredActivityName);

        Spinner launcherChooser = (Spinner) getActivity().findViewById(R.id.home_target_chooser);
        ComponentInformation selected = (ComponentInformation) launcherChooser.getSelectedItem();

        assertEquals("the preferred activity should be selected in the view",
                preferredActivityName, selected.componentName);
    }

    public void testCorrectLogLevelIsSelected() {

        Y60 y60 = getActivity();
        Spinner logLevelChooser = (Spinner) y60.findViewById(R.id.log_level_chooser);
        String selectedName = (String) logLevelChooser.getSelectedItem();
        String configuredName = DeviceConfiguration.load().getLogLevel().name();
        String actualName = Logger.getFilterLevel().name();
        assertEquals("should have set the configured log level on the Logger", configuredName,
                actualName);
        assertEquals("should have the configured filter level selected in the view",
                configuredName, selectedName);
    }

    public void testCorrectDevicePathIsDisplayed() {

        Y60 y60 = getActivity();
        EditText devicePathView = (EditText) y60.findViewById(R.id.device_path_edit);
        String devicePath = DeviceConfiguration.load().getDevicePath();
        assertEquals("should display device path from config file", devicePath, devicePathView
                .getText().toString());
    }
}
