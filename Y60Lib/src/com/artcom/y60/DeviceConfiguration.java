package com.artcom.y60;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceConfiguration {

    public static class IpAddressNotFoundException extends Exception {

        public IpAddressNotFoundException(String pMessage) {
            super(pMessage);
        }

        private static final long serialVersionUID = 1L;

    }

    private static final String LOG_TAG = "DeviceConfiguration";
    private static final String CONFIG_FILE_PATH = "/sdcard/device_config.json";

    public static DeviceConfiguration load() {
        return new DeviceConfiguration();
    }

    private String mGomUrl;
    private String mDevicePath;
    private Logger.Level mLogLevel;

    public String getGomUrl() {
        return mGomUrl;
    }

    public static String getDeviceIpAddress() throws IpAddressNotFoundException {

        HashSet<InetAddress> addresses = null;
        try {
            addresses = NetworkHelper.getLocalIpAddresses();
        } catch (SocketException e1) {
            throw new IpAddressNotFoundException("could not retive a valid ip address");
        }

        Iterator<InetAddress> itr = addresses.iterator();
        while (itr.hasNext()) {
            InetAddress addr = itr.next();
            Logger.d(LOG_TAG, "address: ", addr.getHostAddress());
            String addrString = addr.getHostAddress();
            if (!addrString.equals("127.0.0.1")) {
                return addrString;
            }
        }

        throw new IpAddressNotFoundException("could not retive a valid ip address");
    }

    public static boolean isRunningAsEmulator() throws IpAddressNotFoundException {
        if (getDeviceIpAddress().startsWith("10.0.2.")) {
            return true;
        }
        return false;
    }

    public String getDevicePath() {
        return mDevicePath;
    }

    public Logger.Level getLogLevel() {
        return mLogLevel;
    }

    private DeviceConfiguration() {
        JSONObject configuration = null;
        try {

            FileReader fr = new FileReader(CONFIG_FILE_PATH);
            char[] inputBuffer = new char[255];
            fr.read(inputBuffer);
            configuration = new JSONObject(new String(inputBuffer));
            mGomUrl = configuration.getString("gom-url");
            mDevicePath = configuration.getString("device-path");
            mLogLevel = Logger.Level.fromString(configuration.getString("log-level"));
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
}
