package com.artcom.y60.gom;

import java.util.LinkedList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import com.artcom.y60.BindingException;
import com.artcom.y60.BindingListener;
import com.artcom.y60.ErrorHandling;
import com.artcom.y60.Logger;

public class GomProxyHelper {

    // Constants ---------------------------------------------------------

    public static final String              LOG_TAG = "GomProxyHelper";

    // Instance Variables ------------------------------------------------

    /**
     * The client for this helperProxyHelper
     */
    // private Context mContext;
    private IGomProxyService                mProxy;

    private GomProxyServiceConnection       mConnection;

    private BindingListener<GomProxyHelper> mBindingListener;

    private Context                         mContext;

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
        Logger.v(logTag(), "binding to GomProxy (" + toString() + ")");
        if (!mContext.bindService(proxyIntent, mConnection, Context.BIND_AUTO_CREATE)) {

            throw new BindingException("bindService failed for GomProxyService");
        }
    }

    public void unbind() {
        Logger.v(logTag(), "unbinding from GomProxy (" + toString() + ")");
        mContext.unbindService(mConnection);
        onUnbound();
    }

    public boolean isBound() {
        return mProxy != null;
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

    public boolean hasInCache(String pPath) {

        assertConnected();

        try {

            return mProxy.hasInCache(pPath);

        } catch (Exception x) {

            Logger.e(LOG_TAG, "hasInCache failed", x);
            throw new RuntimeException(x);
        }
    }

    public void saveAttribute(String pPath, String pValue) {

        assertConnected();

        try {

            mProxy.saveAttribute(pPath, pValue);

        } catch (Exception x) {

            Logger.e(LOG_TAG, "saveAttribute failed", x);
            throw new RuntimeException(x);
        }
    }

    public void saveNode(String pNodePath, LinkedList<String> pSubNodeNames,
            LinkedList<String> pAttributeNames) {

        assertConnected();

        try {

            mProxy.saveNode(pNodePath, pSubNodeNames, pAttributeNames);

        } catch (Exception x) {

            Logger.e(LOG_TAG, "saveNode failed", x);
            throw new RuntimeException(x);
        }
    }

    // Package Protected Instance Methods --------------------------------

    IGomProxyService getProxy() {

        return mProxy;
    }

    // Private Instance Methods ------------------------------------------

    private void onUnbound() {
        mProxy = null;

        if (mBindingListener != null) {
            mBindingListener.unbound(GomProxyHelper.this);
        }
    }

    private void assertConnected() {

        if (mProxy == null) {

            throw new BindingException("GomProxyHelper " + toString() + " unable to bind proxy!");
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

        @Override
        public void onServiceDisconnected(ComponentName pName) {

            ErrorHandling.signalServiceError("GomProxyServiceConnection", new Exception(
                    "Service as been unexpetly disconnected"), mContext);
            onUnbound();
        }
    }

}
