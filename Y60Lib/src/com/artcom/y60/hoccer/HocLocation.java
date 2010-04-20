package com.artcom.y60.hoccer;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import com.artcom.y60.data.ProblemDescriptor;

public class HocLocation extends Location {

    private List<ScanResult> mScanResults = null;

    private HocLocation(Location pLocation) {
        super(pLocation);
    }

    private HocLocation(Parcel pIn) {
        super(Location.CREATOR.createFromParcel(pIn));
        mScanResults = new ArrayList<ScanResult>();

        pIn.readList(mScanResults, null);
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

    public List<ScanResult> getScanResults() {
        return mScanResults;
    }

    public void setScanResult(List<ScanResult> pScanResults) {
        mScanResults = pScanResults;
    }

    public int getHoccability() {
        int hoccability = 0;

        if (mScanResults != null && mScanResults.size() > 0) {
            hoccability += 1;
        }

        if (getAccuracy() < 200) {
            hoccability += 2;
        } else if (getAccuracy() < 2000) {
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
        if (getHoccability() > 1) {
            return null;
        }

        HocLocationProblem problem = new HocLocationProblem();
        problem.setProblem(descriptor.getDescription("location_inaccurate"));

        ArrayList<String> recoverySuggestions = new ArrayList<String>();
        recoverySuggestions.add(descriptor.getDescription("location_improve_go_outside"));
        if (mScanResults == null || mScanResults.size() == 0) {
            recoverySuggestions.add(descriptor.getDescription("location_improve_turn_wifi_on"));
        }

        StringBuffer recoverySuggestion = new StringBuffer();
        recoverySuggestion.append(descriptor.getDescription("location_intro"));
        if (recoverySuggestions.size() > 0) {
            recoverySuggestion.append(recoverySuggestions.get(0));
        }
        if (recoverySuggestions.size() > 1) {
            recoverySuggestion.append(descriptor.getDescription("location_tip_join")
                    + recoverySuggestions.get(1));
        }

        problem.setRecoverySuggestion(recoverySuggestion.toString());
        return problem;
    }
}
