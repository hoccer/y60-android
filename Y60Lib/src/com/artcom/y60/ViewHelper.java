package com.artcom.y60;

import android.app.Activity;
import android.view.View;

public class ViewHelper {

    public static View getCurrentView(int pId, Activity pActivity) {
        return pActivity.getWindow().getDecorView().findViewById(pId);
    }
}
