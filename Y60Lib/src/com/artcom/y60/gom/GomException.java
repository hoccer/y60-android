package com.artcom.y60.gom;

public class GomException extends Exception {

    public GomException() {
        super();
    }

    public GomException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
    }

    public GomException(String pDetailMessage) {
        super(pDetailMessage);
    }

    public GomException(Throwable pThrowable) {
        super(pThrowable);
    }

}