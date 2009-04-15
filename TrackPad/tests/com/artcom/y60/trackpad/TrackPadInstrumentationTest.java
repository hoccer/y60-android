package com.artcom.y60.trackpad;

import java.net.SocketAddress;

import android.view.KeyEvent;

import com.artcom.y60.Logger;
import com.artcom.y60.Y60ActivityInstrumentationTest;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class TrackPadInstrumentationTest extends Y60ActivityInstrumentationTest<TrackPad> {

    // Constructors ------------------------------------------------------

    public TrackPadInstrumentationTest() {
        
        super("com.artcom.y60.trackpad", TrackPad.class);
    }
    
    
    
    // Public Instance Methods -------------------------------------------

    @Override
    public void setUp() throws Exception {
        
        super.setUp();
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
    

    
    // Private Instance Methods ------------------------------------------

    private void testTrackballMove(int pKeyCode, int pX, int pY) throws Exception {
        
        Logger.v(tag(), "testTrackballMove");
        OSCServer server = OSCServer.newUsing( OSCServer.UDP, 4711);
        TestOSCListener lsner = new TestOSCListener();
        server.addOSCListener(lsner);
        server.start();
        pressKey(pKeyCode, 100);
        
        try {
            
            Thread.sleep(50);
            
        } catch (InterruptedException x) {
            
            // kthxbye
        }
        
        server.stop();
        server.dispose();
        
        if (lsner.error != null) {
            
            throw new RuntimeException(lsner.error);
        }
        
        if (lsner.x == null || lsner.y == null) {
            
            fail("Didn't receive a packet with movement information!");
        }
        
        assertEquals("x delta mismatch", pX, lsner.x.intValue());
        assertEquals("y delta mismatch", pY, lsner.y.intValue());
    }

    
    
    // Inner Classes -----------------------------------------------------

    class TestOSCListener implements OSCListener {

        Integer x;
        
        Integer y;
        
        String error;

        @Override
        public void messageReceived(OSCMessage pMsg, SocketAddress pAddr, long pLong) {
            
            Logger.v("TradPadInstrumentationTest", "message received: ", pMsg.getName());
            
            if ("x".equals(pMsg.getName())) {
                if (x != null) {
                    error = "x value sent twice!";
                } else {
                    x = (Integer)pMsg.getArg(0);
                }
            } else if ("y".equals(pMsg.getName())) {
                if (y != null) {
                    error = "y value sent twice!";
                } else {
                    y = (Integer)pMsg.getArg(0);
                }
            }
        }
    }
    

}
