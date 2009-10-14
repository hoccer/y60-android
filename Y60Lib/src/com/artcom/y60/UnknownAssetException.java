package com.artcom.y60;

public class UnknownAssetException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnknownAssetException(String pMessage) {
        super(pMessage);
    }

    public UnknownAssetException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public UnknownAssetException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
        // TODO Auto-generated constructor stub
    }

    public UnknownAssetException(Throwable pThrowable) {
        super(pThrowable);
        // TODO Auto-generated constructor stub
    }
}
