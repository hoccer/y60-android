package com.artcom.y60.trackpad;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.view.KeyEvent;

import com.artcom.y60.Logger;
import com.artcom.y60.Y60ActivityInstrumentationTest;

public class TrackPadInstrumentationTest extends Y60ActivityInstrumentationTest<TrackPad> {

    // Constructors ------------------------------------------------------

    public TrackPadInstrumentationTest() {
        
        super("com.artcom.y60.trackpad", TrackPad.class);
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    @Override
    public void setUp() throws Exception {
        
        super.setUp();
        
        getActivity().getRemote().overrideTargetAndConnect(InetAddress.getByName("localhost"), 1999);
    }
    
    
    @Override
    public void tearDown() throws Exception {
        
        getActivity().finish();
        super.tearDown();
    }
    
    
    public void testTrackball() throws Exception {
        
        testTrackballMove(KeyEvent.KEYCODE_DPAD_UP,     0, -1);
        testTrackballMove(KeyEvent.KEYCODE_DPAD_DOWN,   0,  1);
        testTrackballMove(KeyEvent.KEYCODE_DPAD_LEFT,  -1,  0);
        testTrackballMove(KeyEvent.KEYCODE_DPAD_RIGHT,  1,  0);
    }
    
    
    public void testStability() throws Exception {
        
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 30000) {
            
            testTrackballMove(KeyEvent.KEYCODE_DPAD_RIGHT,  1,  0);
        }
    }

    
    
    // Private Instance Methods ------------------------------------------

    private void testTrackballMove(int pKeyCode, int pX, int pY) throws Exception {
        
        Logger.v(tag(), "testTrackballMove");
        MovementServerThread server = new MovementServerThread();
        server.start();
        pressKey(pKeyCode, 100);
        
        try {
            
            Thread.sleep(50);
            
        } catch (InterruptedException x) {
            
            // kthxbai
        }
        
        if (server.error != null) {
            
            throw new RuntimeException(server.error);
        }
        
        if (server.x == null || server.y == null) {
            
            fail("Didn't receive a datagram with movement information!");
        }
        
        assertEquals("x delta mismatch", pX, server.x.byteValue());
        assertEquals("y delta mismatch", pY, server.y.byteValue());
    }

    
    
    // Inner Classes -----------------------------------------------------

    class MovementServerThread extends Thread {

        private DatagramSocket mSocket = null;
        
        Byte x = null;
        
        Byte y = null;
        
        Throwable error;

        public MovementServerThread() throws IOException {
            
            super("MovementServerThread");
            mSocket = new DatagramSocket(1999);
        }

        public void run() {

            try {
                byte[] buffer = new byte[2];

                // receive request
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                mSocket.receive(packet);
                
                x = buffer[0];
                y = buffer[1];

            } catch (IOException e) {
                
                error = e;
            } finally {
                mSocket.close();
            }
        }
    }

}
