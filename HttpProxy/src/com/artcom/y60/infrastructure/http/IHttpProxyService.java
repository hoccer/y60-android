/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/arne/Development/work/t-gallery/devel/y60-android/HttpProxy/src/com/artcom/y60/infrastructure/http/IHttpProxyService.aidl
 */
package com.artcom.y60.infrastructure.http;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
public interface IHttpProxyService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.artcom.y60.infrastructure.http.IHttpProxyService
{
private static final java.lang.String DESCRIPTOR = "com.artcom.y60.infrastructure.http.IHttpProxyService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IHttpProxyService interface,
 * generating a proxy if needed.
 */
public static com.artcom.y60.infrastructure.http.IHttpProxyService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.artcom.y60.infrastructure.http.IHttpProxyService))) {
return ((com.artcom.y60.infrastructure.http.IHttpProxyService)iin);
}
return new com.artcom.y60.infrastructure.http.IHttpProxyService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_get:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
byte[] _result = this.get(_arg0);
reply.writeNoException();
reply.writeByteArray(_result);
return true;
}
case TRANSACTION_fetchFromCache:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
byte[] _result = this.fetchFromCache(_arg0);
reply.writeNoException();
reply.writeByteArray(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.artcom.y60.infrastructure.http.IHttpProxyService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public byte[] get(java.lang.String pUri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pUri);
mRemote.transact(Stub.TRANSACTION_get, _data, _reply, 0);
_reply.readException();
_result = _reply.createByteArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public byte[] fetchFromCache(java.lang.String pUri) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pUri);
mRemote.transact(Stub.TRANSACTION_fetchFromCache, _data, _reply, 0);
_reply.readException();
_result = _reply.createByteArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_get = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_fetchFromCache = (IBinder.FIRST_CALL_TRANSACTION + 1);
}
public byte[] get(java.lang.String pUri) throws android.os.RemoteException;
public byte[] fetchFromCache(java.lang.String pUri) throws android.os.RemoteException;
}
