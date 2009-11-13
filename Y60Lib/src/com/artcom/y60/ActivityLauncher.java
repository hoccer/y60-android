package com.artcom.y60;

import android.content.Intent;

public class ActivityLauncher extends SlotLauncher {

    private static final String LOG_TAG = ActivityLauncher.class.getName();

    // Instance Variables ------------------------------------------------

    protected final Intent      mIntent;

    // Constructors ------------------------------------------------------

    public ActivityLauncher(Intent pIntent) {

        mIntent = pIntent;
    }

    public ActivityLauncher(String pActivityClass) {

        mIntent = IntentHelper.getExplicitIntentForClass(pActivityClass);
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void launch() {
        Logger.v(LOG_TAG, "launch intent slot launcher");
        getContext().startActivity(mIntent);
    }

    @Override
    public String toString() {

        return mIntent.toString();
    }

    public String getActivityClass() {

        return mIntent.getComponent().getClassName();
    }
}
