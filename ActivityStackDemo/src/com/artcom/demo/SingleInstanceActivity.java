package com.artcom.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;

public class SingleInstanceActivity extends ActivityStackDemo {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        mTextView.setText("SingleInstance Activity \n\n " + df.format(date));
    }
}
