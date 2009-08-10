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
        mTextView.setText("SingleInstance Activity " + df.format(date) + "\n\n"
                + "Only one instance of this activity exits. It is always the root activity "
                + "of the task. Activities triggered from this activity start a new task. "
                + "Thus, this activities is always the only one in its task.\n\n ");
    }
}
