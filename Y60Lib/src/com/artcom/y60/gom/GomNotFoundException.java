package com.artcom.y60.gom;

public class GomNotFoundException extends GomException {

    public GomNotFoundException() {
        super();
    }

    public GomNotFoundException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
    }

    public GomNotFoundException(String pDetailMessage) {
        super(pDetailMessage);
    }

    public GomNotFoundException(Throwable pThrowable) {
        super(pThrowable);
    }

}
