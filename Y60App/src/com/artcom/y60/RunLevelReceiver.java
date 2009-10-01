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

    @Override
    public void onReceive(Context pContext, Intent pIntent) {

        if (pIntent.getAction().equals(Y60Action.SEARCH_READY)) {
            isSearchReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ search is ready");
            launchHomeScreenIfReady(pContext);
        } else if (pIntent.getAction().equals(Y60Action.CALL_READY)) {
            isCallReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ call is ready");
            launchHomeScreenIfReady(pContext);
        } else if (pIntent.getAction().equals(Y60Action.JAVASCRIPT_VIEWS_READY)) {
            isJavaScriptViewsReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ javascript views is ready");
            launchHomeScreenIfReady(pContext);
        } else if (pIntent.getAction().equals(Y60Action.GLOBAL_OBSERVERS_READY)) {
            isGlobalObserversReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ global observers is ready");
            launchHomeScreenIfReady(pContext);
        } else if (pIntent.getAction().equals(Y60Action.PRELOAD_BROWSE_READY)) {
            isPreloadBrowseViewsReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ preload browse is ready");
            launchHomeScreenIfReady(pContext);
        } else if (pIntent.getAction().equals(Y60Action.VIDEO_PRELOAD_READY)) {
            isVideoPreloadReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ video preload is ready");
            launchHomeScreenIfReady(pContext);
        }

    }

    private void launchHomeScreenIfReady(Context pContext) {
        if (isEverythingReady()) {
            Intent intent = new Intent(Y60Action.INIT_READY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            pContext.startActivity(intent);
        }
    }

    private boolean isEverythingReady() {
        Logger.v(LOG_TAG, "isEveryThingReady? search: ", isSearchReady, " call: ", isCallReady,
                " globalObservers: ", isGlobalObserversReady, " jsViews: ", isJavaScriptViewsReady,
                " videoPreload: ", isVideoPreloadReady, " preloadBrowse: ",
                isPreloadBrowseViewsReady);
        return isSearchReady && isCallReady && isGlobalObserversReady && isJavaScriptViewsReady
                && isVideoPreloadReady && isPreloadBrowseViewsReady;
    }

    public void reset() {
        isSearchReady = false;
        isCallReady = false;
        isGlobalObserversReady = false;
        isJavaScriptViewsReady = false;
        isVideoPreloadReady = false;
        isPreloadBrowseViewsReady = false;
    }
}
