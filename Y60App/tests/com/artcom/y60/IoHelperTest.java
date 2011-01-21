package com.artcom.y60;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import com.artcom.y60.http.HttpHelper;

import android.test.suitebuilder.annotation.Suppress;

public class IoHelperTest extends TestCase {

    private static final String LOG_TAG = "IoHelperTest";
    private static final String FILE_1  = "/sdcard/test_file_"
                                                + String.valueOf(System.currentTimeMillis());
    private static final String FILE_2  = "/sdcard/test_file_"
                                                + String.valueOf(System.currentTimeMillis());
    private static final String RES_URI = "http://www.artcom.de/templates/artcom/css/images/artcom_rgb_screen_193x22.png";

    public void testWriteWebByteArrayToFile() throws Exception {

        HttpHelper.fetchUriToFile(RES_URI, FILE_1);
        byte[] file1Bytes = IoHelper.getByteArrayFromFile(FILE_1);

        byte[] file2BytesTmp = HttpHelper.getAsByteArray(RES_URI);
        IoHelper.writeByteArrayToFile(file2BytesTmp, FILE_2);
        byte[] file2Bytes = IoHelper.getByteArrayFromFile(FILE_2);

        assertTrue(
                "directly written file and downloaded as byte and than stored file should be equal",
                IoHelper.areWeEqual(file1Bytes, file2Bytes));
    }

    // test for setting homescreen programatically, doesnt work yet
    @Suppress
    public void testSimpleAdbListPackagesCommand() throws Exception {

        top();

        String packages = executeAdbListPackages();
        assertNotNull("Packages shouldnt be null", packages);
        assertTrue("Packages should contain my package", packages.contains("com.artcom.y60"));
    }

    public static void top() throws IOException {
        Process process;
        process = Runtime.getRuntime().exec("top -n 1 -d 1");
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Logger.v(LOG_TAG, "top: ", in.toString());
    }

    public static String executeAdbListPackages() throws IOException, InterruptedException {

        Logger.v(LOG_TAG, "executeAdbListPackages");

        Process process = Runtime.getRuntime().exec("pm disable com.android.launcher\n");
        DataOutputStream os = new DataOutputStream(process.getOutputStream());
        // DataInputStream is = new DataInputStream(process.getInputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Logger.v(LOG_TAG, "readline: ", in.readLine());
        // for (String single : commands) {
        // os.writeBytes(single + "\n");
        // os.flush();
        // res.add(osRes.readLine());
        // }

        // os.writeBytes("pm list packages\n");
        // os.flush();
        // String result = IoHelper.readStringFromStream(osRes);
        // Logger.v(LOG_TAG, "result: ", result);

        os.writeBytes("exit\n");
        os.flush();
        process.waitFor();

        return null;
    }
}
