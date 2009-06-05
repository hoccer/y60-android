package com.artcom.y60;

import com.artcom.y60.Y60ActivityInstrumentationTest.Condition;

public class TestHelper {

    public static void assertTrueAsync(String pFailMessage, long pWaitMillis, Condition pCon) {

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < pWaitMillis) {

            if (pCon.isSatisfied()) {

                return;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        throw new AssertionError(pFailMessage);
    }

    public static void assertFalseAsync(String pFailMessage, long pWaitMillis, final Condition pCon) {

        assertTrueAsync(pFailMessage, pWaitMillis, new Condition() {
            public boolean isSatisfied() {

                return !pCon.isSatisfied();
            }
        });
    }

}
