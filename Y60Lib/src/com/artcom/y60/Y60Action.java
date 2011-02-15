package com.artcom.y60;

public abstract class Y60Action {

    public static final String SEARCH                     = "y60.intent.SEARCH";
    public static final String SEARCH_BC                  = "y60.intent.SEARCH_BC";

    public static final String VOICE_CONTROL              = "y60.intent.VOICE_CONTROL";
    public static final String VOICE_CONTROL_BC           = "y60.intent.VOICE_CONTROL_BC";

    public static final String SHUTDOWN_ACTIVITIES_BC     = "y60.intent.SHUTDOWN_ACTIVITIES";

    public static final String START_Y60                  = "y60.intent.SHOW_Y60";

    public static final String SERVICE_DEVICE_CONTROLLER  = "y60.intent.SERVICE_DEVICE_CONTROLLER";

    public static final String SERVICE_STATUS_WATCHER     = "y60.intent.SERVICE_STATUS_WATCHER";

    public static final String INIT_PROXY_SERVICES        = "y60.intent.INIT_PROXY_SERVICES";
    public static final String SERVICE_HTTP_PROXY         = "y60.intent.SERVICE_HTTP_PROXY";
    public static final String SERVICE_GOM_PROXY          = "y60.intent.SERVICE_GOM_PROXY";

    public static final String SHUTDOWN_SERVICES_BC       = "y60.intent.SHUTDOWN_SERVICES_BC";
    public static final String REQUEST_STATUS_BC          = "y60.intent.REQUEST_STATUS_BC";
    public static final String REPORT_STATUS_BC           = "y60.intent.REPORT_STATUS_BC";
    public static final String SERVICE_GOM_PROXY_READY    = "y60.intent.SERVICE_GOM_PROXY_READY";
    public static final String SERVICE_GOM_PROXY_CLEARED  = "y60.intent.SERVICE_GOM_PROXY_CLEARED";
    public static final String SERVICE_GOM_PROXY_DOWN     = "y60.intent.SERVICE_GOM_PROXY_DOWN";
    public static final String SERVICE_HTTP_PROXY_READY   = "y60.intent.SERVICE_HTTP_PROXY_READY";
    public static final String SERVICE_HTTP_PROXY_CLEARED = "y60.intent.SERVICE_HTTP_PROXY_CLEARED";
    public static final String SERVICE_HTTP_PROXY_DOWN    = "y60.intent.SERVICE_HTTP_PROXY_DOWN";

    public static final String DEVICE_CONTROLLER_READY    = "y60.intent.DEVICE_CONTROLLER_READY";
    public static final String DEVICE_CONTROLLER_DOWN     = "y60.intent.DEVICE_CONTROLLER_DOWN";
    public static final String JAVASCRIPT_VIEWS_READY     = "y60.intent.JAVASCRIPT_VIEWS_READY";
    public static final String GLOBAL_OBSERVERS_READY     = "y60.intent.GLOBAL_OBSERVERS_READY";
    public static final String SEARCH_READY               = "y60.intent.SEARCH_READY";
    public static final String SEARCH_CONNECTION_FAILED   = "y60.intent.SEARCH_CONNECTION_FAILED";
    public static final String SEARCH_DOWN                = "y60.intent.SEARCH_DOWN";
    public static final String CALL_READY                 = "y60.intent.CALL_READY";
    public static final String CALL_DOWN                  = "y60.intent.CALL_DOWN";
    public static final String PRELOAD_BROWSE_READY       = "y60.intent.PRELOAD_BROWSE_READY";

    public static final String START_BLOCKING_AUDIO       = "y60.intent.START_BLOCKING_AUDIO";
    public static final String STOP_BLOCKING_AUDIO        = "y60.intent.STOP_BLOCKING_AUDIO";
    public static final String PAUSE_AUDIO                = "y60.intent.PAUSE_AUDIO";
    public static final String RESUME_AUDIO               = "y60.intent.RESUME_AUDIO";

    public static final String INIT_READY                 = "y60.intent.INIT_READY";

    public static final String GOM_NOTIFICATION_BC        = "y60.intent.GOM_NOTIFICATION";

    public static final String COMMAND_BUFFER_FLUSH_BC    = "y60.intent.COMMAND_BUFFER_FLUSH";
    public static final String COMMAND_BUFFER_DELETE_BC   = "y60.intent.COMMAND_BUFFER_DELETE";

    public static final String SYNERGY_SERVER             = "y60.intent.SYNERGY_SERVER";

    public static final String MONITOR_GOM_NOTIFICATION   = "y60.intent.MONITOR_GOM_NOTIFICATION";
    public static final String RESET_BC                   = "y60.intent.RESET";
    public static final String RESET_BC_GOM_PROXY         = "y60.intent.RESET_BC_GOM_PROXY";
    public static final String RESET_BC_HTTP_PROXY        = "y60.intent.RESET_BC_HTTP_PROXY";
    public static final String PRELOAD_CACHE              = "y60.intent.PRELOAD_CACHE";
    public static final String MOVIE_CONTROL_BC           = "y60.intent.MOVIE_CONTROL_BC";
    public static final String PICTURE_CONTROL_BC         = "y60.intent.PICTURE_CONTROL_BC";
    public static final String USER_FEEDBACK_BC           = "y60.intent.USER_FEEDBACK_BC";

    public static final String SERVICE_VNC_SERVER         = "y60.intent.SERVICE_VNC_SERVER";
    public static final String KILL_VNC_SERVER            = "y60.intent.KILL_VNC_SERVER";
    public static final String VNC_SERVICE_READY          = "y60.intent.VNC_SERVICE_READY";
}
