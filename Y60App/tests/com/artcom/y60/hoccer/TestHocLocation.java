package com.artcom.y60.hoccer;

import android.location.Location;
import android.test.AndroidTestCase;

import com.artcom.y60.data.ProblemDescriptor;

public class TestHocLocation extends AndroidTestCase {

    public void testGoodLocationAndNoBSSIDS() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(199);

        HocLocation hocLocation = new HocLocation(location);
        assertEquals(2, hocLocation.getQuality());
    }

    public void testBadLocationAndNoBSSIDS() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(HocLocation.WORST_ACCURACY + 1);

        HocLocation hocLocation = new HocLocation(location);
        assertEquals(0, hocLocation.getQuality());
    }

    public void testMediumLocationAndNoBSSIDS() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(1999);

        HocLocation hocLocation = new HocLocation(location);
        assertEquals(1, hocLocation.getQuality());

        location.setAccuracy(HocLocation.OPTIMAL_ACCURACY + 1);

        hocLocation = new HocLocation(location);
        assertEquals(1, hocLocation.getQuality());
    }

    public void testNoProblemWhenLocationIsGood() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(HocLocation.OPTIMAL_ACCURACY - 1);

        HocLocation hocLocation = new HocLocation(location);
        assertEquals(2, hocLocation.getQuality());
        assertEquals(hocLocation.getHocLocationProblem(new MockedProblemDescriptor())
                .getRecoverySuggestion(),
                ProblemDescriptor.Suggestions.HOCCABILITY_2_GPS_GOOD_BSSIDS_BAD);
    }

    public void testRecoverySuggestionContainsGoOutsideWhenLocationIsBad() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(HocLocation.WORST_ACCURACY - 1);

        HocLocation hocLocation = new HocLocation(location);
        assertEquals(hocLocation.getHocLocationProblem(new MockedProblemDescriptor())
                .getRecoverySuggestion(),
                ProblemDescriptor.Suggestions.HOCCABILITY_1_GPS_OK_BSSIDS_BAD);
    }

    private class MockedProblemDescriptor implements ProblemDescriptor {

        @Override
        public String getDescription(String id) {
            return id;
        }
    }

}
