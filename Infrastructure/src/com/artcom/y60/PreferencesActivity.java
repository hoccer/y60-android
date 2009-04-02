package com.artcom.y60;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {
		
	// TODO there should be a way to reference strings we've defined in
	// res/values/strings.xml via R.java here. Figure out how. Else we
	// have to keep this string here in sync with the one that we defined
	// in the AndroidManifest.xml
	public static final String KEY_GOM_LOCATION = "pref_gom_location";
	public static final String KEY_DEVICE_ID = "pref_device_id";
	public static final String KEY_DEVICES_PATH = "pref_device_path";

	@Override
	protected void onCreate( Bundle icicle )
	{
		super.onCreate(icicle);

	}
}
