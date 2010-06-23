package com.artcom.y60;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.artcom.y60.http.HttpProxyConstants;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.os.Bundle;

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

    public static boolean isWantedServiceClassNameOnSdcard(String pWantedService)
            throws FileNotFoundException {
        String[] sdcardFiles = getAliveServicesFromSdcard();
        for (String filename : sdcardFiles) {
            if (filename.equals(pWantedService)) {
                return true;
            }
        }
        return false;
    }

    public static String[] getAliveServicesFromSdcard() throws FileNotFoundException {
        String aliveServicesDirectory = Constants.Device.ALIVE_SERVICES_PATH;

        File dir = new File(aliveServicesDirectory);
        String[] children = dir.list();
        if (children == null) {
            throw new FileNotFoundException("Either " + aliveServicesDirectory
                    + " does not exist or is not a directory");
        } else {
            return children;
        }
    }

    public static void cleanAllServicesOnSdcard() throws Exception {

        File f = new File(Constants.Device.ALIVE_SERVICES_PATH);
        if (!f.exists()) {
            return;
        }

        String aliveServicesDirectory = Constants.Device.ALIVE_SERVICES_PATH;
        File dir = new File(aliveServicesDirectory);
        String[] children = dir.list();
        if (children == null) {
            throw new Exception("Either " + aliveServicesDirectory
                    + " does not exist or is not a directory");
        } else {
            for (String filename : children) {
                Logger.v(TestHelper.LOG_TAG, "deleting: ", filename);
                new File(aliveServicesDirectory + "/" + filename).delete();
            }
        }

    }

    private void writeMyLifecycleOnSdcard() {

        File f = new File(Constants.Device.ALIVE_SERVICES_PATH);
        if (f.exists() == false) {
            f.mkdirs();
        }

        FileWriter fw;
        try {
            fw = new FileWriter(Constants.Device.ALIVE_SERVICES_PATH + "/" + getClass().getName());
            fw.write("");
            fw.flush();
            fw.close();
            Logger.v(LOG_TAG, "____ Wrote: ", Constants.Device.ALIVE_SERVICES_PATH + "/"
                    + getClass().getName(), " on sdcard");
        } catch (IOException e) {
            // ErrorHandling.signalIOError(LOG_TAG, e, this);
        }
    }

    protected void deleteMyLifecycleFromSdcard() {
        boolean deletedMySelf = false;
        String aliveServicesDirectory = Constants.Device.ALIVE_SERVICES_PATH;

        File dir = new File(aliveServicesDirectory);
        String[] children = dir.list();
        if (children == null) {
            Logger.e(LOG_TAG, "No services at all listed in alive services on sdcard");
        } else {
            for (String filename : children) {
                Logger.v(LOG_TAG, "deleteMyLifecycleFromSdcard: ", filename, ", i am: ", getClass()
                        .getName());

                if (filename.equals(getClass().getName())) {
                    File myClassName = new File(aliveServicesDirectory + "/" + filename);
                    myClassName.delete();
                    Logger.v(LOG_TAG, "deleted: ", filename);
                    deletedMySelf = true;
                }
            }
        }
        if (deletedMySelf) {
            return;
        }
        Logger.e(LOG_TAG, "I am not listed in alive services on sdcard");
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

        byte[] buffer;
        try {
            File file = new File(resourcePath);
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
        ActivityManager am = (ActivityManager) pContext.getSystemService(pContext.ACTIVITY_SERVICE);
        return am.getRunningServices(100);
    }

    public static void writeByteArrayToFile(byte[] arrayTmp, String localResourcePath)
            throws IOException {

        File file = new File(localResourcePath);
        file.createNewFile();
        DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
        stream.write(arrayTmp);
        stream.close();

        // writeInputStreamToFile(bais, localResourcePath);

    }
}
