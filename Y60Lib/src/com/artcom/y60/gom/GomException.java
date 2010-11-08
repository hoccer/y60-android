package com.artcom.y60.gom;

public class GomException extends Exception {

	private static final long serialVersionUID = 695956208101959198L;

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