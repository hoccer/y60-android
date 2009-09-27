package com.artcom.y60;

import android.content.IntentFilter;

public class Constants {

    public static class Network {

        public static final int    DEFAULT_PORT         = 4042;

        /** target for GNP HTTP requests */
        public static final String GNP_TARGET           = "/notifications";

        public static final String IP_ADDRESS_ATTRIBUTE = "ip_address";

    }

    public static class Gom {

        public static class Keywords {
            // GOM keywords
            public static final String NODE         = "node";
            public static final String URI          = "uri";
            public static final String ATTRIBUTE    = "attribute";
            public static final String ENTRIES      = "entries";
            public static final String NAME         = "name";
            public static final String VALUE        = "value";
            public static final String TYPE         = "type";
            public static final String DISPLAY_NAME = "display_name";
            public static final String BROWSABLE    = "browsable";
            public static final String ITEM         = "item";
        }

        public static final String       URI;
        public static final String       DEVICE_PATH;
        public static final String       SCRIPT_BASE_PATH;
        public static final String       SCRIPT_RUNNER_URI  = "http://t-gom.service.t-gallery.act/gom/script-runner";

        public static final String       OBSERVER_BASE_PATH = "/gom/observer";

        public static final IntentFilter GNP_INTENT_FILTER;

        static {
            DeviceConfiguration config = DeviceConfiguration.load();
            URI = config.getGomUrl();
            GNP_INTENT_FILTER = new IntentFilter(Y60Action.GOM_NOTIFICATION_BC);
            DEVICE_PATH = config.getDevicePath();
            SCRIPT_BASE_PATH = DEVICE_PATH + "/scripts";
        }

    }

    public static class Device {

        public static final String JSON_CONFIG_FILE = "/sdcard/device_config.json";

    }
}
