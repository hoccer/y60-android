/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/lisa/code/y60/y60-android/GomProxy/src/com/artcom/y60/infrastructure/gom/IGomProxyService.aidl
 */
package com.artcom.y60.infrastructure.gom;
import java.lang.String;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
import java.util.List;
// import com.artcom.y60.infrastructure.gom.GomAttribute;
// import com.artcom.y60.infrastructure.gom.GomEntry;
// import com.artcom.y60.infrastructure.gom.GomNode;

public interface IGomProxyService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.artcom.y60.infrastructure.gom.IGomProxyService
{
private static final java.lang.String DESCRIPTOR = "com.artcom.y60.infrastructure.gom.IGomProxyService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an IGomProxyService interface,
 * generating a proxy if needed.
 */
public static com.artcom.y60.infrastructure.gom.IGomProxyService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.artcom.y60.infrastructure.gom.IGomProxyService))) {
return ((com.artcom.y60.infrastructure.gom.IGomProxyService)iin);
}
return new com.artcom.y60.infrastructure.gom.IGomProxyService.Stub.Proxy(obj);
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
case TRANSACTION_getNodeData:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.List<java.lang.String> _arg1;
_arg1 = new java.util.ArrayList<java.lang.String>();
java.util.List<java.lang.String> _arg2;
_arg2 = new java.util.ArrayList<java.lang.String>();
this.getNodeData(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeStringList(_arg1);
reply.writeStringList(_arg2);
return true;
}
case TRANSACTION_getAttributeValue:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getAttributeValue(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_refreshEntry:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.refreshEntry(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getBaseUri:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getBaseUri();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.artcom.y60.infrastructure.gom.IGomProxyService
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
public void getNodeData(java.lang.String pPath, java.util.List<java.lang.String> pSubNodeNames, java.util.List<java.lang.String> pAttributeNames) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pPath);
mRemote.transact(Stub.TRANSACTION_getNodeData, _data, _reply, 0);
_reply.readException();
_reply.readStringList(pSubNodeNames);
_reply.readStringList(pAttributeNames);
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.lang.String getAttributeValue(java.lang.String pPath) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pPath);
mRemote.transact(Stub.TRANSACTION_getAttributeValue, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void refreshEntry(java.lang.String pPath) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pPath);
mRemote.transact(Stub.TRANSACTION_refreshEntry, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.lang.String getBaseUri() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getBaseUri, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getNodeData = (IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getAttributeValue = (IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_refreshEntry = (IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getBaseUri = (IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void getNodeData(java.lang.String pPath, java.util.List<java.lang.String> pSubNodeNames, java.util.List<java.lang.String> pAttributeNames) throws android.os.RemoteException;
public java.lang.String getAttributeValue(java.lang.String pPath) throws android.os.RemoteException;
public void refreshEntry(java.lang.String pPath) throws android.os.RemoteException;
public java.lang.String getBaseUri() throws android.os.RemoteException;
}
