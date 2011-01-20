package com.artcom.y60;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.List;

import junit.framework.Assert;

import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.test.AssertionFailedError;

import com.artcom.y60.http.AsyncHttpGet;
import com.artcom.y60.http.HttpClientException;
import com.artcom.y60.http.HttpHelper;

public class TestHelper {

    static final String LOG_TAG = "TestHelper";

    /**
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
            Thread.sleep(20);
        }
        throw new AssertionError(pFailMessage);
    }

    /**
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

    /**
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
        blockUntilBackendResumed(pActivity, 60000);
    }

    public static void blockUntilResourceAvailable(String pFailMessage, final String pUrl)
            throws Exception {
        blockUntilResourceAvailable(pFailMessage, pUrl, 3000);
    }

    public static void blockUntilResourceAvailable(String pFailMessage, final String pUrl,
            int pTimeout) throws Exception {
        blockUntilTrue(pFailMessage, pTimeout, new TestHelper.Condition() {
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

    public static void assertDeviceControllerIsRunning(Context pContext) throws Exception {
        Intent startIntent = new Intent(Y60Action.SERVICE_DEVICE_CONTROLLER);
        pContext.startService(startIntent);
        TestHelper.blockUntilDeviceControllerIsRunning();
    }

    public static void blockUntilDeviceControllerIsRunning() throws Exception {
        blockUntilDeviceControllerIsRunning(10000);
    }

    public static void blockUntilDeviceControllerIsRunning(long pTimeout) throws Exception {
        TestHelper.blockUntilEquals("device controller should have started within " + pTimeout
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
        notification.putExtra(IntentExtraKeys.NOTIFICATION_PATH, pPath);
        notification.putExtra(IntentExtraKeys.NOTIFICATION_OPERATION, pOperation);
        notification.putExtra(IntentExtraKeys.NOTIFICATION_DATA_STRING, pData);
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

    public interface Condition {
        public boolean isSatisfied() throws Exception;
    }

    /**
     * Mesures current state of an object.
     */
    public interface Measurement {
        public Object getActualValue() throws Exception;
    }

    /**
     * Assert two {@linkplain File files} to have equal content.
     * 
     * @param message
     *            the error message
     * @param expected
     *            reference file
     * @param current
     *            file to compare
     * @author Apache Project (package org.apache.commons.id.test)
     * @license Apache Lichense 2.0
     */
    public static void assertFileEquals(final String message, final File expected,
            final File current) {
        try {
            assertInputStreamEquals(new BufferedInputStream(new FileInputStream(expected)),
                    new BufferedInputStream(new FileInputStream(current)));
        } catch (final FileNotFoundException e) {
            Assert.fail((message != null ? message + ": " : "") + e.getMessage());
        }
    }

    private static void assertInputStreamEquals(final InputStream expected,
            final InputStream current) {
        assertInputStreamEquals(null, expected, current);
    }

    /**
     * Assert two {@linkplain InputStream input streams} to deliver equal content.
     * 
     * @param message
     *            the error message
     * @param expected
     *            reference input
     * @param current
     *            input to compare
     * @since 1.0
     * @author Apache Project (package org.apache.commons.id.test)
     * @license Apache Lichense 2.0
     */
    public static void assertInputStreamEquals(final String message, final InputStream expected,
            final InputStream current) {
        long counter = 0;
        int eByte, cByte;
        try {
            for (; (eByte = expected.read()) != -1; ++counter) {
                cByte = current.read();
                if (eByte != cByte) {
                    Assert.assertEquals((message != null ? message + ": " : "")
                            + "Stream not equal at position " + counter, eByte, cByte);
                }
            }
        } catch (final IOException e) {
            Assert.fail((message != null ? message + ": " : "") + e.getMessage());
        }
    }

    public static void assertGreater(String message, double minimum, double measured) {
        if (minimum > measured) {
            Assert.fail(message + " but " + minimum + " is greater than " + measured);
        }
    }

    public static void assertGreater(String message, int minimum, int measured) {
        if (minimum > measured) {
            Assert.fail(message + " but " + minimum + " is greater than " + measured);
        }
    }

    public static void assertSmaller(String message, double maximum, double measured) {
        if (maximum < measured) {
            Assert.fail(message + " but " + maximum + " is smaller than " + measured);
        }
    }

    public static void assertSmaller(String message, int maximum, int measured) {
        if (maximum < measured) {
            Assert.fail(message + " but " + maximum + " is smaller than " + measured);
        }
    }

    public static void assertIncludes(String message, String substring, String measured) {
        if (!measured.contains(substring)) {
            Assert.fail(message + " but '" + measured + "' does not contain '" + substring + "'");
        }
    }

    public static void assertMatches(String message, String regexp, String measured) {
        if (!measured.matches(regexp)) {
            Assert.fail(message + " but '" + regexp + "' does not match '" + measured + "'");
        }
    }

    public static void assertEquals(final String message, final byte[] expected,
            final byte[] current) {

        int eByte, cByte;
        int i = 0;
        try {
            for (; i < expected.length; ++i) {
                eByte = expected[i];
                cByte = current[i];

                if (eByte != cByte) {
                    Assert.assertEquals((message != null ? message + ": " : "")
                            + "Byte Array not equal at position " + i, eByte, cByte);
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            Assert.fail((message != null ? message + ": " : "")
                    + "Byte Array truncated at position  " + i);
        }

        Assert.assertEquals("byte array to long", expected.length, current.length);

    }

    public static File[] getFiles(String dirName) throws Exception {
        return getFiles(dirName, "");
    }

    public static File[] getFiles(String dirName, final String pFileEnding) throws Exception {
        File dataDir = new File(dirName);
        Assert.assertTrue("dir should exists", dataDir.exists());
        Assert.assertTrue(dirName + " should be a directory", dataDir.isDirectory());
        Assert.assertTrue("dir should be readable", dataDir.canRead());

        FileFilter filter = new FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(pFileEnding);
            }
        };

        return dataDir.listFiles(filter);
    }

    public static void blockUntilActivityIsFinishing(final Activity pActivity, long pTimeout)
            throws Exception {
        blockUntilTrue("Activity should be finished by now", pTimeout, new Condition() {
            @Override
            public boolean isSatisfied() throws Exception {
                return pActivity.isFinishing();
            }
        });

    }

    public static void assertRequestIsDone(final AsyncHttpGet request) throws Exception {
        TestHelper.blockUntilTrue("should finish", 3000, new TestHelper.Condition() {

            @Override
            public boolean isSatisfied() throws Exception {
                return request.isTaskCompleted();
            }
        });
    }

    public static void logServices(String pLOG_TAG, List<RunningServiceInfo> runningServices) {
        logServices(pLOG_TAG, "", runningServices);
    }

    public static void logServices(String pLOG_TAG, String additionalTag,
            List<RunningServiceInfo> runningServices) {
        for (RunningServiceInfo runningService : runningServices) {
            Logger.v(pLOG_TAG, additionalTag, " ", runningService.service.getClassName());
        }
    }
}
