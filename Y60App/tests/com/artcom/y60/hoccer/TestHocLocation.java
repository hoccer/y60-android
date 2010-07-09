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

        HocLocation hocLocation = HocLocation.createFromLocation(location);
        assertEquals(2, hocLocation.getHoccability());
    }

    public void testBadLocationAndNoBSSIDS() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(2001);

        HocLocation hocLocation = HocLocation.createFromLocation(location);
        assertEquals(0, hocLocation.getHoccability());
    }

    public void testMediumLocationAndNoBSSIDS() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(1999);

        HocLocation hocLocation = HocLocation.createFromLocation(location);
        assertEquals(1, hocLocation.getHoccability());

        location.setAccuracy(201);

        hocLocation = HocLocation.createFromLocation(location);
        assertEquals(1, hocLocation.getHoccability());
    }

    public void testNoProblemWhenLocationIsGood() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(199);

        HocLocation hocLocation = HocLocation.createFromLocation(location);
        assertNull(hocLocation.getHocLocationProblem(new MockedProblemDescriptor()));
    }

    public void testRecoverySuggestionContainsGoOutsideWhenLocationIsBad() {
        Location location = new Location("GPS_PROVIDER");
        location.setLongitude(13);
        location.setLatitude(42);
        location.setAccuracy(1999);

        HocLocation hocLocation = HocLocation.createFromLocation(location);
        assertTrue(hocLocation.getHocLocationProblem(new MockedProblemDescriptor())
                .getRecoverySuggestion().contains("go_outside"));

        assertTrue(hocLocation.getHocLocationProblem(new MockedProblemDescriptor())
                .getRecoverySuggestion().contains("turn_wifi_on"));
    }

    private class MockedProblemDescriptor implements ProblemDescriptor {

        @Override
        public String getDescription(String id) {
            return id;
        }
    }

}
