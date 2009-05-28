package com.artcom.y60;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;

public class NetworkHelper {
    
    private static final String LOG_TAG = "NetworkHelper";

    public static HashSet<InetAddress> getLocalIpAddresses() throws SocketException {
        
        HashSet<InetAddress> addresses = new HashSet<InetAddress>();
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        while (nis.hasMoreElements()) {
            
            NetworkInterface ni = nis.nextElement();
            Enumeration<InetAddress> iis = ni.getInetAddresses();
            while (iis.hasMoreElements()) {
                
                addresses.add(iis.nextElement());
            }
        }
        
        return addresses;
    }
    
    public static InetAddress getStagingIp() {
        InetAddress ip = null;
        try {
            Collection<InetAddress> addrs = NetworkHelper.getLocalIpAddresses();
            for (InetAddress addr : addrs) {

                Logger.v(LOG_TAG, "ip address: <", addr, ">");
                if (addr.toString().indexOf("192.168.9.") > -1) {

                    ip = addr;
                }
            }
        } catch (SocketException e) {

            //ErrorHandling.signalNetworkError(LOG_TAG, e);
        }
        return ip;
    }

}
