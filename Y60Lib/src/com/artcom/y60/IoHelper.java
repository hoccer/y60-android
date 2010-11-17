package com.artcom.y60;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.os.Bundle;

import com.artcom.y60.http.HttpProxyConstants;

public class IoHelper {
    private static final String LOG_TAG = "IoHelper";

    public static String encodeUrl(String pString) {
        int separate = pString.lastIndexOf("/");
        String lastPart = pString.substring(separate + 1);
        try {
            lastPart = URLEncoder.encode(lastPart, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        lastPart = lastPart.replaceAll("\\+", "%20");
        return pString.substring(0, separate + 1) + lastPart;
    }

    public static String getColonSplittedItem(String pString, int pReturnItem, int pExpectedNumber) {
        String[] tmp = pString.split(",");
        if (tmp.length != pExpectedNumber) {
            return null;
        }
        return tmp[pReturnItem];
    }

    public static String cutString(String pString, int pMaxWidth) {
        pMaxWidth -= 3;
        if (pString.length() > pMaxWidth) {
            return pString.substring(0, pMaxWidth) + "...";
        }
        return pString;
    }

    public static String readStringFromStream(InputStream pInStream) throws IOException {

        InputStreamReader isr = new InputStreamReader(pInStream);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            builder.append(line + "\n");
        }

        br.close();
        return builder.toString();
    }

    public static byte[] readByteArrayFromStream(InputStream pInStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int in;
        while ((in = pInStream.read()) != -1) {
            baos.write(in);
        }
        return baos.toByteArray();
    }

    public static void writeInputStreamToFile(InputStream pInputStream, String pFilename)
            throws IOException {
        File tmpFile = new File(pFilename);
        FileOutputStream fos = new FileOutputStream(tmpFile);

        byte[] buffer = new byte[0xFFFF];
        for (int len; (len = pInputStream.read(buffer)) != -1;) {
            fos.write(buffer, 0, len);
        }
        fos.close();
    }

    public static void writeDataToStream(byte[] originalData, OutputStream outStream)
            throws IOException {
        InputStream is = new ByteArrayInputStream(originalData);
        int downloaded = 0;
        byte[] buffer = new byte[0xF];
        int len;
        while ((len = is.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
            downloaded += len;
        }
    }

    public static void writeStringToFile(String string, String filename) throws IOException {
        File tmpFile = new File(filename);
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(string.getBytes());
        fos.close();
    }

    public static void deleteDir(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String element : children) {
                deleteDir(new File(file, element));
            }
        }
        file.delete();
    }

    public static String getValue(JSONObject jo, String name) throws JSONException {
        if (jo.has(name)) {
            return jo.getString(name);
        }
        return "";
    }

    public static boolean areWeEqual(byte[] pArray1, byte[] pArray2) {
        if (pArray1 == null && pArray2 == null) {
            return true;
        }
        if (pArray1 == null || pArray2 == null) {
            return false;
        }
        if (pArray1.length != pArray2.length) {
            return false;
        }

        for (int i = 0; i < pArray1.length; ++i) {
            if (pArray1[i] != pArray2[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] convertResourceBundleToByteArray(Bundle resourceDescription) {

        if (resourceDescription == null) {
            return null;
        }

        String resourcePath = resourceDescription
                .getString(HttpProxyConstants.LOCAL_RESOURCE_PATH_TAG);
        if (resourcePath == null) {
            return resourceDescription.getByteArray(HttpProxyConstants.BYTE_ARRAY_TAG);
        }

        return getByteArrayFromFile(resourcePath);
    }

    public static byte[] getByteArrayFromFile(String filepath) {
        byte[] buffer;
        try {
            File file = new File(filepath);
            FileInputStream stream = new FileInputStream(file);
            if (file.length() > Integer.MAX_VALUE) {
                throw new RuntimeException("file '" + file + "' is to big");
            }
            buffer = new byte[(int) file.length()];
            stream.read(buffer);
        } catch (IOException e) {
            Logger.e(ResourceDownloadHelper.LOG_TAG, "io error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return buffer;
    }

    public static boolean areGivenServicesFinished(String[] pServices,
            List<RunningServiceInfo> runningServices) {

        for (String service : pServices) {
            if (isThisParticularServiceInRunningServiceList(service, runningServices)) {
                return false;
            }
        }
        return true;
    }

    public static boolean areGivenServicesFinished(List<String> pServices,
            List<RunningServiceInfo> runningServices) {

        for (String service : pServices) {
            if (isThisParticularServiceInRunningServiceList(service, runningServices)) {
                return false;
            }
        }
        return true;
    }

    public static LinkedList<String> getRunningServicesFromList(String[] pServices,
            List<RunningServiceInfo> runningServices) {

        LinkedList<String> stillRunning = new LinkedList<String>();

        for (String service : pServices) {
            if (isThisParticularServiceInRunningServiceList(service, runningServices)) {
                stillRunning.add(service);
            }
        }
        return stillRunning;
    }

    public static LinkedList<String> getRunningServicesFromList(List<String> pServices,
            List<RunningServiceInfo> runningServices) {

        LinkedList<String> stillRunning = new LinkedList<String>();

        for (String service : pServices) {
            if (isThisParticularServiceInRunningServiceList(service, runningServices)) {
                stillRunning.add(service);
            }
        }
        return stillRunning;
    }

    public static boolean isThisParticularServiceInRunningServiceList(String service,
            List<RunningServiceInfo> runningServices) {
        for (RunningServiceInfo runningService : runningServices) {
            if (runningService.service.getClassName().equals(service)) {
                return true;
            }
        }
        return false;
    }

    public static boolean areAllMonitoredServicesInRunningServices(String[] pMonitoredServices,
            List<RunningServiceInfo> runningServices) {

        for (String service : pMonitoredServices) {
            if (!isThisParticularServiceInRunningServiceList(service, runningServices)) {
                return false;
            }
        }
        return true;
    }

    public static List<String> getAllServicesFromListThatAreNotRunning(String[] pMonitoredServices,
            List<RunningServiceInfo> runningServices) {

        List<String> servicesNotRunning = new ArrayList<String>();

        for (String service : pMonitoredServices) {
            if (!isThisParticularServiceInRunningServiceList(service, runningServices)) {
                servicesNotRunning.add(service);
            }
        }
        return servicesNotRunning;
    }

    public static List<RunningServiceInfo> getRunningServices(Context pContext) {
        ActivityManager am = (ActivityManager) pContext.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningServices(100);
    }

    public static void writeByteArrayToFile(byte[] arrayTmp, String filepath) throws IOException {

        FileOutputStream fos = new FileOutputStream(filepath);
        fos.write(arrayTmp);
        fos.close();

    }

}
