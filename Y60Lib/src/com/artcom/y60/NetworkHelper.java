package com.artcom.y60;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

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

    /**
     * Fetches the ip address used to go "on stage". This is either the one we got from a dhcp
     * server or (on emulators) an address setted in the GOM.
     * 
     * @throws IpAddressNotFoundException
     */
    public static InetAddress getStagingIp() throws IpAddressNotFoundException {

        InetAddress ip = null;

        try {
            Collection<InetAddress> addrs = NetworkHelper.getLocalIpAddresses();
            for (InetAddress addr : addrs) {

                Logger.v(LOG_TAG, "ip address: <", addr, ">");
                if (!addr.toString().contains("127.0.0.1") && !addr.toString().contains("10.0.2")) {
                    ip = addr;
                }
            }

            if (ip != null)
                return ip;

        } catch (SocketException e) {

            Logger.e(LOG_TAG, "couldn't determine local staging IP, now trying to get it from GOM",
                    e);
        }

        try {
            DeviceConfiguration dc = DeviceConfiguration.load();

            String self = dc.getDevicePath();
            String ipAttrPath = self + ":" + Constants.Network.IP_ADDRESS_ATTRIBUTE;
            Uri ipAttrUrl = Uri.parse(Constants.Gom.URI + ipAttrPath);
            String ipStr = GomHttpWrapper.getAttributeValue(ipAttrUrl);

            return InetAddress.getByName(ipStr);

        } catch (Exception ex) {

            throw new IpAddressNotFoundException("could not retrieve a valid ip address", ex);
        }
    }

    /**
     * Fetches the first ip address of the device wich is not the loopback 127.0.0.1.
     */
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

}
