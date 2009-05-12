package com.artcom.y60;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IoHelper {

    public static String readStringFromStream(InputStream pInStream) throws IOException {
        
        InputStreamReader isr     = new InputStreamReader(pInStream);
        BufferedReader    br      = new BufferedReader(isr);
        StringBuilder     builder = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            builder.append(line + "\n");
        }

        br.close();
        return builder.toString();
    }
}
