package com.artcom.y60;

public class TestHelper {

    public interface Condition {

        public boolean isSatisfied();
    }

    /**
     * Mesures current state of an object.
     */
    public interface Measurement {

        public Object getActualValue();
    }

    private static final String LOG_TAG = "TestHelper";

    /**
     * 
     * @param pFailMessage
     * @param pTimeout
     *            in milliseconds
     * @param pCon
     */
    public static void blockUntilTrue(String pFailMessage, long pTimeout, TestHelper.Condition pCon) {

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
    public static void blockUntilFalse(String pFailMessage, long pTimeout,
            final TestHelper.Condition pCon) {

        blockUntilTrue(pFailMessage, pTimeout, new TestHelper.Condition() {
            public boolean isSatisfied() {

                return !pCon.isSatisfied();
            }
        });
    }

    /**
     * 
     * @param pFailMessage
     * @param pTimeout
     * @param pMeasurement
     */
    public static void blockUntilNull(String pFailMessage, long pTimeout,
            final TestHelper.Measurement pMeasurement) {

        blockUntilEquals(pFailMessage, pTimeout, null, pMeasurement);
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
            final TestHelper.Measurement pMesurement) {

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
                throw new AssertionError(e);
            }
        }

        throw new AssertionError(pFailMessage + ": should be <" + pExpected + ">, but was <"
                + mesuredValue + ">");
    }

    public static void blockUntilBackendAvailable(final Y60Activity pActivity) {
        blockUntilTrue("Backend is not available", 2000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return pActivity.hasBackendAvailableBeenCalled();
            }

        });

    }

    public static void blockUntilResourceAvailable(String pFailMessage, final String pUrl) {

        blockUntilTrue(pFailMessage, 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                try {
                    return HttpHelper.get(pUrl) != null;
                } catch (RuntimeException rex) {

                    if (rex.toString().contains("404")) {

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return false;

                    } else {

                        throw new RuntimeException(rex);
                    }
                }
            }
        });

    }

    public static void blockUntilWebServerIsRunning() {

        long timeout = 3000;
        TestHelper.blockUntilEquals("device controller should have started withhin" + timeout
                + " milliseconds", 3000, 404, new TestHelper.Measurement() {
            @Override
            public Object getActualValue() {

                try {
                    return HttpHelper.getStatusCode("http://localhost:4042/");
                } catch (Exception e) {
                    return e.getMessage();
                }
            }
        });

    }
}