package com.artcom.y60.trackpad;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TrackPadLauncher extends Activity {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "Trackpad";
    protected static final String PREF_KEY_REMOTE_ADDRESS = "REMOTE_ADDRESS";
    private static final String PREF_KEY_REMOTE_PORT = "REMOTE_PORT";
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String addrPref = prefs.getString(PREF_KEY_REMOTE_ADDRESS, "10.0.2.2");
        String portPref = prefs.getString(PREF_KEY_REMOTE_PORT, "1999");
        EditText addrInput = (EditText)findViewById(R.id.address_input);
        addrInput.setText(addrPref);
        EditText portInput = (EditText)findViewById(R.id.port_input);
        portInput.setText(portPref);
        
        Button trackpadButton = (Button)findViewById(R.id.tp_launch_button);
        trackpadButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View pArg0) {
                
                EditText add_input = (EditText)findViewById(R.id.address_input);
                String address = add_input.getText().toString();
                EditText port_input = (EditText)findViewById(R.id.port_input);
                String port = port_input.getText().toString();
                
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TrackPadLauncher.this);
                Editor editor = prefs.edit();
                editor.putString(TrackPadLauncher.PREF_KEY_REMOTE_ADDRESS, address);
                editor.putString(TrackPadLauncher.PREF_KEY_REMOTE_PORT, port);
                editor.commit();
                
                Intent intent = new Intent(TrackPadLauncher.this, TrackPad.class);
                intent.putExtra(TrackPad.ADDRESS_EXTRA, address);
                intent.putExtra(TrackPad.PORT_EXTRA, port);

                startActivity(intent);
            }
            
        });
        
        Button tiltControlButton = (Button)findViewById(R.id.tilt_launch_button);
        tiltControlButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View pArg0) {
                
                EditText add_input = (EditText)findViewById(R.id.address_input);
                String address = add_input.getText().toString();
                EditText port_input = (EditText)findViewById(R.id.port_input);
                String port = port_input.getText().toString();
                
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(TrackPadLauncher.this);
                Editor editor = prefs.edit();
                editor.putString(TrackPadLauncher.PREF_KEY_REMOTE_ADDRESS, address);
                editor.putString(TrackPadLauncher.PREF_KEY_REMOTE_PORT, port);
                editor.commit();
                
                Intent intent = new Intent(TrackPadLauncher.this, TiltController.class);
                intent.putExtra(TrackPad.ADDRESS_EXTRA, address);
                intent.putExtra(TrackPad.PORT_EXTRA, port);

                startActivity(intent);
            }
            
        });
    }
    
}
