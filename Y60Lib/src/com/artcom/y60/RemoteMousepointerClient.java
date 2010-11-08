package com.artcom.y60;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class RemoteMousepointerClient {
	private static final String LOG_TAG = "RemoteMousepointerClient";
	private InetAddress mAddress;
	private int mPort;
	private DatagramSocket mSocket;

	public RemoteMousepointerClient() {
	}
	

    public void overrideTargetAndConnect(InetAddress pAddr, int pPort) throws IOException {
        
        mAddress = pAddr;
        mPort    = pPort;
        disconnectFromDisplay();
        connectToDisplay();
    }
    
    private void connectToDisplay() throws SocketException {
        
        mSocket = new DatagramSocket();
    }
    
    
    public void disconnectFromDisplay() {
        
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket = null;
        }
    }
    
    
    public void sendMoveEvent(float pX, float pY) throws IOException, SocketException {
        
        if (mSocket == null) {
            
            connectToDisplay();
        }
        
        byte[]         payload = new byte[]{ deltaToByte(pX), deltaToByte(pY) };
        DatagramPacket packet  = new DatagramPacket(payload, payload.length, mAddress, mPort);
        
        Logger.d(LOG_TAG, "sending bytes ", payload[0], ", ", payload[1], " as UDP datagram");
        
        mSocket.send(packet);
    }
    
    
    private byte deltaToByte(float pDelta) {
        
        // should suffice
        return (byte)pDelta;
    }
    
    
    /*private boolean isConnected() {
        
        return (mSocket != null) && (mSocket.isConnected());
    }*/
}
