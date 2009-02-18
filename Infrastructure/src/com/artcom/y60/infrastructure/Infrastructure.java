package com.artcom.y60.infrastructure;

import com.artcom.y60.infrastructure.R;

import android.app.Activity;
import android.os.Bundle;

public class Infrastructure extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}