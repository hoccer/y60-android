package com.artcom.y60;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceConfiguration {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG          = "DeviceConfiguration";
    private static final String CONFIG_FILE_PATH = "/sdcard/device_config.json";

    private static final String GOM_URL_KEY      = "gom-url";
    private static final String DEVICE_PATH_KEY  = "device-path";
    private static final String LOG_LEVEL_KEY    = "log-level";

    // Static Methods ----------------------------------------------------

    public static DeviceConfiguration load() {
        return new DeviceConfiguration();
    }

    // Instance Variables ------------------------------------------------

    private String       mGomUrl;
    private String       mDevicePath;
    private Logger.Level mLogLevel;

    // Constructors ------------------------------------------------------

    private DeviceConfiguration() {
        JSONObject configuration = null;
        try {

            FileReader fr = new FileReader(CONFIG_FILE_PATH);
            char[] inputBuffer = new char[255];
            fr.read(inputBuffer);
            configuration = new JSONObject(new String(inputBuffer));
            mGomUrl = configuration.getString(GOM_URL_KEY);
            mDevicePath = configuration.getString(DEVICE_PATH_KEY);
            mLogLevel = Logger.Level.fromString(configuration.getString(LOG_LEVEL_KEY));
            fr.close();

        } catch (FileNotFoundException e) {
            Logger.e(LOG_TAG, "Could not find configuration file ", CONFIG_FILE_PATH);
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            Logger
                    .e(LOG_TAG, "Configuration file ", CONFIG_FILE_PATH,
                            " uses unsupported encoding");
            throw new RuntimeException(e);
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error while reading configuration file ", CONFIG_FILE_PATH);
            throw new RuntimeException(e);
        } catch (JSONException e) {
            Logger.e(LOG_TAG, "Error while parsing configuration file ", CONFIG_FILE_PATH);
            throw new RuntimeException(e);
        }
    }

    // Public Instance Methods -------------------------------------------

    public String getGomUrl() {
        return mGomUrl;
    }

    /**
     * @return true iff the code is executed on an emulator
     */
    public static boolean isRunningAsEmulator() throws IpAddressNotFoundException {
        if (NetworkHelper.getDeviceIpAddress().startsWith("10.0.2.")) {
            return true;
        }
        return false;
    }

    public String getDevicePath() {
        return mDevicePath;
    }

    public String getDeviceId() {
        return mDevicePath.substring(mDevicePath.lastIndexOf("/") + 1);
    }

    public Logger.Level getLogLevel() {

        return mLogLevel;
    }

    public void saveLogLevel(Logger.Level pLevel) {

        mLogLevel = pLevel;
        save();
    }

    // Private Instance Methods ------------------------------------------

    private void save() {

        try {
            JSONObject configJson = new JSONObject();
            configJson.put(GOM_URL_KEY, mGomUrl);
            configJson.put(DEVICE_PATH_KEY, mDevicePath);
            configJson.put(LOG_LEVEL_KEY, mLogLevel.toString());
            FileWriter writer = new FileWriter(CONFIG_FILE_PATH);
            PrintWriter printer = new PrintWriter(writer);
            printer.println(configJson.toString());
            printer.flush();
            printer.close();

        } catch (Exception ex) {

            throw new RuntimeException(ex);
        }
    }
}
