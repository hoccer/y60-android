package com.artcom.y60;

public class Constants {

    public static class Network {
        
        public static final int DEFAULT_PORT = 4042;
        
        /** target for GNP HTTP requests */
        public static final String GNP_TARGET = "/notifications";
        
    }
    
    public static class Gom {
        
        public static final String URI;
        
        public static final String OBSERVER_BASE_PATH = "/gom/observer";
        
        static {
            
            DeviceConfiguration config = DeviceConfiguration.load();
            URI = config.getGomUrl();
        }
        
    }
}
