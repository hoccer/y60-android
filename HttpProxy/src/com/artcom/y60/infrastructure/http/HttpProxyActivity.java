package com.artcom.y60.infrastructure.http;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity for starting a HttpProxyService.
 * 
 * @author arne
 * @see HttpProxyService
 */
public class HttpProxyActivity extends Activity {
    
    // Public Instance Methods -------------------------------------------

    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        // Watch for button clicks.
        Button startButton = (Button)findViewById(R.id.start);
        startButton.setOnClickListener(
                new OnClickListener() {
                    
                    public void onClick(View v) {
                        
                        Intent intent = new Intent(HttpProxyActivity.this, HttpProxyService.class);
                        startService(intent);
                    }
                }
        );

        Button stopButton = (Button)findViewById(R.id.stop);
        stopButton.setOnClickListener(
                new OnClickListener() {
                    
                    public void onClick(View v) {
                        
                        stopService(new Intent(HttpProxyActivity.this, HttpProxyService.class));
                    }
                }
        );
    }
    
    
    
    // Protected Instance Methods ----------------------------------------

    protected void onResume() {
        
        super.onResume();
    }
}