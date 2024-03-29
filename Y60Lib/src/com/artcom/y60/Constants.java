package com.artcom.y60;

import android.content.IntentFilter;

public class Constants {

    public static class Network {

        public static final int    DEFAULT_PORT         = 4042;

        /** target for GNP HTTP requests */
        public static final String GNP_TARGET           = "/notifications";

        public static final String IP_ADDRESS_ATTRIBUTE = "ip_address";
    }

    public static class Languages {
        public static final String GERMAN  = "deu";
        public static final String ENGLISH = "eng";
    }

    public static class Gom {

        public static class Keywords {
            // GOM keywords
            public static final String NODE         = "node";
            public static final String URI          = "uri";
            public static final String ATTRIBUTE    = "attribute";
            public static final String C_TIME       = "ctime";
            public static final String ENTRIES      = "entries";
            public static final String NAME         = "name";
            public static final String VALUE        = "value";
            public static final String TYPE         = "type";
            public static final String DISPLAY_NAME = "display_name";
        }

        public static final String       URI;
        public static final String       DEVICE_PATH;
        public static final String       SCRIPT_BASE_PATH;
        public static final String       DEBUG_MODE_ATTR;
        public static final String       ENABLE_ODP_ATTR;
        public static final String       ENABLE_ODP_AGC_ATTR;

        public static final String       OBSERVER_BASE_PATH = "/gom/observer";

        public static final IntentFilter GNP_INTENT_FILTER;

        static {
            DeviceConfiguration config = DeviceConfiguration.load();
            URI = config.getGomUrl();
            GNP_INTENT_FILTER = new IntentFilter(Y60Action.GOM_NOTIFICATION_BC);
            DEVICE_PATH = config.getDevicePath();
            SCRIPT_BASE_PATH = DEVICE_PATH + "/scripts";
            DEBUG_MODE_ATTR = DEVICE_PATH + ":debug_mode";

            ENABLE_ODP_ATTR = DEVICE_PATH + ":enable_odp";
            ENABLE_ODP_AGC_ATTR = DEVICE_PATH + ":enable_odp_agc";
        }

    }

    public static class Device {

        public static final String DEPLOYDROID_SERVICEPATH_FILE = "/sdcard/deploydroid_servicepath.txt";
        public static final String DEPLOYED_VERSION_FILE = "/sdcard/deployed_version.txt";
        public static final String JSON_CONFIG_FILE      = "/sdcard/device_config.json";
        public static final String ALIVE_SERVICES_PATH   = "/sdcard/alive_services";

    }
}
