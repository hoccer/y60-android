package com.artcom.y60.hoccer;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.artcom.y60.Logger;

public class BestLocationManager implements LocationListener {

    private static final String   LOG_TAG               = "BestLocationManager";

    private static final String   UNKNOWN_LOCATION_TEXT = "You can not hoc without a location";

    private final LocationManager mLocationManager;

    private final Context         mContext;

    private HocLocation           mLastKnownLocation    = null;

    private List<ScanResult>      mScanResults;

    private final WifiManager     mWifiManager;

    public BestLocationManager(Context pContext) {
        mContext = pContext;

        mLocationManager = (LocationManager) pContext.getSystemService(Context.LOCATION_SERVICE);
        mWifiManager = (WifiManager) pContext.getSystemService(Context.WIFI_SERVICE);
    }

    public HocLocation getBestLocation() throws UnknownLocationException {
        mScanResults = mWifiManager.getScanResults();
        HocLocation networkLocation = HocLocation.createFromLocation(mLocationManager
                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER), mScanResults);
        HocLocation gpsLocation = HocLocation.createFromLocation(mLocationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER), mScanResults);

        if (gpsLocation == null && networkLocation == null) {

            if (mLastKnownLocation != null
                    && System.currentTimeMillis() - mLastKnownLocation.getTime() < 5000) {

                return mLastKnownLocation;
            } else {
                throw new UnknownLocationException();
            }
        }

        if (gpsLocation == null && networkLocation != null)
            mLastKnownLocation = networkLocation;
        else if (networkLocation == null && gpsLocation != null)
            mLastKnownLocation = gpsLocation;
        else if (networkLocation.getTime() > gpsLocation.getTime())
            mLastKnownLocation = networkLocation;
        else
            mLastKnownLocation = gpsLocation;

        if (mLastKnownLocation == null) {
            throw new UnknownLocationException();
        }

        mLastKnownLocation.setAddress(getDisplayableAddress());
        return mLastKnownLocation;
    }

    public void deactivate() {
        mLocationManager.removeUpdates(this);
    }

    public void activate() {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Logger.v(LOG_TAG, location);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public Address getAddress(Location pLocation) throws IOException {
        if (pLocation == null) {
            return new Address(null);
        }

        Geocoder gc = new Geocoder(mContext);
        // Logger.v(LOG_TAG, "location: ", pLocation);

        Address address = null;
        List<Address> addresses = gc.getFromLocation(pLocation.getLatitude(), pLocation
                .getLongitude(), 1);
        if (addresses.size() > 0) {
            address = addresses.get(0);
        }
        return address;
    }

    public String getDisplayableAddress() {
        if (mLastKnownLocation == null) {
            return UNKNOWN_LOCATION_TEXT;
        }
        Location location = mLastKnownLocation;

        try {
            Address address = getAddress(location);

            String addressLine = null;
            String info = " (~" + location.getAccuracy() + "m)";
            if (location.getAccuracy() < 500) {
                addressLine = address.getAddressLine(0);
            } else {
                addressLine = address.getAddressLine(1);
            }

            addressLine = trimAddress(addressLine);

            return addressLine + info;

        } catch (Exception e) {
            Logger.e(LOG_TAG, e);
            return UNKNOWN_LOCATION_TEXT + " ~" + location.getAccuracy() + "m";
        }
    }

    private String trimAddress(String pAddressLine) {
        if (pAddressLine.length() < 27)
            return pAddressLine;

        String newAddress = pAddressLine.substring(0, 18) + "..."
                + pAddressLine.substring(pAddressLine.length() - 5);

        return newAddress;
    }
}
