package com.artcom.y60.gom;

import java.util.LinkedList;
import java.util.List;

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
import com.artcom.y60.RpcStatus;

public class GomProxyHelper {

    public static final String                    LOG_TAG = "GomProxyHelper";
    private final GomProxyServiceConnection       mConnection;
    private final BindingListener<GomProxyHelper> mBindingListener;
    
    private IGomProxyService                      mProxy;
    protected Context                             mContext;

    public GomProxyHelper(Context pContext, BindingListener<GomProxyHelper> pBindingListener) {
        Logger.v(LOG_TAG, "GomProxyHelper constructor, context is: ", pContext);
        mBindingListener = pBindingListener;
        mContext = pContext;
        mConnection = new GomProxyServiceConnection();
        bind();
    }

    public void bind() {
        Intent proxyIntent = new Intent(IGomProxyService.class.getName());
        Logger.v(logTag(), "binding to GomProxy (" + toString() + ")");
        if (!mContext.bindService(proxyIntent, mConnection, Context.BIND_AUTO_CREATE)) {
            throw new BindingException("bindService failed for GomProxyService");
        }
    }

    public void unbind() {
        Logger.v(logTag(), "unbinding from GomProxy (" + toString() + ") and context: ", mContext);
        mContext.unbindService(mConnection);
        onUnbound();
    }

    public boolean isBound() {
        return mProxy != null;
    }

    public GomEntry getEntry(String pPath) throws GomEntryTypeMismatchException,
            GomEntryNotFoundException {

        assertConnected();
        String lastSeg = pPath.substring(pPath.lastIndexOf("/") + 1);
        if (lastSeg.contains(":")) {
            return getAttribute(pPath);
        } else {
            return getNode(pPath);
        }
    }

    public GomNode getNode(String pPath) {
        assertConnected();
        return new GomNode(pPath, this);
    }

    public GomNode getNode(Uri uri) throws GomException {
        return getNode(uri.toString());
    }

    public GomAttribute getAttribute(String pPath) throws GomEntryTypeMismatchException,
            GomEntryNotFoundException {
        assertConnected();
        try {
            RpcStatus status = new RpcStatus();
            String value = getProxy().getAttributeValue(pPath, status);

            if (status.hasError()) {
                Logger.v(LOG_TAG, status.getError());
                Throwable err = status.getError();
                if (err instanceof GomEntryNotFoundException) {
                    throw new GomEntryNotFoundException(err);
                } else {
                    throw new RuntimeException(err);
                }
            }
            return new GomAttribute(pPath, this, value);

        } catch (RemoteException rex) {
            Logger.e(LOG_TAG, "failed to retrieve attribute data", rex);
            throw new RuntimeException(rex);
        }
    }

    public boolean hasAttribute(String pPath) {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            getProxy().getAttributeValue(pPath, status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (status.hasError()) {
            return false;
        }
        return true;
    }

    // TODO test this
    public String getCachedAttributeValue(String pPath) throws GomProxyException {
        assertConnected();
        RpcStatus status = new RpcStatus();
        String result = null;
        try {
            result = mProxy.getCachedAttributeValue(pPath, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                if (err instanceof GomProxyException) {
                    throw (GomProxyException) err;
                }
                throw new RuntimeException(err);
            }
        } catch (RemoteException rex) {
            Logger.e(LOG_TAG, "error: rpcStatus has no error", rex);
            throw new RuntimeException(rex);
        }
        return result;
    }

    public Uri getBaseUri() {
        assertConnected();
        RpcStatus status = new RpcStatus();
        Uri uri;
        try {
            uri = Uri.parse(mProxy.getBaseUri(status));
            if (status.hasError()) {
                Throwable err = status.getError();
                throw new RuntimeException(err);
            }
        } catch (Exception x) {
            Logger.e(LOG_TAG, "getBaseUri failed", x);
            throw new RuntimeException(x);
        }
        return uri;
    }

    public boolean hasInCache(String pPath) {
        RpcStatus status = new RpcStatus();
        boolean inCache;
        assertConnected();
        try {
            inCache = mProxy.hasInCache(pPath, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                throw new RuntimeException(err);
            }
        } catch (NullPointerException e) {
            throw new BindingException(
                    "Catched exception which indicates an asyncrounous unbind from gomproxy", e);
        } catch (Exception x) {
            Logger.e(LOG_TAG, "hasInCache failed", x);
            throw new RuntimeException(x);
        }
        return inCache;
    }

    public void saveAttribute(String pPath, String pValue) {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            mProxy.saveAttribute(pPath, pValue, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                throw new RuntimeException(err);
            }
        } catch (Exception x) {
            Logger.e(LOG_TAG, "saveAttribute failed", x);
            throw new RuntimeException(x);
        }
    }

    public void saveNode(String pNodePath, LinkedList<String> pSubNodeNames,
            LinkedList<String> pAttributeNames) {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            mProxy.saveNode(pNodePath, pSubNodeNames, pAttributeNames, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                throw new RuntimeException(err);
            }
        } catch (Exception x) {
            Logger.e(LOG_TAG, "saveNode failed", x);
            throw new RuntimeException(x);
        }
    }

    public void deleteEntry(String pPath) {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            mProxy.deleteEntry(pPath, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                throw new RuntimeException(err);
            }
        } catch (RemoteException rex) {
            Logger.e(LOG_TAG, "failed to delete entry", rex);
            throw new RuntimeException(rex);
        }
    }

    public void clear() {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            mProxy.clear(status);
            if (status.hasError()) {
                Throwable err = status.getError();
                throw new RuntimeException(err);
            }
        } catch (RemoteException rex) {
            Logger.e(LOG_TAG, "failed to clear", rex);
            throw new RuntimeException(rex);
        }
    }

    public void updateEntry(String pPath, String pJsonData) {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            mProxy.updateEntry(pPath, pJsonData, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                throw new RuntimeException(err);
            }
        } catch (RemoteException rex) {
            Logger.e(LOG_TAG, "failed to update Entry", rex);
            throw new RuntimeException(rex);
        }
    }

    public void createEntry(String pPath, String pJsonData) {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            mProxy.createEntry(pPath, pJsonData, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                throw new RuntimeException(err);
            }
        } catch (RemoteException rex) {
            Logger.e(LOG_TAG, "failed to create Entry", rex);
            throw new RuntimeException(rex);
        }
    }

    void refreshEntry(String pPath) throws Throwable {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            mProxy.refreshEntry(pPath, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                throw err;
            }
        } catch (RemoteException rex) {
            Logger.e(LOG_TAG, "failed to refresh entry", rex);
            throw new RuntimeException(rex);
        }
    }

    void getNodeData(String pPath, List<String> pSubNodeNames, List<String> pAttributeNames)
            throws GomEntryNotFoundException {
        assertConnected();
        try {
            RpcStatus status = new RpcStatus();
            mProxy.getNodeData(pPath, pSubNodeNames, pAttributeNames, status);

            if (status.hasError()) {
                Throwable err = status.getError();
                if (err instanceof GomEntryNotFoundException) {
                    throw new GomEntryNotFoundException(err);
                } else {
                    throw new RuntimeException(err);
                }
            }
        } catch (RemoteException rex) {

            Logger.e(LOG_TAG, "failed to retrieve node data", rex);
            throw new RuntimeException(rex);
        }
    }

    public void getCachedNodeData(String pPath, List<String> pSubNodeNames,
            List<String> pAttributeNames) throws GomProxyException {
        assertConnected();
        RpcStatus status = new RpcStatus();
        try {
            mProxy.getCachedNodeData(pPath, pSubNodeNames, pAttributeNames, status);
            if (status.hasError()) {
                Throwable err = status.getError();
                if (err instanceof GomProxyException) {
                    throw (GomProxyException) err;
                }
                throw new RuntimeException(err);
            }
        } catch (RemoteException rex) {
            Logger.e(LOG_TAG, "error: rpcStatus has no error", rex);
            throw new RuntimeException(rex);
        }
    }

    IGomProxyService getProxy() {
        return mProxy;
    }

    private void onUnbound() {
        mProxy = null;
        if (mBindingListener != null) {
            mBindingListener.unbound(GomProxyHelper.this);
        }
    }

    private void assertConnected() {
        if (mProxy == null) {
            BindingException e = new BindingException("GomProxyHelper " + toString()
                    + " not bound to proxy!");
            e.fillInStackTrace();
            throw e;
        }
    }

    private String logTag() {
        return "GomProxyHelper";
    }

    class GomProxyServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName pName, IBinder pBinder) {
            Logger.v(LOG_TAG, "onServiceConnected(", pName, ")");
            mProxy = IGomProxyService.Stub.asInterface(pBinder);
            if (mBindingListener != null) {
                mBindingListener.bound(GomProxyHelper.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName pName) {
            // ErrorHandling.signalServiceError("GomProxyServiceConnection", new
            // Exception(
            // "GOM proxy service has been disconnected unexpectedly!"),
            // mContext);
            Logger.w(LOG_TAG, "GOM proxy service has been disconnected unexpectedly!");
            onUnbound();
        }
    }
}