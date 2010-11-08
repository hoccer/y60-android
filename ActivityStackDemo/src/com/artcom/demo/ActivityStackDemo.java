package com.artcom.demo;

//import java.text.SimpleDateFormat;
//import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityStackDemo extends Activity {

    protected TextView mTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mTextView = (TextView) findViewById(R.id.mytext);
        //Date date = new Date();
        //SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        mTextView.setText("Activity Stack Demo - understanding launch modes.\n\n"
                + "In order to understand the differences between the various launch modes it is "
                + "advised to temporally set this application as HOME application.\n");

        Button standard = ((Button) findViewById(R.id.standard));
        standard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                startActivity(new Intent(ActivityStackDemo.this, StandardActivity.class));
            }
        });

        Button singleTop = ((Button) findViewById(R.id.singletop));
        singleTop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                startActivity(new Intent(ActivityStackDemo.this, SingleTopActivity.class));
            }
        });

        Button singleTask = ((Button) findViewById(R.id.singletask));
        singleTask.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                startActivity(new Intent(ActivityStackDemo.this, SingleTaskActivity.class));
            }
        });

        Button singleInstance = ((Button) findViewById(R.id.singleinstance));
        singleInstance.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View pV) {
                startActivity(new Intent(ActivityStackDemo.this, SingleInstanceActivity.class));
            }
        });

    }

}