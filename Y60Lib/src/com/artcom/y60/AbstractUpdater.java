package com.artcom.y60;

public abstract class AbstractUpdater {

    private boolean      mDoUpdates  = true;
    private boolean      mUpdateNow  = false;

    private final Thread updateThrad = new Thread() {
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

    public void start() {
        updateThrad.start();
    }

    public void join() throws InterruptedException {
        updateThrad.join();
    }

    public void refreshNow() {
        mUpdateNow = true;
    }

    public void shutdown() {
        mDoUpdates = false;
    }

    abstract protected void update();

}
