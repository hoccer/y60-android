package com.artcom.y60;

public class ReferenceHelper {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "ReferenceHelper";

    // Static Methods ----------------------------------------------------

    public static void releaseDelayed(final Object pTarget, final long pDelay) {

        new Thread(new Runnable() {
            public void run() {

                Logger.d(LOG_TAG, "now waiting ", pDelay, "ms until releasing");
                Object target = pTarget;
                try {
                    Thread.sleep(pDelay);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Logger.d(LOG_TAG, "releasing ", target, " after ", pDelay, "ms");
                target = null;
            }
        }).start();
    }

}
