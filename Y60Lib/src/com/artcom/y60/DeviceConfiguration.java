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
    public static final String  CONFIG_FILE_PATH = "/sdcard/device_config.json";

    private static final String GOM_URL_KEY      = "gom_url";
    private static final String DEVICE_PATH_KEY  = "device_path";

    // Static Methods ----------------------------------------------------

    public static DeviceConfiguration load() {
        return new DeviceConfiguration();
    }

    // Instance Variables ------------------------------------------------

    private String       mGomUrl     = "gom url couldnt be read - sdcard probably not mounted";
    private String       mDevicePath = "device path couldnt be read - sdcard probably not mounted";
    private Logger.Level mLogLevel;

    // Constructors ------------------------------------------------------

    private DeviceConfiguration() {
        JSONObject configuration = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(CONFIG_FILE_PATH);
        } catch (FileNotFoundException e) {
            Logger.e(LOG_TAG, "Could not find configuration file ", CONFIG_FILE_PATH,
                    "sdcard probably not mounted yet");
            // throw new RuntimeException(e);
        }

        if (fileReader != null) {

            try {
                char[] inputBuffer = new char[255];
                fileReader.read(inputBuffer);
                fileReader.close();
                configuration = new JSONObject(new String(inputBuffer));

                mGomUrl = configuration.getString(GOM_URL_KEY);
                mDevicePath = configuration.getString(DEVICE_PATH_KEY);
                mLogLevel = Logger.Level.VERBOSE;

            } catch (UnsupportedEncodingException e) {
                Logger.e(LOG_TAG, "Configuration file ", CONFIG_FILE_PATH,
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

    }

    public String getGomUrl() {
        return mGomUrl;
    }

    /**
     * @return true if the code is executed via vpn from the Artcom developer network.
     */
    public static boolean isRunningViaArtcomDevelopmentVpn() throws IpAddressNotFoundException {
        if (NetworkHelper.getDeviceIpAddress().startsWith("10.")) {
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

    public static boolean switchMenuAndBackButton() {

        Logger.v(LOG_TAG, "BUILD MODEL: ", android.os.Build.MODEL);

        if (android.os.Build.MODEL.equals("Nexus One")) {
            return false;
        } else if (android.os.Build.MODEL.equals("Nexus S")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isWvga() {
        if (android.os.Build.MODEL.equals("Nexus One")) {
            return true;
        } else if (android.os.Build.MODEL.equals("Nexus S")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean hasRootPermissions() {
        if (android.os.Build.MODEL.equals("Nexus One")) {
            return true;
        } else if (android.os.Build.MODEL.equals("Nexus S")) {
            return true;
        } else {
            return false;
        }
    }

    public void saveLogLevel(Logger.Level pLevel) {
        mLogLevel = pLevel;
        save();
    }

    private void save() {
        try {
            JSONObject configJson = new JSONObject();
            configJson.put(GOM_URL_KEY, mGomUrl);
            configJson.put(DEVICE_PATH_KEY, mDevicePath);
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
