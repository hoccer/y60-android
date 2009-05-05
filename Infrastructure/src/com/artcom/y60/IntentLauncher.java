package com.artcom.y60;

import android.content.ComponentName;
import android.content.Intent;

public class IntentLauncher extends SlotLauncher {

    // Instance Variables ------------------------------------------------

    private Intent mIntent;
    
    
    
    // Constructors ------------------------------------------------------

    public IntentLauncher(String pActivityClass) {
        
        int    dotPos = pActivityClass.lastIndexOf(".");
        String pkg    = pActivityClass.substring(0, dotPos);
        ComponentName compName = new ComponentName(pkg, pActivityClass);
        mIntent = new Intent();
        mIntent.setComponent(compName);
    }
    
    
    
    // Public Instance Methods -------------------------------------------
    
    @Override
    public void launch() {
        
        getContext().startActivity(mIntent);
    }
    
    public String toString() {
        
        return mIntent.toString();
    }
    
    public String getActivityClass() {
        
        return mIntent.getComponent().getClassName();
    }
}
