package com.artcom.y60;

public class DynamicSlotLauncher extends SlotLauncher {

    private static final String LOG_TAG = ActivityLauncher.class.getName();

    // Instance Variables ------------------------------------------------

    protected final Launchable  mRocket;

    // Constructors ------------------------------------------------------

    public DynamicSlotLauncher(Launchable pRocket) {

        mRocket = pRocket;

    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void launch() {
        Logger.v(LOG_TAG, "launch DynamicRocketLauncher");
        mRocket.execute();
    }

    @Override
    public String toString() {
        return "dynamic launcher";
    }

    public interface Launchable {
        // public SlotLauncher getLauncher();
        public void execute();
    }
}
