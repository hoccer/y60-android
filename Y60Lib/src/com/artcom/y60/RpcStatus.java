package com.artcom.y60;

import android.os.Parcel;
import android.os.Parcelable;

public class RpcStatus implements Parcelable {

    // Constants ---------------------------------------------------------

    public static final Parcelable.Creator<RpcStatus> CREATOR = new Parcelable.Creator<RpcStatus>() {
                                                                  public RpcStatus createFromParcel(
                                                                          Parcel in) {
                                                                      RpcStatus status = new RpcStatus();
                                                                      status.readFromParcel(in);
                                                                      return status;
                                                                  }

                                                                  public RpcStatus[] newArray(
                                                                          int size) {
                                                                      return new RpcStatus[size];
                                                                  }
                                                              };

    // Instance Variables ------------------------------------------------

    private Throwable                                 mError;

    // Constructors ------------------------------------------------------

    public RpcStatus() {

        mError = null;
    }

    // Public Instance Methods -------------------------------------------

    public boolean isOk() {

        return mError == null;
    }

    public boolean hasError() {

        return !isOk();
    }

    public Throwable getError() {
        mError.fillInStackTrace();
        return mError;
    }

    public void setError(Throwable pError) {

        mError = pError;
    }

    @Override
    public int describeContents() {

        // HILFE
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pArg0, int pArg1) {

        pArg0.writeSerializable(mError);
    }

    public void readFromParcel(Parcel pParcel) {

        setError((Throwable) pParcel.readSerializable());
    }
}
