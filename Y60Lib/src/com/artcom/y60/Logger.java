package com.artcom.y60;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Logger {

    private static Level    sLevel         = Level.VERBOSE;
    public static final int MAX_LINE_WIDTH = 500;

    public enum Level implements Comparable<Level> {

        VERBOSE(0, Log.VERBOSE, "verbose"), DEBUG(1, Log.DEBUG, "debug"), INFO(2, Log.INFO, "info"), WARN(
                3, Log.WARN, "warn"), ERROR(4, Log.ERROR, "error");

        private final static Map<String, Level> BY_NAME;

        public static List<Level> getLogLevels() {
            ArrayList<Level> levels = new ArrayList<Level>();
            levels.addAll(BY_NAME.values());
            Collections.sort(levels);
            return levels;
        }

        static {

            BY_NAME = new HashMap<String, Level>();
            BY_NAME.put(VERBOSE.toString(), VERBOSE);
            BY_NAME.put(DEBUG.toString(), DEBUG);
            BY_NAME.put(INFO.toString(), INFO);
            BY_NAME.put(WARN.toString(), WARN);
            BY_NAME.put(ERROR.toString(), ERROR);
        }

        public static Level fromString(String pName) {

            return BY_NAME.get(pName.toLowerCase());
        }

        private final int    mAsInt;
        private final int    mPriority;
        private final String mName;

        Level(int pAsInt, int pPriority, String pName) {

            mAsInt = pAsInt;
            mPriority = pPriority;
            mName = pName;
        }

        @Override
        public String toString() {

            return mName;
        }

        public void log(String pTag, Object[] pToLog) {

            if (sLevel.shows(this)) {

                if (pTag == null) {

                    throw new NullPointerException(
                            "Log tag is null! This must be some kind of accident?!");
                }

                StringBuilder builder = new StringBuilder();
                for (Object obj : pToLog) {
                    builder.append(toString(obj));
                }

                Log.println(mPriority, pTag, builder.toString());

                // String line = builder.toString();
                // if (line.length() < MAX_LINE_WIDTH) {
                // Log.println(mPriority, pTag, builder.toString());
                // } else {
                // int start = 0;
                // int end = MAX_LINE_WIDTH;
                // do {
                // Log.println(mPriority, pTag, line.substring(start, end));
                // start = end;
                // end = Math.min(start + MAX_LINE_WIDTH - 1, line.length() -
                // 1);
                // } while (start < line.length() - 1);
                // }
            }
        }

        public boolean shows(Level pLevel) {

            boolean shows = pLevel.asInt() >= mAsInt;
            // Log.v("Logger", toString()+".shows("+pLevel.toString()+"): "+
            // shows);
            return shows;
        }

        public int asInt() {

            return mAsInt;
        }

        private String toString(Object obj) {

            if (obj instanceof Throwable) {

                // Throwable t = (Throwable) obj;
                // String msg = String.valueOf(t.getMessage());
                // StringWriter sw = new StringWriter();
                // PrintWriter pw = new PrintWriter(sw);
                // t.printStackTrace(pw);
                // String stack = sw.toString();
                // StringBuilder builder = new StringBuilder(msg.length() +
                // stack.length() + 1);
                // builder.append(msg);
                // builder.append(" ");
                // builder.append(stack);
                // return builder.toString();

                return Log.getStackTraceString((Throwable) obj);

            } else {

                return String.valueOf(obj);
            }
        }
    };

    // Static Methods ----------------------------------------------------

    public static void setFilterLevel(Level pLevel) {

        Log.i("Logger", "setting filter level to '" + pLevel.toString() + "'");
        sLevel = pLevel;
    }

    public static Level getFilterLevel() {

        return sLevel;
    }

    public static void vIntent(String pTag, String log, Intent intent) {

        String keyset = "no keyset";
        String values = "no values";
        if (intent.getExtras() != null) {
            keyset = "";
            values = "";
            for (String key : intent.getExtras().keySet()) {
                if (key.equals(Intent.EXTRA_STREAM)) {
                    values += key + ": " + intent.getExtras().getParcelable(Intent.EXTRA_STREAM)
                            + ", ";
                } else if (key.equals(Intent.EXTRA_TEXT)) {
                    values += key + ": " + intent.getExtras().getString(Intent.EXTRA_TEXT) + ", ";

                } else if (key.equals(Intent.EXTRA_SUBJECT)) {
                    values += key + ": " + intent.getExtras().getString(Intent.EXTRA_SUBJECT)
                            + ", ";
                }
                keyset += key + " ";
            }
        }

        Object[] logIntent = {
                log,
                (intent + " getAction: " + intent.getAction() + " getData: " + intent.getData()
                        + " getType: " + intent.getType() + " extras: " + intent.getExtras()
                        + " keyset: " + keyset + " values: " + values), " - END" };

        Level.VERBOSE.log(pTag, logIntent);
    }

    public static void v(String pTag, Object... pToLog) {

        Level.VERBOSE.log(pTag, pToLog);
    }

    public static void d(String pTag, Object... pToLog) {

        Level.DEBUG.log(pTag, pToLog);
    }

    public static void i(String pTag, Object... pToLog) {

        Level.INFO.log(pTag, pToLog);
    }

    public static void w(String pTag, Object... pToLog) {

        Level.WARN.log(pTag, pToLog);
    }

    public static void e(String pTag, Object... pToLog) {

        Level.ERROR.log(pTag, pToLog);
    }

    public static void logMemoryInfo(String pLogTag, Context pContext) {
        ActivityManager actMgr = (ActivityManager) pContext
                .getSystemService(pContext.ACTIVITY_SERVICE);
        MemoryInfo memInfo = new MemoryInfo();
        actMgr.getMemoryInfo(memInfo);
        d(pLogTag, "available memory: ", readable(memInfo.availMem));
        d(pLogTag, "threshold: ", readable(memInfo.threshold));
        d(pLogTag, "free runtime memory: ", readable(Runtime.getRuntime().freeMemory()));
    }

    private static String readable(float pMemory) {

        return (Math.round(pMemory / 100000) / 10) + "MB";
    }
}
