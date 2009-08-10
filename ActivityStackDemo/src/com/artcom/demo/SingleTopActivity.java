package com.artcom.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;

public class SingleTopActivity extends ActivityStackDemo {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        mTextView
                .setText("SingleTop Activity "
                        + df.format(date)
                        + "\n\n"
                        + "creates a new instance if the activity is not already on top of the current stack\n\n ");
    }
}
