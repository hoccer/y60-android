package com.artcom.y60.gom;

public class GomProxyException extends Exception {

    public GomProxyException() {
        super();
    }

    public GomProxyException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
    }

    public GomProxyException(String pDetailMessage) {
        super(pDetailMessage);
    }

    public GomProxyException(Throwable pThrowable) {
        super(pThrowable);
    }

}
