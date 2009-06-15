package com.artcom.y60.gom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.artcom.y60.BindingException;
import com.artcom.y60.ReferenceTo;
import com.artcom.y60.RpcStatus;
import com.artcom.y60.Y60ActivityInstrumentationTest;
import com.artcom.y60.Y60TestActivity;

public class GomProxyClientTest extends Y60ActivityInstrumentationTest<Y60TestActivity> {

    public GomProxyClientTest() {

        super("com.artcom.y60", Y60TestActivity.class);
    }

    public void testExceptionInRpcStatus() throws Exception {

        long timestamp = System.currentTimeMillis();
        final String path = "/tralala:" + timestamp;
        final ReferenceTo<Throwable> errHolder = new ReferenceTo<Throwable>(null);

        Intent proxyIntent = new Intent(IGomProxyService.class.getName());
        ServiceConnection conn = new ServiceConnection() {

            public void onServiceConnected(ComponentName pName, IBinder pBinder) {

                try {
                    IGomProxyService proxy = IGomProxyService.Stub.asInterface(pBinder);
                    RpcStatus status = new RpcStatus();
                    proxy.getAttributeValue(path, status);

                    assertTrue("Expected an exception", status.hasError());
                    assertTrue("Expected a 404 exception", status.getError().toString().contains(
                            "404"));

                } catch (Throwable rex) {

                    errHolder.setValue(rex);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName pName) {
            }
        };

        if (!getActivity().bindService(proxyIntent, conn, Context.BIND_AUTO_CREATE)) {

            throw new BindingException("bindService failed for GomProxyService");
        }

        Thread.sleep(2500);

        if (errHolder.getValue() != null) {

            throw new RuntimeException(errHolder.getValue());
        }
    }
}
