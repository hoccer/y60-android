package com.artcom.y60;

public abstract class AbstractUpdater {

    private static final String LOG_TAG    = "AbstractUpdater";

    private boolean             mDoUpdates = true;
    private boolean             mUpdateNow = false;

    private Thread              updateThread;

    public void start() {
        mDoUpdates = true;

        Logger.v(LOG_TAG, "Startting Abstract Updater");
        updateThread = new Thread() {
            @Override
            public void run() {

                while (mDoUpdates) {

                    update();

                    try {
                        for (int i = 0; i < 100; i++) {
                            if (!mDoUpdates) {
                                return;
                            }
                            if (mUpdateNow) {
                                update();
                                mUpdateNow = false;
                            }
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e1) {
                        break;
                    }
                }
            }

        };

        updateThread.start();

    }

    public void join() throws InterruptedException {
        updateThread.join();
    }

    public void refreshNow() {
        mUpdateNow = true;
    }

    public void shutdown() {
        mDoUpdates = false;
        try {
            join();
        } catch (InterruptedException e) {
        }
    }

    abstract protected void update();

}
