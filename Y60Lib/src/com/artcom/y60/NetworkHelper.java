package com.artcom.y60;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import com.artcom.y60.gom.GomHttpWrapper;

import android.telephony.TelephonyManager;

public class NetworkHelper {

    private static final String LOG_TAG = "NetworkHelper";

    public static String getNetworkType(TelephonyManager telephonyManager) {

        int networkTypeId = telephonyManager.getNetworkType();
        String networkType = "";

        switch (networkTypeId) {

            case TelephonyManager.NETWORK_TYPE_EDGE:
                networkType = "EDGE";
                break;

            case TelephonyManager.NETWORK_TYPE_GPRS:
                networkType = "GPRS";
                break;

            case TelephonyManager.NETWORK_TYPE_1xRTT:
                networkType = "1xRTT";
                break;

            case TelephonyManager.NETWORK_TYPE_CDMA:
                networkType = "CDMA";
                break;

            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                networkType = "EVDO_0";
                break;

            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                networkType = "EVDO_A";
                break;

            case TelephonyManager.NETWORK_TYPE_HSDPA:
                networkType = "HSDPA";
                break;

            case TelephonyManager.NETWORK_TYPE_HSPA:
                networkType = "HSPA";
                break;

            case TelephonyManager.NETWORK_TYPE_HSUPA:
                networkType = "HSPUA";
                break;

            case TelephonyManager.NETWORK_TYPE_UMTS:
                networkType = "UMTS";
                break;

            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                networkType = "UNKNOWN";
                break;

            default:
                networkType = "UNKNOWN";
                break;
        }

        return networkType;

    }

    public static HashSet<InetAddress> getLocalIpAddresses() throws SocketException {
        Logger.v(LOG_TAG, "1");
        HashSet<InetAddress> addresses = new HashSet<InetAddress>();
        Logger.v(LOG_TAG, "2");
        Enumeration<NetworkInterface> nis;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Logger.v(LOG_TAG, "SOCKET EXCEPTION");
        }
        nis = NetworkInterface.getNetworkInterfaces();
        Logger.v(LOG_TAG, "3");
        while (nis.hasMoreElements()) {
            Logger.v(LOG_TAG, "4");

            NetworkInterface ni = nis.nextElement();
            Enumeration<InetAddress> iis = ni.getInetAddresses();
            while (iis.hasMoreElements()) {
                InetAddress inetAddress = iis.nextElement();
                Logger.v(LOG_TAG, inetAddress.getHostAddress()
                        + "--------------------------<<------ host add ------------------");
                addresses.add(inetAddress);
            }
        }

        Logger.v(LOG_TAG, addresses.size()
                + "--------------------------<<------ size of add ------------------");
        return addresses;
    }

    /**
     * Fetches the ip address used to go "on stage". This is either the one we got from a dhcp
     * server or (on emulators) an address set in the GOM.
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

            if (ip != null) {
                return ip;
            }

        } catch (SocketException e) {

            Logger.e(LOG_TAG, "couldn't determine local staging IP, now trying to get it from GOM",
                    e);
        }

        try {
            DeviceConfiguration dc = DeviceConfiguration.load();

            String self = dc.getDevicePath();
            String ipAttrPath = self + ":" + Constants.Network.IP_ADDRESS_ATTRIBUTE;
            String ipAttrUrl = Constants.Gom.URI + ipAttrPath;
            String ipStr = GomHttpWrapper.getAttributeValue(ipAttrUrl);
            Logger.v(LOG_TAG, "ip str '", ipStr, "'");

            return InetAddress.getByName(ipStr);
        } catch (Exception ex) {
            throw new IpAddressNotFoundException("could not retrieve a valid ip address", ex);
        }
    }

    /**
     * Fetches the first ip address of the device wich is not the loopback 127.0.0.1.
     */
    public static String getDeviceIpAddress() throws IpAddressNotFoundException {

        Logger.v(LOG_TAG, "getDeviceIpAddress "
                + "--------------------------<<------------------------");

        HashSet<InetAddress> addresses = null;
        try {
            addresses = NetworkHelper.getLocalIpAddresses();
        } catch (SocketException e1) {
            throw new IpAddressNotFoundException("could not retrieve a valid ip address");
        }

        Iterator<InetAddress> itr = addresses.iterator();
        while (itr.hasNext()) {
            InetAddress addr = itr.next();
            Logger.v(LOG_TAG, addr + "--------------------------<<------------------------");
            String addrString = addr.getHostAddress();
            if (!addrString.equals("127.0.0.1")) {
                return addrString;
            }
        }

        throw new IpAddressNotFoundException("could not retrieve a valid ip address");
    }
}
