package com.artcom.y60.conf;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

public class DeviceConfiguration {

	private static final String LOG_TAG = "DeviceConfiguration";
	private static final String CONFIG_FILE = "/sdcard/device_config.json";
	
	public static DeviceConfiguration load(){
		return new DeviceConfiguration();
	}

	private String mGomUrl;
	private String mDevicePath;
	
	public String getGomUrl() {
		return mGomUrl;
	}

	public String getDevicePath() {
		return mDevicePath;
	}

	private DeviceConfiguration(){
		JSONObject configuration = null;
		try {
			
			FileReader fr = new FileReader( CONFIG_FILE );
			char[] inputBuffer = new char[255];
			fr.read(inputBuffer);
			configuration = new JSONObject(new String(inputBuffer));
			mGomUrl = configuration.getString("gom-url");
			mDevicePath = configuration.getString("device-path");

		} catch (FileNotFoundException e) {
			Log.e( LOG_TAG, "Could not find configuration file " + CONFIG_FILE );
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			Log.e( LOG_TAG, "Configuration file " + CONFIG_FILE + " uses unsupported encoding" );
			throw new RuntimeException(e);
		} catch (IOException e) {
			Log.e( LOG_TAG, "Error while reading configuration file " + CONFIG_FILE );
			throw new RuntimeException(e);
		} catch (JSONException e) {
			Log.e( LOG_TAG, "Error while parsing configuration file " + CONFIG_FILE );
			throw new RuntimeException(e);
		}

	}
}
