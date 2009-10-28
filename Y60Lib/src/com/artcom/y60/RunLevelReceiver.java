package com.artcom.y60;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RunLevelReceiver extends BroadcastReceiver {

    private static final String LOG_TAG                   = "RunLevelReceiver";

    private boolean             isSearchReady             = false;
    private boolean             isCallReady               = false;
    private boolean             isGlobalObserversReady    = false;
    private boolean             isJavaScriptViewsReady    = false;
    private boolean             isVideoPreloadReady       = false;
    private boolean             isPreloadBrowseViewsReady = false;
    private boolean             isDeviceControllerReady   = false;
    private boolean             isGomProxyReady           = false;
    private boolean             isHttpProxyReady          = false;
    private ProgressListener    mProgressListener;

    public void setProgressListener(ProgressListener pProgressListener) {
        mProgressListener = pProgressListener;
    }

    public int getNoOfLevels() {
        return 5;
    }

    public void toast(Context pContext, String pMessage) {
        // if (mProgressListener == null)
        // Toast.makeText(pContext, pMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceive(Context pContext, Intent pIntent) {

        if (pIntent.getAction().equals(Y60Action.SEARCH_READY)) {
            isSearchReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ search is ready");
            toast(pContext, "SEARCH is ready");

        } else if (pIntent.getAction().equals(Y60Action.CALL_READY)) {
            isCallReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ call is ready");
            toast(pContext, "CALL is ready");

        } else if (pIntent.getAction().equals(Y60Action.JAVASCRIPT_VIEWS_READY)) {
            isJavaScriptViewsReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ javascript views is ready");
            toast(pContext, "JS VIEWS are ready");

        } else if (pIntent.getAction().equals(Y60Action.GLOBAL_OBSERVERS_READY)) {
            isGlobalObserversReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ global observers is ready");
            toast(pContext, "GLOBAL OBSERVERS is ready");

        } else if (pIntent.getAction().equals(Y60Action.PRELOAD_BROWSE_READY)) {
            isPreloadBrowseViewsReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ preload browse is ready");
            toast(pContext, "PRELOAD_BROWSE_READY is ready");

        } else if (pIntent.getAction().equals(Y60Action.VIDEO_PRELOAD_READY)) {
            isVideoPreloadReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ video preload is ready");
            toast(pContext, "VIDEO_PRELOAD_READY is ready");

        } else if (pIntent.getAction().equals(Y60Action.DEVICE_CONTROLLER_READY)) {
            isDeviceControllerReady = true;
            toast(pContext, "DEVICE_CONTROLLER_READY");

        } else if (pIntent.getAction().equals(Y60Action.SERVICE_GOM_PROXY_READY)) {
            isGomProxyReady = true;
            toast(pContext, "SERVICE_GOM_PROXY_READY");

        } else if (pIntent.getAction().equals(Y60Action.SERVICE_HTTP_PROXY_READY)) {
            isHttpProxyReady = true;
            toast(pContext, "SERVICE_HTTP_PROXY_READY");

        }
        launchHomeScreenIfReady(pContext);
        if (mProgressListener != null) {
            mProgressListener.update();
        }

    }

    private void launchHomeScreenIfReady(Context pContext) {
        if (isEverythingReady()) {
            Logger.v(LOG_TAG, "everything is ready!");
            Intent intent = new Intent(Y60Action.INIT_READY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pContext.startActivity(intent);
        }
    }

    public boolean isEverythingReady() {
        Logger.v(LOG_TAG, "isEveryThingReady? \n search: ", isSearchReady, " \ncall: ",
                isCallReady, " \nglobalObservers: ", isGlobalObserversReady, " \njsViews: ",
                isJavaScriptViewsReady, " \nvideoPreload: ", isVideoPreloadReady,
                " \npreloadBrowse: ", isPreloadBrowseViewsReady, "\nisDeviceControllerReady: ",
                isDeviceControllerReady, "\nisGomProxyReady: ", isGomProxyReady,
                "\nisHttpProxyReady: ", isHttpProxyReady, "\naddress: ", this.toString());
        return /*isSearchReady && isCallReady && */isGlobalObserversReady
                && isJavaScriptViewsReady /*&& isVideoPreloadReady && isPreloadBrowseViewsReady*/
                && isGomProxyReady && isHttpProxyReady && isDeviceControllerReady;
    }

    public void reset() {
        isSearchReady = false;
        isCallReady = false;
        isGlobalObserversReady = false;
        isJavaScriptViewsReady = false;
        isVideoPreloadReady = false;
        isPreloadBrowseViewsReady = false;
        isGomProxyReady = false;
        isHttpProxyReady = false;
        isDeviceControllerReady = false;

        Logger.v(LOG_TAG, "RESET!?\n search: ", isSearchReady, " \ncall: ", isCallReady,
                " \nglobalObservers: ", isGlobalObserversReady, " \njsViews: ",
                isJavaScriptViewsReady, " \nvideoPreload: ", isVideoPreloadReady,
                " \npreloadBrowse: ", isPreloadBrowseViewsReady, "\naddress: ", this.toString());
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

    public boolean isJavaScriptViewsReady() {
        return isJavaScriptViewsReady;
    }

    public boolean isVideoPreloadReady() {
        return isVideoPreloadReady;
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
