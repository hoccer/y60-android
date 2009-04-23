package com.artcom.y60.gom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.Logger;

public class GomProxyHelper {

    // Constants ---------------------------------------------------------

    public static final String LOG_TAG = "GomProxyHelper";

    // Instance Variables ------------------------------------------------

    /**
     * The client for this helperProxyHelper
     */
    // private Context mContext;
    private IGomProxyService mProxy;

    private GomProxyServiceConnection mConnection;

    private BindingListener<GomProxyHelper> mBindingListener;

    private Context mContext;

    // Constructors ------------------------------------------------------

    public GomProxyHelper(Context pContext, BindingListener<GomProxyHelper> pBindingListener) {

        mBindingListener = pBindingListener;
        mContext = pContext;
        mConnection = new GomProxyServiceConnection();
        
        bind();
    }

    // Public Instance Methods -------------------------------------------

    public void bind() {
        Intent proxyIntent = new Intent(IGomProxyService.class.getName());
        Logger.v(logTag(), "binding to GomProxy");
        if (!mContext.bindService(proxyIntent, mConnection, Context.BIND_AUTO_CREATE)) {

            throw new BindingException("bindService failed for GomProxyService");
        }
    }
    
    public void unbind() {
        Logger.v(logTag(), "unbinding from GomProxy");
        mContext.unbindService(mConnection);
    }

    public GomEntry getEntry(String pPath) {

        assertConnected();

        String lastSeg = pPath.substring(pPath.lastIndexOf("/") + 1);
        if (lastSeg.contains(":")) {

            return getAttribute(pPath);

        } else {

            return getNode(pPath);
        }
    }

    public GomNode getNode(String pPath) throws GomEntryTypeMismatchException {

        assertConnected();

        return new GomNode(pPath, this);
    }

    public GomNode getNode(Uri uri) {

        return getNode(uri.toString());
    }

    public GomAttribute getAttribute(String pPath) throws GomEntryTypeMismatchException {

        assertConnected();

        try {
            return new GomAttribute(pPath, this);

        } catch (RemoteException rex) {

            Logger.e(LOG_TAG, "failed to retrieve attribute data", rex);
            throw new RuntimeException(rex);
        }
    }

    public Uri getBaseUri() {

        assertConnected();

        try {

            return Uri.parse(mProxy.getBaseUri());

        } catch (Exception x) {

            Logger.e(LOG_TAG, "getBaseUri failed", x);
            throw new RuntimeException(x);
        }
    }

    // Package Protected Instance Methods --------------------------------

    IGomProxyService getProxy() {

        return mProxy;
    }

    // Private Instance Methods ------------------------------------------

    private void assertConnected() {

        if (mProxy == null) {

            throw new BindingException("Unable to bind proxy!");
        }
    }

    private String logTag() {

        return "GomProxyHelper";
    }

    // Inner Classes -----------------------------------------------------

    class GomProxyServiceConnection implements ServiceConnection {

        public void onServiceConnected(ComponentName pName, IBinder pBinder) {

            Logger.v("GomProxyServiceConnection", "onServiceConnected(", pName, ")");
            mProxy = IGomProxyService.Stub.asInterface(pBinder);

            if (mBindingListener != null) {
                mBindingListener.bound(GomProxyHelper.this);
            }
        }

        public void onServiceDisconnected(ComponentName pName) {

            Logger.v("GomProxyServiceConnection", "onServiceDisconnected(", pName, ")");
            mProxy = null;

            if (mBindingListener != null) {
                mBindingListener.unbound(GomProxyHelper.this);
            }
        }
    }
}
