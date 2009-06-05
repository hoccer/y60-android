package com.artcom.y60;


public class TestHelper {

    public interface Condition {
    
        public boolean isSatisfied();
    }

    public static void assertTrueAsync(String pFailMessage, long pWaitMillis, TestHelper.Condition pCon) {

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

    public static void assertFalseAsync(String pFailMessage, long pWaitMillis, final TestHelper.Condition pCon) {

        assertTrueAsync(pFailMessage, pWaitMillis, new TestHelper.Condition() {
            public boolean isSatisfied() {

                return !pCon.isSatisfied();
            }
        });
    }

}
