package com.artcom.y60;

import android.content.IntentFilter;

public class Constants {

    public static class Network {
        
        public static final int DEFAULT_PORT = 4042;
        
        /** target for GNP HTTP requests */
        public static final String GNP_TARGET = "/notifications";
        
        public static final String IP_ADDRESS_ATTRIBUTE = "ip_address";
        
    }
    
    public static class Gom {
        
        public static class Keywords {
            
            // GOM keywords
            public static final String NODE      = "node",
                                       URI       = "uri",
                                       ATTRIBUTE = "attribute",
                                       ENTRIES   = "entries",
                                       NAME      = "name",
                                       VALUE     = "value",
                                       TYPE      = "type";
        }
        
        public static final String URI;
        
        public static final String OBSERVER_BASE_PATH = "/gom/observer";
        
        public static final IntentFilter NOTIFICATION_FILTER;
        
        static {
            
            DeviceConfiguration config = DeviceConfiguration.load();
            URI = config.getGomUrl();
            
            NOTIFICATION_FILTER = new IntentFilter(Y60Action.GOM_NOTIFICATION_BC);
        }
        
    }
}
