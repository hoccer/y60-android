package com.artcom.y60;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RunLevelReceiver extends BroadcastReceiver {

    private static final String LOG_TAG                   = "RunLevelReceiver";

    private boolean             isSearchReady             = false;
    private boolean             isCallReady               = false;
    private boolean             isGlobalObserversReady    = false;
    private boolean             isPreloadBrowseViewsReady = false;
    private boolean             isDeviceControllerReady   = false;
    private boolean             isGomProxyReady           = false;
    private boolean             isHttpProxyReady          = false;
    private ProgressListener    mProgressListener;

    private boolean             mIsHomescreenStarted      = false;

    public void setProgressListener(ProgressListener pProgressListener) {
        mProgressListener = pProgressListener;
    }

    public int getNoOfLevels() {
        return 4;
    }

    public void updateIfNotNull() {
        if (mProgressListener != null) {
            mProgressListener.update();
        }
    }

    @Override
    public void onReceive(Context pContext, Intent pIntent) {

        Logger.v(LOG_TAG, "onReceive: ", pIntent,
                "has pIntent.hasExtra(IntentExtraKeys.IS_IN_INIT_CHAIN)", pIntent
                        .hasExtra(IntentExtraKeys.IS_IN_INIT_CHAIN));

        if (pIntent.hasExtra(IntentExtraKeys.IS_IN_INIT_CHAIN)) {

            Logger.v(LOG_TAG, "in init chain: ", pIntent.getBooleanExtra(
                    IntentExtraKeys.IS_IN_INIT_CHAIN, false), " the action: ", pIntent.getAction());

            if (pIntent.getAction().equals(Y60Action.SEARCH_READY)) {
                isSearchReady = true;
                Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ search is ready");

            } else if (pIntent.getAction().equals(Y60Action.CALL_READY)) {
                isCallReady = true;
                Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ call is ready");

            } else if (pIntent.getAction().equals(Y60Action.GLOBAL_OBSERVERS_READY)) {
                isGlobalObserversReady = true;
                Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ global observers is ready");
                updateIfNotNull();

            } else if (pIntent.getAction().equals(Y60Action.PRELOAD_BROWSE_READY)) {
                isPreloadBrowseViewsReady = true;
                Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ preload browse is ready");

            } else if (pIntent.getAction().equals(Y60Action.DEVICE_CONTROLLER_READY)) {
                isDeviceControllerReady = true;
                Logger.v(LOG_TAG,
                        "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ DEVICE_CONTROLLER_READY is ready");
                updateIfNotNull();

            } else if (pIntent.getAction().equals(Y60Action.SERVICE_GOM_PROXY_READY)) {
                isGomProxyReady = true;
                Logger.v(LOG_TAG,
                        "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ SERVICE_GOM_PROXY_READY is ready");
                updateIfNotNull();

            } else if (pIntent.getAction().equals(Y60Action.SERVICE_HTTP_PROXY_READY)) {
                isHttpProxyReady = true;
                Logger.v(LOG_TAG,
                        "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ SERVICE_HTTP_PROXY_READY is ready");
                updateIfNotNull();

            }

            launchHomeScreenIfReady(pContext);

            if (isEverythingReady()) {
                Logger.v(LOG_TAG, "EveryThingReady!!!, YES! \nsearch: ", isSearchReady,
                        " \ncall: ", isCallReady, " \nglobalObservers: ", isGlobalObserversReady,
                        " \npreloadBrowse: ",
                        isPreloadBrowseViewsReady, "\nisDeviceControllerReady: ",
                        isDeviceControllerReady, "\nisGomProxyReady: ", isGomProxyReady,
                        "\nisHttpProxyReady: ", isHttpProxyReady, "\naddress: ", this.toString());
            }
        }
    }

    private void launchHomeScreenIfReady(Context pContext) {
        if (areEssentialComponentsReady() && !mIsHomescreenStarted) {
            mIsHomescreenStarted = true;
            Logger.v(LOG_TAG, "Essential components +x? \nsearch: ", isSearchReady, " \ncall: ",
                    isCallReady, " \nglobalObservers: ", isGlobalObserversReady,
                    " \npreload browse: ", isPreloadBrowseViewsReady,
                    "\nisDeviceControllerReady: ", isDeviceControllerReady, "\nisGomProxyReady: ",
                    isGomProxyReady, "\nisHttpProxyReady: ", isHttpProxyReady, "\naddress: ", this
                            .toString());
            Logger.v(LOG_TAG, "essential components are ready!");
            Intent intent = new Intent(Y60Action.INIT_READY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pContext.startActivity(intent);
        }
    }

    public boolean isEverythingReady() {
        return isSearchReady && isCallReady && isGlobalObserversReady
                && isPreloadBrowseViewsReady && isGomProxyReady && isHttpProxyReady
                && isDeviceControllerReady;
    }

    public boolean areEssentialComponentsReady() {
        return isGlobalObserversReady && isGomProxyReady
                && isHttpProxyReady && isDeviceControllerReady;
    }

    public void reset() {
        isSearchReady = false;
        isCallReady = false;
        isGlobalObserversReady = false;
        isPreloadBrowseViewsReady = false;
        isGomProxyReady = false;
        isHttpProxyReady = false;
        isDeviceControllerReady = false;

        Logger.v(LOG_TAG, "RESET!?\n search: ", isSearchReady, " \ncall: ", isCallReady,
                " \nglobalObservers: ", isGlobalObserversReady, " \nbrowsePreload: ",
                isPreloadBrowseViewsReady,
                "\naddress: ", this.toString());
    }

    public boolean isSearchReady() {
        return isSearchReady;
    }

    public boolean isCallReady() {
        return isCallReady;
    }

    public boolean isGlobalObserversReady() {
        return isGlobalObserversReady;
    }

    public boolean isPreloadBrowseViewsReady() {
        return isPreloadBrowseViewsReady;
    }

    public boolean isDeviceControllerReady() {
        return isDeviceControllerReady;
    }

    public boolean isGomProxyReady() {
        return isGomProxyReady;
    }

    public boolean isHttpProxyReady() {
        return isHttpProxyReady;
    }

}
