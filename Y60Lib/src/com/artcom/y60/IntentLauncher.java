package com.artcom.y60;

import android.content.Intent;

public class IntentLauncher extends SlotLauncher {

    private static final String LOG_TAG = IntentLauncher.class.getName();

    // Instance Variables ------------------------------------------------

    private final Intent        mIntent;

    // Constructors ------------------------------------------------------

    public IntentLauncher(Intent pIntent) {

        mIntent = pIntent;
    }

    public IntentLauncher(String pActivityClass) {

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
