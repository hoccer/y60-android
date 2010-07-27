package com.artcom.y60.hoccer;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import com.artcom.y60.data.ProblemDescriptor;

public class HocLocation extends Location {

    private static final String       LOG_TAG      = "HocLocation";

    private List<AccessPointSighting> mScanResults = null;
    private String                    mAddress;

    private HocLocation(Location pLocation) {
        super(pLocation);
    }

    /**
     * copy constructor
     */
    public HocLocation(HocLocation hocLocation) {
        super(hocLocation);
        mScanResults = hocLocation.mScanResults;
    }

    private HocLocation(Parcel pIn) {
        super(Location.CREATOR.createFromParcel(pIn));
        mScanResults = new ArrayList<AccessPointSighting>();

        pIn.readList(mScanResults, null);
    }

    public static HocLocation createFromLocation(Location pLocation) {
        if (pLocation == null) {
            return null;
        } else {
            HocLocation hocLocation = new HocLocation(pLocation);
            return hocLocation;
        }
    }

    public static HocLocation createFromLocation(Location pLocation, List<ScanResult> pScanResults) {
        if (pLocation == null) {
            return null;
        } else {
            HocLocation hocLocation = new HocLocation(pLocation);
            hocLocation.setScanResult(pScanResults);
            return hocLocation;
        }
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
        int hoccability = 0;

        if (mScanResults != null && mScanResults.size() > 0) {
            hoccability += 1;
        }

        if (getAccuracy() < 200) {
            hoccability += 2;
        } else if (getAccuracy() < 5000) {
            hoccability += 1;
        }

        return hoccability;
    }

    public String getProblemDescription() {

        return null;
    }

    public String getResolvingHelp() {
        return null;
    }

    @Override
    public void writeToParcel(Parcel pDest, int pFlags) {
        super.writeToParcel(pDest, pFlags);

        pDest.writeList(mScanResults);
    }

    public static final Parcelable.Creator<HocLocation> CREATOR = new Parcelable.Creator<HocLocation>() {
                                                                    public HocLocation createFromParcel(
                                                                            Parcel in) {
                                                                        return new HocLocation(in);
                                                                    }

                                                                    public HocLocation[] newArray(
                                                                            int size) {
                                                                        return new HocLocation[size];
                                                                    }
                                                                };

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
}
