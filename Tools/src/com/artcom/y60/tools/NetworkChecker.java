package com.artcom.y60.tools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import com.artcom.y60.ErrorHandling;
import com.artcom.y60.HttpHelper;
import com.artcom.y60.Logger;
import com.artcom.y60.gom.GomNode;
import com.artcom.y60.gom.GomProxyHelper;

public class NetworkChecker extends Activity {

    private static final String CONFIG_FILE   = "/sdcard/device_config.json";
    private static final String LOG_TAG       = "NetworkChecker";
    private static final String TEST_RESOURCE = "/test";
    private static final int    REPEAT        = 100;

    private String              m_GomUriString;
    private String              m_SelfPath;
    private TextView            m_View;
    private TableLayout         m_Layout;
    private Button              m_Button;
    private int                 m_CurrentStep;
    private GomNode             m_SelfNode    = null;
    private static final int    NUM_STEPS     = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        m_CurrentStep = 2;

        m_Layout = new TableLayout(this);
        m_Button = new Button(this);
        m_Button.setText("Next Step");
        ContinueListener listener = new ContinueListener();
        m_Button.setOnClickListener(listener);
        m_Layout.addView(m_Button);
        m_View = new TextView(this);
        m_View.setText("Exercizing network\n");
        m_View.refreshDrawableState();
        m_Layout.addView(m_View);
        setContentView(m_Layout);

        m_SelfPath = null;
        m_GomUriString = null;
        JSONObject configuration = null;
        try {

            FileReader fr = new FileReader(CONFIG_FILE);

            char[] inputBuffer = new char[255];
            fr.read(inputBuffer);

            configuration = new JSONObject(new String(inputBuffer));
            m_GomUriString = configuration.getString("gom-url");
            m_SelfPath = configuration.getString("device-path");

        } catch (FileNotFoundException e) {
            Logger.e(LOG_TAG, "Could not find configuration file ", CONFIG_FILE);
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            Logger.e(LOG_TAG, "Configuration file ", CONFIG_FILE, " uses unsupported encoding");
            throw new RuntimeException(e);
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Error while reading configuration file ", CONFIG_FILE);
            throw new RuntimeException(e);
        } catch (JSONException e) {
            Logger.e(LOG_TAG, "Error while parsing configuration file ", CONFIG_FILE);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void executeStep(int step) {

        if (step < 0 || step >= NUM_STEPS) {
            Logger.e(LOG_TAG, "You are trying to execute step ", step,
                    ", which doesn't exist. Resetting to 0.");
            return;
        }

        switch (step) {
            case 0:
                step0();
                break;
            case 1:
                step1();
                break;
            case 2:
                step2();
                break;
            case 3:
                step3();
                break;
            case 4:
                step4();
                break;
            case 5:
                step5();
                break;
        }

        update_step();
    }

    private synchronized void update_step() {
        m_CurrentStep++;
        if (m_CurrentStep < 0 || m_CurrentStep >= NUM_STEPS) {
            m_CurrentStep = 0;
        }
    }

    private synchronized void reset_step() {
        m_CurrentStep = 0;
    }

    private void step0() {

        for (int i = 0; i < REPEAT; i++) {
            // Do a GET on GOM top-level resource using the HTTPHelper

            say("[" + i + "]" + ">> HTTPHelper.get(" + m_GomUriString + ")");
            HttpHelper.get(m_GomUriString);
            say("[" + i + "]" + "<< HTTPHelper.get(" + m_GomUriString + ")");
            rest(500);
        }
    }

    private void step1() {
        for (int i = 0; i < REPEAT; i++) {
            // Do a GET on an XML GOM resource using the HTTPHelper

            say("[" + i + "]" + ">> HTTPHelper.get(" + m_GomUriString + TEST_RESOURCE + ".xml)");
            HttpHelper.get(m_GomUriString + TEST_RESOURCE + ".xml");
            say("[" + i + "]" + "<< HTTPHelper.get(" + m_GomUriString + TEST_RESOURCE + ".xml)");
            rest(500);
        }
    }

    private void step2() {
        for (int i = 0; i < REPEAT; i++) {
            // Do a GET on a JSON resource using the HTTPHelper

            say("[" + i + "]" + ">> HTTPHelper.get(" + m_GomUriString + TEST_RESOURCE + ".json)");
            HttpHelper.get(m_GomUriString + TEST_RESOURCE + ".json");
            say("[" + i + "]" + "<< HTTPHelper.get(" + m_GomUriString + TEST_RESOURCE + ".json)");
            rest(500);
        }
    }

    private void step3() {
        for (int i = 0; i < REPEAT; i++) {
            // Use the GomRepository interface to retrieve the 'self' node

            say("[" + i + "]" + ">> Retrieving 'self' node from GOM");
            GomProxyHelper rep = new GomProxyHelper(this, null);
            try {
                m_SelfNode = rep.getNode(m_SelfPath);
            } catch (Exception gx) {
                ErrorHandling.signalGomError(LOG_TAG, gx, this);
                return;
            }
            say("[" + i + "]" + "<< Retrieving 'self' node from GOM");
            rest(500);
        }
    }

    private void step4() {
        for (int i = 0; i < REPEAT; i++) {
            // Use the GomRepository interface to retrieve an attribute

            String test_attribute = "http://t-gom.service.t-gallery.act/test/android/y60/infrastructure/gom_attribute_test:attribute";

            say("[" + i + "]" + ">> Retrieving attribute " + test_attribute);
            GomProxyHelper rep = new GomProxyHelper(this, null);
            try {
                rep.getAttribute(test_attribute);
            } catch (Exception gx) {
                ErrorHandling.signalGomError(LOG_TAG, gx, this);
                return;
            }
            say("[" + i + "]" + "<< Retrieving attribute " + test_attribute);
            rest(500);
        }
    }

    private void step5() {
        for (int i = 0; i < REPEAT; i++) {
            // Fetch some resource pointer from the GOM to a file

            String uri_string = "http://storage.service.t-gallery.act/movies/dragracer-blows-up.3gp";
            String filename = "/sdcard/tmp/movies.3gp";
            say("[" + i + "]" + ">> Fetching " + uri_string + " to local file " + filename);
            HttpHelper.fetchUriToFile(uri_string, filename);
            say("[" + i + "]" + "<< Fetching " + uri_string + " to local file " + filename);
            rest(500);
            // TODO Post something to the GOM
        }

    }

    private void say(String msg) {
        m_View.append(msg + "\n");
        m_View.refreshDrawableState();
        Logger.d(LOG_TAG, msg);
    }

    private void rest(int ms) {
        say("Sleeping for " + ms + " ms");
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    class ContinueListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            executeStep(m_CurrentStep);
        }

    }
}