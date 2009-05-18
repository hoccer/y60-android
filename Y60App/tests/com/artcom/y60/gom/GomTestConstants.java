package com.artcom.y60.gom;

import android.net.Uri;

public abstract class GomTestConstants {

    // Constants ---------------------------------------------------------

    static final Uri TEST_REPOSITORY_URI;

    static final String FIXTURES = "/test/android/y60/infrastructure/";

    static {

        try {
            TEST_REPOSITORY_URI = Uri.parse("http://t-gom.service.t-gallery.act");

        } catch (Exception e) {

            throw new ExceptionInInitializerError(e);
        }
    }

}
