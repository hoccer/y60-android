package com.artcom.y60.synergy;

import java.util.Arrays;
import java.util.Vector;

import junit.framework.TestCase;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.test.AssertionFailedError;

import com.artcom.y60.Logger;

public class SynergyServerTest extends TestCase {

    private static final String     LOG_TAG                 = "SynergyServerTest";

    public void testMessagePreparation() throws Exception {
        Vector<Byte> message;
        String messageText = "abcd";

        int dataLength = 0x1;
        message = SynergyServerHelper.prepareMessageFromString(messageText,dataLength);
        assertEqual("",
            new byte[]{0,0,0, (byte) (messageText.length() + dataLength), 'a','b','c','d'}, message );

        dataLength = 0x100;
        message = SynergyServerHelper.prepareMessageFromString(messageText,dataLength);
        assertEqual("",
            new byte[]{0,0,1, (byte)messageText.length(), 'a','b','c','d'}, message );

        dataLength = 0x10000;
        message = SynergyServerHelper.prepareMessageFromString(messageText,dataLength);
        assertEqual("",
            new byte[]{0,1,0, (byte)messageText.length(), 'a','b','c','d'}, message );

        dataLength = 0x1000000;
        message = SynergyServerHelper.prepareMessageFromString(messageText,dataLength);
        assertEqual("",
            new byte[]{1,0,0, (byte)messageText.length(), 'a','b','c','d'}, message );
    }

    public void testMessageStringSearch() throws Exception {
        assertTrue("",SynergyServerHelper.messageSearchString(
            arrayToByteVector(new byte[]{0,0,0,8, 'a','b','c','d',1,2,3,4}),
            "abcd" ));
        assertTrue("",SynergyServerHelper.messageSearchString(
            arrayToByteVector(new byte[]{0,0,0,8, 'a','b','c','d',1,2,3,4}),
            "abc" ));
        assertFalse("",SynergyServerHelper.messageSearchString(
            arrayToByteVector(new byte[]{0,0,0,8, 'a','b','c','d',1,2,3,4}),
            "abcde" ));

        assertFalse("",SynergyServerHelper.messageSearchString(
            arrayToByteVector(new byte[]{0,0,0,0, 'a'}),
            "abcd" ));
        assertFalse("",SynergyServerHelper.messageSearchString(
            arrayToByteVector(new byte[]{0,0}),
            "abcd" ));
    }

    public void testMessageExtractDataLength() throws Exception {
        assertEquals("",(byte)0xff,SynergyServerHelper.extractMessageDataLength(
            arrayToByteVector(new byte[]{0,0,(byte)0x00,(byte)0xff}),0));
        assertEquals("",258,SynergyServerHelper.extractMessageDataLength(
            arrayToByteVector(new byte[]{0,0,(byte)0x01,(byte)0x02}),0));
        assertEquals("",66051,SynergyServerHelper.extractMessageDataLength(
            arrayToByteVector(new byte[]{(byte)0x00,(byte)0x01,(byte)0x02,(byte)0x03}),0));
        assertEquals("",16909060,SynergyServerHelper.extractMessageDataLength(
            arrayToByteVector(new byte[]{(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04}),0));

        // with offset
        assertEquals("",16909060,SynergyServerHelper.extractMessageDataLength(
            arrayToByteVector(new byte[]{9,9,9,9,9,9,(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04}),6));

        // broken message
        assertEquals("",0,SynergyServerHelper.extractMessageDataLength(
            arrayToByteVector(new byte[]{0,0,0}),0));
    }

    public void testMessageToQueue() throws Exception {
        BlockingQueue<Vector<Byte>> queue = new LinkedBlockingQueue<Vector<Byte>>();
        Vector<Byte> message;

        // extract one single message
        assertEquals("",1, SynergyServerHelper.addMessagetoQueue(
                arrayToByteVector(new byte[]{0,0,0,4,1,2,3,4}),
                queue) );
        message = queue.poll(0,TimeUnit.SECONDS);
        assertNotNull("",message);
        assertEqual("",new byte[]{0,0,0,4,1,2,3,4},message);
        assertNull("queue should be empty",queue.peek());

        // extract three messages
        assertEquals("",3, SynergyServerHelper.addMessagetoQueue(
                arrayToByteVector(new byte[]{0,0,0,4,1,2,3,4,  0,0,0,2,1,2,   0,0,0,3,1,2,3 }),
                queue) );
        message = queue.poll(0,TimeUnit.SECONDS);
        assertNotNull("",message);
        assertEqual("",new byte[]{0,0,0,4,1,2,3,4},message);

        message = queue.poll(0,TimeUnit.SECONDS);
        assertNotNull("",message);
        assertEqual("",new byte[]{0,0,0,2,1,2},message);

        message = queue.poll(0,TimeUnit.SECONDS);
        assertNotNull("",message);
        assertEqual("",new byte[]{0,0,0,3,1,2,3},message);
        assertNull("queue should be empty",queue.peek());

        // extract one message and discard incomplete message
        assertEquals("",1, SynergyServerHelper.addMessagetoQueue(
                arrayToByteVector(new byte[]{0,0,0,4,1,2,3,4,  0,0,0,5,1 }),
                queue) );
        message = queue.poll(0,TimeUnit.SECONDS);
        assertNotNull("",message);
        assertEqual("",new byte[]{0,0,0,4,1,2,3,4},message);
        assertNull("queue should be empty",queue.peek());

        // extract one message and discard incomplete message
        assertEquals("",1, SynergyServerHelper.addMessagetoQueue(
                arrayToByteVector(new byte[]{0,0,0,4,1,2,3,4,  0,0,0 }),
                queue) );
        message = queue.poll(0,TimeUnit.SECONDS);
        assertNotNull("",message);
        assertEqual("",new byte[]{0,0,0,4,1,2,3,4},message);
        assertNull("queue should be empty",queue.peek());

    }

    // ---------------------
    // |      Helpers      |
    // ---------------------
    static Vector<Byte> arrayToByteVector( byte[] byteArray){
        Vector<Byte> byteVector = new Vector<Byte>();
        for(int i=0;i<byteArray.length;++i){
            byteVector.add( (byte) byteArray[i] );
        }
        return byteVector;
    }

    static void assertEqual(String failMessage, byte[] array, Vector<Byte> vector) {
        boolean isEqual = true;
        if (vector.size() == array.length ) {
            for(int i=0;i<array.length;++i){
                if (vector.get(i) != array[i] ) {
                    isEqual = false;
                    break;
                } 
            }
        } else {
            isEqual = false;
        }
        if (isEqual != true ) {
            throw new AssertionFailedError(failMessage + ": should have been: " + vector + " but was: "
                    + Arrays.toString(array));
        }
    }

}
