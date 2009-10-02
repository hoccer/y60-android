package com.artcom.y60;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

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
            Toast.makeText(pContext, "SEARCH is ready", Toast.LENGTH_SHORT).show();

        } else if (pIntent.getAction().equals(Y60Action.CALL_READY)) {
            isCallReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ call is ready");
            launchHomeScreenIfReady(pContext);
            Toast.makeText(pContext, "CALL is ready", Toast.LENGTH_SHORT).show();

        } else if (pIntent.getAction().equals(Y60Action.JAVASCRIPT_VIEWS_READY)) {
            isJavaScriptViewsReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ javascript views is ready");
            launchHomeScreenIfReady(pContext);
            Toast.makeText(pContext, "JS VIEWS are ready", Toast.LENGTH_SHORT).show();

        } else if (pIntent.getAction().equals(Y60Action.GLOBAL_OBSERVERS_READY)) {
            isGlobalObserversReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ global observers is ready");
            launchHomeScreenIfReady(pContext);
            Toast.makeText(pContext, "GLOBAL OBSERVERS is ready", Toast.LENGTH_SHORT).show();

        } else if (pIntent.getAction().equals(Y60Action.PRELOAD_BROWSE_READY)) {
            isPreloadBrowseViewsReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ preload browse is ready");
            launchHomeScreenIfReady(pContext);
            Toast.makeText(pContext, "PRELOAD_BROWSE_READY is ready", Toast.LENGTH_SHORT).show();

        } else if (pIntent.getAction().equals(Y60Action.VIDEO_PRELOAD_READY)) {
            isVideoPreloadReady = true;
            Logger.v(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ video preload is ready");
            launchHomeScreenIfReady(pContext);
            Toast.makeText(pContext, "VIDEO_PRELOAD_READY is ready", Toast.LENGTH_SHORT).show();
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
        Logger.v(LOG_TAG, "isEveryThingReady? \n search: ", isSearchReady, " \ncall: ",
                isCallReady, " \nglobalObservers: ", isGlobalObserversReady, " \njsViews: ",
                isJavaScriptViewsReady, " \nvideoPreload: ", isVideoPreloadReady,
                " \npreloadBrowse: ", isPreloadBrowseViewsReady, "\naddress: ", this.toString());
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

        Logger.v(LOG_TAG, "RESET!?\n search: ", isSearchReady, " \ncall: ", isCallReady,
                " \nglobalObservers: ", isGlobalObserversReady, " \njsViews: ",
                isJavaScriptViewsReady, " \nvideoPreload: ", isVideoPreloadReady,
                " \npreloadBrowse: ", isPreloadBrowseViewsReady, "\naddress: ", this.toString());
    }
}
