package com.artcom.y60;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.test.AssertionFailedError;

import com.artcom.y60.http.HttpClientException;

public class TestHelper {

    // Constants ---------------------------------------------------------

    private static final String LOG_TAG = "TestHelper";

    // Static Methods ----------------------------------------------------

    /**
     * 
     * @param pFailMessage
     * @param pTimeout
     *            in milliseconds
     * @param pCon
     */
    public static void blockUntilTrue(String pFailMessage, long pTimeout, TestHelper.Condition pCon)
            throws Exception {

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
            final TestHelper.Condition pCon) throws Exception {

        blockUntilTrue(pFailMessage, pTimeout, new TestHelper.Condition() {
            public boolean isSatisfied() throws Exception {

                return !pCon.isSatisfied();
            }
        });
    }

    public static String createJsonFromAttr(String pParentNode, String pName, String pValue) {

        return "{ \"attribute\": {\"name\": \"" + pName + "\"," + "\"node\": \"" + pParentNode
                + "\",\"value\": \"" + pValue + "\",\"type\": \"string\","
                + "\"mtime\": \"2009-07-06T16:01:24+02:00\","
                + "\"ctime\": \"2009-07-06T16:01:24+02:00\"} }";
    }

    public static String createFakeJsonFromAttr(String pParentNode, String pName, String pValue) {

        return "{ \"attribute\": {\"name\": \"" + pName + "\"," + "\"node\": \"" + pParentNode
                + "\"," + "\"value\": \"" + pValue + "\"} }";
    }

    public static String createFake2JsonFromAttr(String pParentNode, String pName, String pValue) {

        return "{ \"attribute\": {\"value\": \"" + pValue + "\"," + "\"node\": \"" + pParentNode
                + "\"," + "\"name\": \"" + pName + "\"}}";
    }

    public static boolean assertJsonEquality(JSONObject pA, JSONObject pB) {

        return false;

    }

    /**
     * 
     * @param pFailMessage
     * @param pTimeout
     * @param pMeasurement
     */
    public static void blockUntilNull(String pFailMessage, long pTimeout,
            final TestHelper.Measurement pMeasurement) throws Exception {

        blockUntilEquals(pFailMessage, pTimeout, null, pMeasurement);
    }

    public static void blockUntilNotNull(String pFailMessage, long pTimeout,
            final TestHelper.Measurement pMeasurement) throws Exception {

        blockUntilTrue(pFailMessage, pTimeout, new Condition() {

            @Override
            public boolean isSatisfied() throws Exception {

                return pMeasurement.getActualValue() != null;
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
            final TestHelper.Measurement pMesurement) throws Exception {

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

        throw new AssertionFailedError(pFailMessage + ": should be <" + pExpected + ">, but was <"
                + mesuredValue + ">");
    }

    public static void blockUntilBackendAvailable(final Y60Activity pActivity) throws Exception {
        blockUntilTrue("Backend is not available", 10000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() {
                return pActivity.hasBackendAvailableBeenCalled();
            }

        });

    }

    public static void blockUntilBackendResumed(final Y60Activity pActivity, int pTimeout)
            throws Exception {
        blockUntilBackendAvailable(pActivity);

        blockUntilTrue("ResumeWithBackend should have been called", pTimeout,
                new TestHelper.Condition() {

                    @Override
                    public boolean isSatisfied() {
                        return pActivity.hasResumeWithBackendBeenCalled();
                    }

                });

    }

    public static void blockUntilBackendResumed(final Y60Activity pActivity) throws Exception {
        blockUntilBackendResumed(pActivity, 10000);
    }

    public static void blockUntilResourceAvailable(String pFailMessage, final String pUrl)
            throws Exception {

        blockUntilTrue(pFailMessage, 3000, new TestHelper.Condition() {
            @Override
            public boolean isSatisfied() {
                try {
                    return HttpHelper.getAsString(pUrl) != null;
                } catch (HttpClientException ex) {

                    if (ex.getStatusCode() == 404) {

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return false;

                    } else {

                        throw new RuntimeException(ex);
                    }
                } catch (Exception ex) {

                    throw new RuntimeException(ex);
                }
            }
        });

    }

    public static void assertDeviceControlerIsRunning(Context pContext) throws Exception {

        Intent startIntent = new Intent("y60.intent.SERVICE_DEVICE_CONTROLLER");
        pContext.startService(startIntent);
        TestHelper.blockUntilDeviceControllerIsRunning();
    }

    public static void blockUntilDeviceControllerIsRunning() throws Exception {
        blockUntilDeviceControllerIsRunning(5000);
    }

    public static void blockUntilDeviceControllerIsRunning(long pTimeout) throws Exception {

        TestHelper.blockUntilEquals("device controller should have started withhin " + pTimeout
                + " milliseconds", pTimeout, "404", new TestHelper.Measurement() {
            @Override
            public Object getActualValue() {

                String statusCode;
                try {
                    statusCode = String.valueOf(HttpHelper.getStatusCode("http://localhost:4042/"));
                } catch (HttpHostConnectException e) {
                    return "HttpHostConnectException";
                } catch (SocketTimeoutException e) {
                    return "SocketTimeoutException";
                } catch (IOException e) {
                    return "SocketTimeoutException";
                }
                return statusCode;
            }
        });

    }

    public static void sendCreateAttributeNotificationBroadcast(String pPath, Context pContext) {

        sendNotificationBroadcast(pPath, generateAttributeDummyJsonString(pPath), "create",
                pContext);
    }

    public static void sendUpdateAttributeNotificationBroadcast(String pPath, Context pContext) {

        sendNotificationBroadcast(pPath, generateAttributeDummyJsonString(pPath), "update",
                pContext);
    }

    public static void sendDeleteAttributeNotificationBroadcast(String pPath, Context pContext) {

        sendNotificationBroadcast(pPath, generateAttributeDummyJsonString(pPath), "delete",
                pContext);
    }

    public static void sendCreateNodeNotificationBroadcast(String pPath, Context pContext) {

        sendNotificationBroadcast(pPath, generateNodeDummyJsonString(pPath), "create", pContext);
    }

    public static void sendUpdateNodeNotificationBroadcast(String pPath, Context pContext) {

        sendNotificationBroadcast(pPath, generateNodeDummyJsonString(pPath), "update", pContext);
    }

    public static void sendDeleteNodeNotificationBroadcast(String pPath, Context pContext) {

        sendNotificationBroadcast(pPath, generateNodeDummyJsonString(pPath), "delete", pContext);
    }

    public static void sendNotificationBroadcast(String pPath, String pData, String pOperation,
            Context pContext) {

        Intent notification = new Intent();
        notification.setAction(Y60Action.GOM_NOTIFICATION_BC);
        notification.putExtra(IntentExtraKeys.KEY_NOTIFICATION_PATH, pPath);
        notification.putExtra(IntentExtraKeys.KEY_NOTIFICATION_OPERATION, pOperation);
        notification.putExtra(IntentExtraKeys.KEY_NOTIFICATION_DATA_STRING, pData);
        pContext.sendBroadcast(notification);
    }

    public static String generateAttributeDummyJsonString(String pPath) {

        return generateAttributeJsonString(pPath, "dummy data");
    }

    public static String generateAttributeJsonString(String pPath, String pValue) {

        return "{ \"attribute\": { \"name\": \"attribute\", \"node\": \""
                + pPath.substring(0, pPath.lastIndexOf(":")) + "\", \"value\": \"" + pValue
                + "\", \"type\": \"string\" } }";
    }

    public static String generateNodeDummyJsonString(String pPath) {

        return "{ \"node\": { \"uri\" : \"" + pPath + "\", \"entries\" : [] } }";
    }

    // Inner Classes -----------------------------------------------------

    public interface Condition {

        public boolean isSatisfied() throws Exception;
    }

    /**
     * Mesures current state of an object.
     */
    public interface Measurement {

        public Object getActualValue() throws Exception;
    }

}
