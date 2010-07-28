package com.artcom.y60.hoccer;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.net.wifi.ScanResult;

import com.artcom.y60.data.ProblemDescriptor;

public class HocLocation {

    private static final String       LOG_TAG          = "HocLocation";

    private List<AccessPointSighting> mScanResults     = null;
    private String                    mAddress;
    private final Location            mLocation;

    public static final int           OPTIMAL_ACCURACY = 200;
    public static final int           WORST_ACCURACY   = 5000;

    private final long                mCreationTime    = System.currentTimeMillis();

    HocLocation(Location pLocation, List<ScanResult> scanResults) {
        mLocation = pLocation;
        setScanResult(scanResults);
    }

    public boolean hasLatLong() {
        return mLocation != null;
    }

    public HocLocation(HocLocation hocLocation) {
        mLocation = hocLocation.mLocation;
        mScanResults = hocLocation.mScanResults;
    }

    public HocLocation(Location location, ArrayList<AccessPointSighting> sightings) {
        mLocation = location;
        mScanResults = sightings;
    }

    public HocLocation(Location location) {
        mLocation = location;
        mScanResults = new ArrayList<AccessPointSighting>();
    }

    public List<AccessPointSighting> getScanResults() {
        return mScanResults;
    }

    public void setScanResult(List<ScanResult> pScanResults) {
        mScanResults = new ArrayList<AccessPointSighting>();
        if (pScanResults == null) {
            return;
        }
        for (ScanResult scan : pScanResults) {
            mScanResults.add(new AccessPointSighting(scan.BSSID, scan.level));
        }
    }

    public void setAccesPointSightings(List<AccessPointSighting> pScanResults) {
        mScanResults = pScanResults;
    }

    /**
     * @return quality of the hoc location (0=ultra bad, 1=may work, 2=seems ok, 3=super)
     */
    public int getQuality() {
        int quality = 0;

        if (mScanResults != null && mScanResults.size() > 0) {
            quality += 1;
        }

        if (!hasLatLong()) {
            return quality;
        }

        if (getAccuracy() < OPTIMAL_ACCURACY) {
            quality += 2;
        } else if (getAccuracy() < WORST_ACCURACY) {
            quality += 1;
        }

        return quality;
    }

    float getAccuracy() {
        return mLocation.getAccuracy();
    }

    public String getProblemDescription() {

        return null;
    }

    public String getResolvingHelp() {
        return null;
    }

    public HoccabilityProblem getHocLocationProblem(ProblemDescriptor descriptor) {

        HoccabilityProblem problem = new HoccabilityProblem();

        switch (getQuality()) {
            case 0:
                problem.setProblem(descriptor
                        .getDescription(ProblemDescriptor.Problems.HOCCABILITY_BAD));
                problem.setRecoverySuggestion(descriptor
                        .getDescription(ProblemDescriptor.Suggestions.HOCCABILITY_0));
                break;

            case 1:
                problem.setProblem(descriptor
                        .getDescription(ProblemDescriptor.Problems.HOCCABILITY_OK));

                if (mScanResults == null || mScanResults.size() == 0) {
                    problem
                            .setRecoverySuggestion(descriptor
                                    .getDescription(ProblemDescriptor.Suggestions.HOCCABILITY_1_GPS_OK_BSSIDS_BAD));
                } else {
                    problem
                            .setRecoverySuggestion(descriptor
                                    .getDescription(ProblemDescriptor.Suggestions.HOCCABILITY_1_GPS_BAD_BSSIDS_GOOD));
                }

                break;

            case 2:
                problem.setProblem(descriptor
                        .getDescription(ProblemDescriptor.Problems.HOCCABILITY_OK));

                if (mScanResults == null || mScanResults.size() == 0) {
                    problem
                            .setRecoverySuggestion(descriptor
                                    .getDescription(ProblemDescriptor.Suggestions.HOCCABILITY_2_GPS_GOOD_BSSIDS_BAD));
                } else {
                    problem
                            .setRecoverySuggestion(descriptor
                                    .getDescription(ProblemDescriptor.Suggestions.HOCCABILITY_2_GPS_OK_BSSIDS_GOOD));
                }

                break;

            case 3:
                problem.setProblem(descriptor
                        .getDescription(ProblemDescriptor.Problems.HOCCABILITY_GOOD));
                problem.setRecoverySuggestion(descriptor
                        .getDescription(ProblemDescriptor.Suggestions.HOCCABILITY_3));
                break;

            default:
                break;
        }

        return problem;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getAddress() {
        return mAddress;
    }

    public long getTime() {
        if (!hasLatLong()) {
            return mCreationTime;
        }

        return mLocation.getTime();
    }

    public double getLatitude() {
        return mLocation.getLatitude();
    }

    public double getLongitude() {
        return mLocation.getLongitude();
    }

    void setLatitude(double latitude) {
        mLocation.setLatitude(latitude);
    }

    void setLongitude(double longitude) {
        mLocation.setLongitude(longitude);
    }

    void setAccuracy(int accuracy) {
        mLocation.setAccuracy(accuracy);
    }
}
