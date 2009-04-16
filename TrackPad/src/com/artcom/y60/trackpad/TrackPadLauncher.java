package com.artcom.y60.trackpad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TrackPadLauncher extends Activity {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "Trackpad";
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        
        Button butt = (Button)findViewById(R.id.launch_button);
        butt.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View pArg0) {
                
                EditText add_input = (EditText)findViewById(R.id.address_input);
                String address = add_input.getText().toString();
                EditText port_input = (EditText)findViewById(R.id.port_input);
                String port = port_input.getText().toString();
                
                Intent intent = new Intent(TrackPadLauncher.this, TrackPad.class);
                intent.putExtra(TrackPad.ADDRESS_EXTRA, address);
                intent.putExtra(TrackPad.PORT_EXTRA, port);

                startActivity(intent);
            }
            
        });
    }
    
}
