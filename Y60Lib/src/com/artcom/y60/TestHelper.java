package com.artcom.y60;

public class TestHelper {

    public interface Condition {

        public boolean isSatisfied();
    }

    /**
     * Mesures current state of an object.
     */
    public interface Mesurement {

        public Object getActualValue();
    }

    /**
     * 
     * @param pFailMessage
     * @param pTimeout
     *            in milliseconds
     * @param pCon
     */
    public static void assertTrueAsync(String pFailMessage, long pTimeout, TestHelper.Condition pCon) {

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < pTimeout) {

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

    /**
     * 
     * @param pFailMessage
     * @param pTimeout
     *            in milliseconds
     * @param pCon
     */
    public static void assertFalseAsync(String pFailMessage, long pTimeout,
            final TestHelper.Condition pCon) {

        assertTrueAsync(pFailMessage, pTimeout, new TestHelper.Condition() {
            public boolean isSatisfied() {

                return !pCon.isSatisfied();
            }
        });
    }

    /**
     * Compare two objects with equals method.
     * 
     * @param pFailMessage
     * @param pTimeout
     *            in Millseconds
     * @param pExpected
     *            the expected object
     * @param pMesurement
     */
    public static void blockUntilEquals(String pFailMessage, long pTimeout, Object pExpected,
            final TestHelper.Mesurement pMesurement) {

        Object mesuredValue = null;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < pTimeout) {

            mesuredValue = pMesurement.getActualValue();
            if (pExpected.equals(mesuredValue)) {
                return;
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        throw new AssertionError(pFailMessage + ": should be <" + pExpected + ">, but was <"
                + mesuredValue + ">");
    }
}
