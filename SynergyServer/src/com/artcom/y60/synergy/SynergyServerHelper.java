package com.artcom.y60.synergy;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import com.artcom.y60.Logger;

public class SynergyServerHelper {

    // ------------------------
    // |    Static Members    |
    // ------------------------
    private static final String     LOG_TAG                 = "SynergyServerHelper";
    static final int                BITMASK                 = 0x000000FF;
    static final int                MESSAGE_HEAD_OFFSET     = 4;

    // ------------------------
    // |    Static Methods    |
    // ------------------------
    static Vector<Byte> prepareMessageFromString(String pMessageString, int pDataLength) {
        int messageLength = pMessageString.length() + pDataLength;
        Vector<Byte> messageVector = new Vector<Byte>();
        messageVector.add((byte)(messageLength >> 24 & BITMASK));
        messageVector.add((byte)(messageLength >> 16 & BITMASK));
        messageVector.add((byte)(messageLength >> 8 & BITMASK));
        messageVector.add((byte)(messageLength & BITMASK));
        for(int i=0; i<pMessageString.length(); ++i){
           messageVector.add((byte)pMessageString.charAt(i)); 
        }
        return messageVector;
    }

    static String messageToString(Vector<Byte> message) {
        String       messageString = "";
        for(int i=0;i<message.size();++i){
            if ( i!=0 ){
                messageString += ",";
            }
            messageString += (byte) message.get(i);
            if ( Character.isISOControl( (char) (byte) message.get(i) ) == false ) {
                messageString += "(" + (char) (byte) message.get(i) + ")";
            }
        }
        return messageString;
    }

    static boolean messageSearchString(Vector<Byte> message, String string) {
        boolean messageContainsString = true;
        for(int i=0;i<string.length();++i) {
            if ( (i+MESSAGE_HEAD_OFFSET) >= message.size() ) {
                messageContainsString = false;
                break;
            }
            if ( string.charAt(i) != message.get(i+MESSAGE_HEAD_OFFSET) ) {
                messageContainsString = false;
                break;
            }
        }
        return messageContainsString;
    }

    static int extractMessageDataLength(Vector<Byte> message, int offset) {
        if (message.size() < (offset+4) ) {
            return 0;
        }
        return  (message.get(offset+0) << 24) +
                (message.get(offset+1) << 16) +
                (message.get(offset+2) <<  8) +
                (message.get(offset+3) );
    }

    static int extractMessageDataLength(Vector<Byte> message) {
        return extractMessageDataLength(message,0);
    }

    static int addMessagetoQueue(Vector<Byte> message,BlockingQueue<Vector<Byte>> queue,
            Vector<Byte> halfMessage) throws InterruptedException {
        int addedMessages = 0;
        if (halfMessage.size() > 0 ) {
            for(int i=(halfMessage.size()-1);i>=0;--i){
                message.add(0,halfMessage.get(i));
            }
            halfMessage.clear();
        }
        int messageLength = extractMessageDataLength(message);
        if (message.size() == (messageLength+MESSAGE_HEAD_OFFSET) ){
            queue.put(message);
            ++addedMessages;
        } else if (message.size() > (messageLength+MESSAGE_HEAD_OFFSET) ){
            int i = 0;
            int j;
            while(true){
                if ( i >= message.size() ) {
                    break;
                }
                messageLength = extractMessageDataLength(message,i);
                if ( (i+MESSAGE_HEAD_OFFSET+messageLength) <= message.size() ) {
                    Vector<Byte> partialMessage = new Vector<Byte>();
                    for(j=i;j<(i+MESSAGE_HEAD_OFFSET+messageLength);++j) {
                        partialMessage.add( message.get(j) );
                    }
                    queue.put(partialMessage);
                    ++addedMessages;
                    i += MESSAGE_HEAD_OFFSET;
                    i += messageLength;
                } else {
                    for(j=i;j<message.size();++j){
                        halfMessage.add(message.get(j));
                    }
                    break;
                }
            }
        } else {
            halfMessage.addAll(message);
        }
        return addedMessages;
    }

}
