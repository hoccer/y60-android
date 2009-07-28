package com.artcom.y60;

import java.io.File;
import java.io.FileReader;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AssertionFailedError;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * Adds some helpers to the ActivityInstrumentationTestCase
 * 
 * @author arne
 * 
 * @param <T>
 *            the activity class to be tested
 */
public abstract class Y60ActivityInstrumentationTest<T extends Activity> extends
        ActivityInstrumentationTestCase2<T> {

    // Constants ---------------------------------------------------------
    private static String      LOG_TAG       = "Y60ActivityInstrumentationTest";

    /**
     * Horizontal resolution of the display in portrait orientation. To be used
     * for testing only, thus protected.
     */
    protected final static int SCREEN_WIDTH  = 320;

    /**
     * Vertical resolution of the display in portrait orientation. To be used
     * for testing only, thus protected.
     */
    protected final static int SCREEN_HEIGHT = 480;

    // Constructors ------------------------------------------------------

    public Y60ActivityInstrumentationTest(String pkg, Class<T> activityClass,
            boolean initialTouchMode) {
        super(pkg, activityClass);
        // TODO Auto-generated constructor stub
    }

    public Y60ActivityInstrumentationTest(String pkg, Class<T> activityClass) {
        super(pkg, activityClass);
        // TODO Auto-generated constructor stub
    }

    // Public Instance Methods -------------------------------------------

    @Override
    public void setUp() throws Exception {
        d(" --- " + getName()
                + " -- setUp ------------------------------------------------------------");
        Logger.logMemoryInfo(tag(), getInstrumentation().getContext());
        reset();

        super.setUp();
        assertNoErrorLogOnSdcard();

    }

    void assertNoErrorLogOnSdcard() throws Exception {

        File f = new File("/sdcard/error_log.txt");
        if (!f.exists()) {
            return;
        }

        Logger.v(LOG_TAG, "in assert Error!");
        FileReader fr;
        fr = new FileReader("/sdcard/error_log.txt");
        char[] inputBuffer = new char[(int) f.length()];

        fr.read(inputBuffer);
        String errorLog = new String(inputBuffer);
        fr.close();
        Logger.e(LOG_TAG, "Error log is: '" + errorLog + " '");
        f.delete();
        throw new AssertionFailedError("This test stumbled upon an error from " + errorLog);
    }

    @Override
    public void tearDown() throws Exception {

        d(" --- " + getName() + " -- tearDown -----"
                + "-------------------------------------------------------");
        reset();
        Logger.logMemoryInfo(tag(), getInstrumentation().getContext());

        super.tearDown();
        assertNoErrorLogOnSdcard();
    }

    // Protected Instance Methods ----------------------------------------

    protected void assertIsShown(int pViewId) {

        View view = getActivity().findViewById(pViewId);
        assertTrue("view '" + getActivity().getResources().getResourceName(pViewId)
                + "' should be visible", view.isShown());

    }

    /**
     * In order for a fling gesture to be detected by the Android
     * GestureDetector class, it has to consist of a sequence of small move
     * events. This method takes care of generating these events.
     */
    protected void fling(Direction pDirection) {

        fling(pDirection, SCREEN_HEIGHT - 10);
    }

    protected void fling(Direction pDirection, int pY) {

        // fling to next
        int y = pY;
        int fromX = SCREEN_WIDTH / 2;
        int deltaX = (pDirection == Direction.LEFT ? -4 : 4);

        long time = SystemClock.uptimeMillis();

        MotionEvent down = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, fromX, y, 1);

        int x = fromX;

        MotionEvent[] moves = new MotionEvent[20];
        for (int mov = 0; mov < 20; mov++) {

            time += 10;
            x += deltaX;

            moves[mov] = MotionEvent.obtain(time, time, MotionEvent.ACTION_MOVE, x, y, 1);
        }

        MotionEvent up = MotionEvent.obtain(time, time, MotionEvent.ACTION_UP, x, y, 1);

        Instrumentation instrumentation = getInstrumentation();
        instrumentation.sendPointerSync(down);
        instrumentation.waitForIdleSync();
        for (MotionEvent move : moves) {
            instrumentation.sendPointerSync(move);
        }
        instrumentation.sendPointerSync(up);
        instrumentation.waitForIdleSync();
    }

    protected void touch(int pX, int pY) {

        Logger.v(LOG_TAG, "touching ", pX, ", ", pY);
        sendMotionEventAndSync(MotionEvent.ACTION_DOWN, pX, pY);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Logger.e(LOG_TAG, e);
        }
    }

    protected void release(int pX, int pY) {

        Logger.v(LOG_TAG, "release ", pX, ", ", pY);
        sendMotionEventAndSync(MotionEvent.ACTION_UP, pX, pY);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Logger.e(LOG_TAG, e);
        }
    }

    protected void move(int pToX, int pToY) {

        sendMotionEventAndSync(MotionEvent.ACTION_MOVE, pToX, pToY);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Logger.e(LOG_TAG, e);
        }
    }

    protected void moveAndRelease(int pToX, int pToY) {

        move(pToX, pToY);
        release(pToX, pToY);

    }

    protected void touch(View pView) {

        int y = pView.getTop() + pView.getHeight() / 2;
        int x = pView.getLeft() + pView.getWidth() / 2;

        touch(x, y);
    }

    protected void release(View pView) {

        int y = pView.getTop() + pView.getHeight() / 2;
        int x = pView.getLeft() + pView.getWidth() / 2;

        release(x, y);
    }

    protected void sendMotionEventAndSync(int pAction, int pX, int pY) {

        long time = SystemClock.uptimeMillis();

        MotionEvent eve = MotionEvent.obtain(time, time, pAction, pX, pY, 1);

        Instrumentation instrumentation = getInstrumentation();
        instrumentation.sendPointerSync(eve);
        instrumentation.waitForIdleSync();
    }

    /**
     * Convenience method for generating DOWN and UP events. See
     * android.view.KeyEvent for key codes.
     */
    protected void pressKey(int pKeyCode, long pDurationMillis) throws InterruptedException {

        Instrumentation instrumentation = getInstrumentation();
        instrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, pKeyCode));
        Thread.sleep(pDurationMillis);
        instrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, pKeyCode));
    }

    protected void pressKeySequence(String pText) {
        Instrumentation instrumentation = getInstrumentation();
        instrumentation.sendStringSync(pText);
    }

    /**
     * Shorthand for <code>getClass().getSimpleName()</code>, to be used for
     * logcat logging.
     */
    protected String tag() {

        return getClass().getSimpleName();
    }

    protected void v(Object... pToLog) {

        Logger.v(tag(), pToLog);
    }

    protected void d(Object... pToLog) {

        Logger.d(tag(), pToLog);
    }

    protected void i(Object... pToLog) {

        Logger.i(tag(), pToLog);
    }

    protected void w(Object... pToLog) {

        Logger.w(tag(), pToLog);
    }

    protected void e(Object... pToLog) {

        Logger.e(tag(), pToLog);
    }

    protected void reset() {

        Intent reset = new Intent(Y60Action.RESET_BC);
        // getInstrumentation().getContext().sendBroadcast(reset);
    }

}
