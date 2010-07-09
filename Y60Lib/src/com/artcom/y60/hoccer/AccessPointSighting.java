package com.artcom.y60.hoccer;

public class AccessPointSighting {

    public String bssid;
    public int    signalStrength;

    public AccessPointSighting(String bssid, int signalStrentgh) {
        this.bssid = bssid;
        this.signalStrength = signalStrentgh;
    }
}
