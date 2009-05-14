package com.artcom.y60;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class NetworkHelper {
    
    private static final String LOG_TAG = "NetworkHelper";

    public static Collection<InetAddress> getLocalIpAddresses() throws SocketException {
        
        Set<InetAddress> addresses = new HashSet<InetAddress>();
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
    
    public InetAddress getTGalleryInetAddress() {
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
