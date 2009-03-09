package com.artcom.y60.infrastructure.dc;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.ComponentName;
import android.content.Intent;
import android.test.ServiceTestCase;

public class DeviceControllerServiceTest extends ServiceTestCase<DeviceControllerService> {

	private static final String TEST_PORT = "4223";
    public static final boolean TEST_NIO = true;
	
	public DeviceControllerServiceTest() {
		super(DeviceControllerService.class);
	}
	
	public void testStartup() throws NumberFormatException, UnknownHostException, IOException {

		DeviceControllerService service = getService();
//        Intent stopIntent = new Intent();
//        stopIntent.setComponent(new ComponentName(DeviceControllerService.class.getPackage().toString(),
//        		DeviceControllerService.class.getName()));
//		service.stopService( stopIntent );

		Intent startIntent =  new Intent();
        startIntent.setComponent(new ComponentName(DeviceControllerService.class.getPackage().toString(),
        		DeviceControllerService.class.getName()));
		startIntent.putExtra(DeviceControllerService.DEFAULT_PORTNAME, TEST_PORT);
        startIntent.putExtra(DeviceControllerService.DEFAULT_NIONAME, TEST_NIO);
        startService(startIntent);
        
		Socket socket = new Socket( "localhost", Integer.parseInt( TEST_PORT ) );
		socket.close();
	}
}
