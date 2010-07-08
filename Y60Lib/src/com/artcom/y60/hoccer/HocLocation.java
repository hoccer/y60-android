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

    public int getHoccability() {
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

    public HocLocationProblem getHocLocationProblem(ProblemDescriptor descriptor) {

        HocLocationProblem problem = new HocLocationProblem();

        switch (0) {
            case 0:
                problem.setProblem(descriptor
                        .getDescription(ProblemDescriptor.Problems.HOCCABILITY_BAD));

                String suggestion = descriptor.getDescription(ProblemDescriptor.HOCCABILITY_INTRO)
                        + " "
                        + descriptor
                                .getDescription(ProblemDescriptor.Suggestions.HOCCABILITY_GO_OUTSIDE)
                        + " "
                        + descriptor.getDescription(ProblemDescriptor.HOCCABILITY_HINT_JOIN)
                        + " "
                        + descriptor
                                .getDescription(ProblemDescriptor.Suggestions.HOCCABILITY_TURN_WIFI_ON)
                        + ".";

                problem.setRecoverySuggestion(suggestion);
                break;

            case 1:

                // if (mScanResults == null || mScanResults.size() == 0) {
                // recoverySuggestions.add(descriptor
                // .getDescription(ProblemDescriptor.HOCCABILITY_TURN_WIFI_ON));
                // }
                //                
                // StringBuffer recoverySuggestion = new StringBuffer();
                // recoverySuggestion.append(descriptor
                // .getDescription(ProblemDescriptor.HOCCABILITY_INTRO));
                // if (recoverySuggestions.size() > 0) {
                // recoverySuggestion.append(recoverySuggestions.get(0));
                // }
                // if (recoverySuggestions.size() > 1) {
                // recoverySuggestion.append(descriptor
                // .getDescription(ProblemDescriptor.HOCCABILITY_HINT_JOIN)
                // + recoverySuggestions.get(1));
                // }

                break;
            case 2:

                break;
            case 3:

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
