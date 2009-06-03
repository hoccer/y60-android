package com.artcom.y60;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;

import android.net.Uri;

import com.artcom.y60.gom.GomHttpWrapper;

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
            
            if (ip != null)
                return ip;
            
        } catch (SocketException e) {

            Logger.e(LOG_TAG, "couldn't determine local staging IP, now trying to get it from GOM", e);
        }
        
        try {
            DeviceConfiguration dc = DeviceConfiguration.load();
            
            String self       = dc.getDevicePath();
            String ipAttrPath = self + ":" + Constants.Network.IP_ADDRESS_ATTRIBUTE;
            Uri    ipAttrUrl  = Uri.parse(Constants.Gom.URI + ipAttrPath); 
            String ipStr      = GomHttpWrapper.getAttributeValue(ipAttrUrl);
            
            return InetAddress.getByName(ipStr);
            
        } catch(Exception ex) {
            
            throw new RuntimeException(ex);
        }
    }

}
