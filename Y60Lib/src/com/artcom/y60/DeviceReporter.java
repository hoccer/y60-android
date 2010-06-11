package com.artcom.y60;

import java.util.HashMap;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

public class DeviceReporter extends BroadcastReceiver {

    private static final String LOG_TAG                      = "DeviceReporter";

    public static final String  BOARD_1                      = "board";
    public static final String  BRAND_1                      = "brand";
    public static final String  DEVICE_1                     = "device";
    public static final String  ID_1                         = "id";
    public static final String  MODEL_1                      = "model";
    public static final String  PRODUCT_1                    = "product";
    public static final String  TAGS_1                       = "tags";
    public static final String  TIME_1                       = "time";
    public static final String  TYPE_1                       = "type";
    public static final String  USER_1                       = "user";
    public static final String  HOST_1                       = "host";
    public static final String  VERSION_INCREMENTAL_1        = "incremental";
    public static final String  VERSION_RELEASE_1            = "release";
    public static final String  VERSION_SDK_1                = "sdk";
    public static final String  FINGERPRINT_1                = "fingerprint";
    public static final String  TELEPHONY_NETWORK_TYPE_1     = "network_type";
    public static final String  LOCAL_IP_1                   = "local_ip";
    public static final String  TELEPHONY_NETWORK_OPERATOR_1 = "network_operator";

    public static final String  DISPLAY_3                    = "display";

    public static final String  CPU_ABI_4                    = "cpu abi";
    public static final String  MANUFACTURER_4               = "manufacturer";
    public static final String  VERSION_CODENAME_4           = "codename";
    public static final String  VERSION_SDK_INT_4            = "sdk_int";

    public static final String  CPU_ABI_8                    = "cpu abi 2";
    public static final String  HARDWARE_8                   = "hardware";
    public static final String  RADIO_8                      = "radio";
    public static final String  BSSIDS                       = "bssids";

    @Override
    public void onReceive(Context context, Intent arg1) {

        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        DeviceReporterCollector deviceReporterCollector = null;

        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.CUPCAKE) {
            deviceReporterCollector = new DeviceReporterCollector(telephonyManager);

        } else if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.DONUT) {
            deviceReporterCollector = new DeviceReporterCollector15(telephonyManager);

        } else {
            deviceReporterCollector = new DeviceReporterCollector16(telephonyManager);
        }

        HashMap<String, String> deviceInfos = deviceReporterCollector.collectInformation();
        Set<String> keys = deviceInfos.keySet();
        for (String key : keys) {
            Logger.v(LOG_TAG, key, deviceInfos.get(key));
        }
    }

    public class DeviceReporterCollector {

        private TelephonyManager mTelephonyManager;

        public DeviceReporterCollector(TelephonyManager telephonyManager) {
            mTelephonyManager = telephonyManager;
        }

        public HashMap<String, String> collectInformation() {
            HashMap<String, String> parameters = new HashMap<String, String>();

            parameters.put(BRAND_1 + ": ", Build.BRAND);
            parameters.put(DEVICE_1 + ": ", Build.DEVICE);
            parameters.put(MODEL_1 + ": ", Build.MODEL);
            parameters.put(BOARD_1 + ": ", Build.BOARD);
            parameters.put(ID_1 + ": ", Build.ID);
            parameters.put(PRODUCT_1 + ": ", Build.PRODUCT);
            parameters.put(TAGS_1 + ": ", Build.TAGS);
            parameters.put(TIME_1 + ": ", String.valueOf(Build.TIME));
            parameters.put(TYPE_1 + ": ", Build.TYPE);
            parameters.put(USER_1 + ": ", Build.USER);
            parameters.put(VERSION_INCREMENTAL_1 + ": ", Build.VERSION.INCREMENTAL);
            parameters.put(VERSION_RELEASE_1 + ": ", Build.VERSION.RELEASE);
            parameters.put(VERSION_SDK_1 + ": ", Build.VERSION.SDK);
            parameters.put(FINGERPRINT_1 + ": ", Build.FINGERPRINT);
            try {
                parameters.put(LOCAL_IP_1 + ": ", NetworkHelper.getDeviceIpAddress());
            } catch (IpAddressNotFoundException e) {
                Logger.e(LOG_TAG, e.toString());
            }
            parameters.put(TELEPHONY_NETWORK_TYPE_1 + ": ", NetworkHelper
                    .getNetworkType(mTelephonyManager));
            parameters.put(TELEPHONY_NETWORK_OPERATOR_1 + ": ", mTelephonyManager
                    .getNetworkOperatorName());

            return parameters;
        }
    }

    public class DeviceReporterCollector15 extends DeviceReporterCollector {
        public DeviceReporterCollector15(TelephonyManager telephonyManager) {
            super(telephonyManager);
        }

        public HashMap<String, String> collectInformation() {
            HashMap<String, String> parameters = super.collectInformation();
            parameters.put(DISPLAY_3 + ": ", Build.DISPLAY);
            return parameters;
        }
    }

    public class DeviceReporterCollector16 extends DeviceReporterCollector15 {
        public DeviceReporterCollector16(TelephonyManager telephonyManager) {
            super(telephonyManager);
        }

        public HashMap<String, String> collectInformation() {
            HashMap<String, String> parameters = super.collectInformation();
            parameters.put(CPU_ABI_4 + ": ", Build.CPU_ABI);
            parameters.put(MANUFACTURER_4 + ": ", Build.MANUFACTURER);
            parameters.put(VERSION_CODENAME_4 + ": ", Build.VERSION.CODENAME);
            parameters.put(VERSION_SDK_INT_4 + ": ", String.valueOf(Build.VERSION.SDK_INT));
            return parameters;
        }
    }
}
