package com.artcom.y60;

public class StringHelper {
    
    public static String trimWithDots(String pText, int maxLength) {
        
        if (pText.length() + 3 > maxLength) {
            pText = pText.substring(0, maxLength - 3) + "...";
        }
        
        return pText;
    }
    
}
