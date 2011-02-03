package com.artcom.y60.dc;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

import com.artcom.y60.Logger;

public class CommandBufferUnitTests extends TestCase {


    public static String LOG_TAG = "CommandBufferUnitTests";

    public static String    oneLineExampleString = "abcdefghijklmnopqrstuvwxyz";

    public static String    timeStampStringA = "09-21 11:54:22.184";
    public static String    timeStampStringB = "02-03 09:36:00.069";

    public static String    multiLineString;

    public void testClearBuffer() {
        StringBuffer buffer = new StringBuffer(oneLineExampleString);
        CommandBuffer.clearBuffer(buffer);
        assertTrue("String buffer should be empty", buffer.toString().equals(""));
    }

    public void testGetTimeStamp() {
        String multiLineString;
        String foundTimeStamp;

        multiLineString = new String();
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringB + " " + oneLineExampleString + "\n";

        foundTimeStamp = CommandBuffer.getLastTimeStampFromString(multiLineString);
        assertNotNull("there should be a found timestamp", foundTimeStamp);
        assertEquals("time stamp was not as excpected",timeStampStringB,foundTimeStamp);

        multiLineString = new String();
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringB + " " + oneLineExampleString + "\n";
        multiLineString += "aaaa\n";
        multiLineString += "bbbb\n";

        foundTimeStamp = CommandBuffer.getLastTimeStampFromString(multiLineString);
        assertNull("there should be no found timestamp", foundTimeStamp);

        multiLineString = new String();
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringA + " " + oneLineExampleString + "\n";
        multiLineString += timeStampStringB + " " + oneLineExampleString + "\n";
        multiLineString += oneLineExampleString + "\n";

        foundTimeStamp = CommandBuffer.getLastTimeStampFromString(multiLineString);
        assertNull("there should be no found timestamp", foundTimeStamp);
    }

    public void testClearBufferUntilTimeStamp() {

        StringBuffer multiLineBuffer;
        String linesToBeDeleted;
        String linesToBeLeft;

        linesToBeDeleted = new String();
        linesToBeDeleted += timeStampStringA + " " + oneLineExampleString + "\n";
        linesToBeDeleted += timeStampStringA + " " + oneLineExampleString + "\n";
        linesToBeDeleted += timeStampStringA + " " + oneLineExampleString + "\n";
        linesToBeDeleted += timeStampStringB + " " + oneLineExampleString + "\n";
        
        linesToBeLeft = new String();
        linesToBeLeft += timeStampStringA + " " + oneLineExampleString + "\n";
        linesToBeLeft += timeStampStringA + " " + oneLineExampleString + "\n";
        linesToBeLeft += timeStampStringA + " " + oneLineExampleString + "\n";

        multiLineBuffer = new StringBuffer();
        multiLineBuffer.append(linesToBeDeleted);
        multiLineBuffer.append(linesToBeLeft);
        assertTrue("time stamp should be found",CommandBuffer.clearBufferUntilTimeStamp(
                multiLineBuffer,timeStampStringB) );
        assertEquals("first lines of string should be deleted",
            linesToBeLeft, multiLineBuffer.toString() );

        multiLineBuffer = new StringBuffer();
        multiLineBuffer.append(linesToBeLeft);
        assertFalse("time stamp should be not found",CommandBuffer.clearBufferUntilTimeStamp(
                multiLineBuffer,timeStampStringB) );
        assertEquals("string buffer should be unchanged",
            linesToBeLeft, multiLineBuffer.toString() );

        multiLineBuffer = new StringBuffer();
        multiLineBuffer.append(linesToBeDeleted);
        assertTrue("time stamp should be found",CommandBuffer.clearBufferUntilTimeStamp(
                multiLineBuffer,timeStampStringB) );
        assertEquals("string buffer should be empty",
            "", multiLineBuffer.toString() );
    }


    public void testGetFromBufferEnd() throws IOException, FileNotFoundException{

        BufferedReader  buffer;
        String          bufferString;
        long            bufferSize;
        int             bytesToRead;
        String          readFromBuffer;

        bufferString = new String();
        bufferString += "1" + oneLineExampleString + "\n";
        bufferString += "2" + oneLineExampleString + "\n";
        bufferString += "3" + oneLineExampleString + "\n";
        bufferString += "4" + oneLineExampleString + "\n";

        bufferSize = bufferString.length();
        buffer = new BufferedReader(new StringReader(bufferString));
        bytesToRead = oneLineExampleString.length() + 2;
        readFromBuffer = CommandBuffer.getFromEndOfBufferedReader(bytesToRead, buffer, bufferSize);
        assertEquals("the result string should be the last line",
            "4"+oneLineExampleString+"\n", readFromBuffer);

        bufferSize = bufferString.length();
        buffer = new BufferedReader(new StringReader(bufferString));
        bytesToRead = oneLineExampleString.length() + 2 + (int)(oneLineExampleString.length() / 2);
        readFromBuffer = CommandBuffer.getFromEndOfBufferedReader(bytesToRead, buffer, bufferSize);
        assertEquals("the result string should be the last line",
            "4"+oneLineExampleString+"\n", readFromBuffer);

        bufferSize = bufferString.length();
        buffer = new BufferedReader(new StringReader(bufferString));
        bytesToRead = (int)(bufferSize * 2);
        readFromBuffer = CommandBuffer.getFromEndOfBufferedReader(bytesToRead, buffer, bufferSize);
        assertEquals("the result string should be the whole string",
            bufferString, readFromBuffer);

    }

}
