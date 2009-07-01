package com.artcom.y60;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class Logger {

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

        private int mAsInt;
        private int mPriority;
        private String mName;

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

                StringBuilder builder = new StringBuilder();
                for (Object obj : pToLog) {
                    builder.append(toString(obj));
                }

                Log.println(mPriority, pTag, builder.toString());
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

                Throwable t = (Throwable) obj;
                String msg = String.valueOf(t.getMessage());
                String stack = Log.getStackTraceString(t);
                StringBuilder builder = new StringBuilder(msg.length() + stack.length() + 1);
                builder.append(msg);
                builder.append(" ");
                builder.append(stack);
                return builder.toString();

            } else {

                return String.valueOf(obj);
            }
        }
    };

    // Static Variables ----------------------------------------------------

    private static Level sLevel = Level.VERBOSE;

    // Static Methods ----------------------------------------------------

    public static void setFilterLevel(Level pLevel) {

        Log.i("Logger", "setting filter level to '" + pLevel.toString() + "'");
        sLevel = pLevel;
    }

    public static Level getFilterLevel() {

        return sLevel;
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
}
