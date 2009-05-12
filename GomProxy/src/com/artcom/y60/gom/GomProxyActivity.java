package com.artcom.y60.gom;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class GomProxyActivity extends Activity {

    // Instance Variables ------------------------------------------------

    private TextView mText;

    // Public Instance Methods -------------------------------------------

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mText = (TextView) findViewById(R.id.text);
    }

    protected void onResume() {

        super.onResume();

//        mText.setText("Binding to GomProxyService...");
//
//        new GomProxyHelper(this, new BindingListener<GomProxyHelper>() {
//
//            public void bound(GomProxyHelper helper) {
//
//                runOnUiThread(new Runnable() {
//
//                    public void run() {
//
//                        mText.setText("Binding to GomProxyService...success.");
//                    }
//                });
//            }
//
//            public void unbound(GomProxyHelper helper) {
//            }
//        });
    }

}