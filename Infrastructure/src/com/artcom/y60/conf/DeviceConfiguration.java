package com.artcom.y60.conf;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.logging.Logger;

public class DeviceConfiguration {

	private static final String LOG_TAG = "DeviceConfiguration";
	private static final String CONFIG_FILE = "/sdcard/device_config.json";
	
	public static DeviceConfiguration load(){
		return new DeviceConfiguration();
	}

	private String mGomUrl;
	private String mDevicePath;
	private Logger.Level mLogLevel;
	
	public String getGomUrl() {
		return mGomUrl;
	}

	public String getDevicePath() {
		return mDevicePath;
	}
	
	public Logger.Level getLogLevel() {
	    return mLogLevel;
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
			mLogLevel = Logger.Level.fromString(configuration.getString("log-level"));

		} catch (FileNotFoundException e) {
			Logger.e( LOG_TAG, "Could not find configuration file ", CONFIG_FILE );
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			Logger.e( LOG_TAG, "Configuration file ", CONFIG_FILE, " uses unsupported encoding" );
			throw new RuntimeException(e);
		} catch (IOException e) {
			Logger.e( LOG_TAG, "Error while reading configuration file ", CONFIG_FILE );
			throw new RuntimeException(e);
		} catch (JSONException e) {
			Logger.e( LOG_TAG, "Error while parsing configuration file ", CONFIG_FILE );
			throw new RuntimeException(e);
		}

	}
}
