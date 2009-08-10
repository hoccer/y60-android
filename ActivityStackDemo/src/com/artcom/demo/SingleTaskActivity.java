package com.artcom.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;

public class SingleTaskActivity extends ActivityStackDemo {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        mTextView
                .setText("SingleTask Activity"
                        + df.format(date)
                        + "\n\n"
                        + "Only one instance of this activity exits. It is always the root activity of the task.\n\n ");
    }
}
