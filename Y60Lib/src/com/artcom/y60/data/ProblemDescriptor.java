package com.artcom.y60.data;

public interface ProblemDescriptor {

    public static class Problems {
        public static final String HOCCABILITY_BAD  = "hoccability_bad";
        public static final String HOCCABILITY_OK   = "hoccability_ok";
        public static final String HOCCABILITY_GOOD = "hoccability_good";
        public static final String NETWORK_OFF      = "network_off";
        public static final String NETWORK_BAD      = "network_bad";
        public static final String SDCARD_MISSING   = "sdcard_missing";
    }

    public static class Suggestions {

        public static final String HOCCABILITY_0                     = "hoccability_0";

        public static final String HOCCABILITY_1_GPS_BAD_BSSIDS_GOOD = "hoccability_1_gps_bad_bssids_good";
        public static final String HOCCABILITY_1_GPS_OK_BSSIDS_BAD   = "hoccability_1_gps_ok_bssids_bad";

        public static final String HOCCABILITY_2_GPS_OK_BSSIDS_GOOD  = "hoccability_2_gps_ok_bssids_good";
        public static final String HOCCABILITY_2_GPS_GOOD_BSSIDS_BAD = "hoccability_2_gps_good_bssids_bad";

        public static final String HOCCABILITY_3                     = "hoccability_3";

        public static final String NETWORK_OFF_SUGGESTION            = "network_off_suggestion";
        public static final String NETWORK_BAD_SUGGESTION            = "network_bad_suggestion";
        public static final String NO_HOC_LOCATION_SUGGESTION        = "no_hoc_location_suggestion";
        public static final String SDCARD_MISSING_SUGGESTION         = "sdcard_missing_suggestion";

    }

    public String getDescription(String id);
}
