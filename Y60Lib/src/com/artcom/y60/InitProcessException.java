package com.artcom.y60;

public class InitProcessException extends Exception {

	private static final long serialVersionUID = -103796926729214550L;

	public InitProcessException(String pMessage) {
        super(pMessage);
    }

    public InitProcessException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public InitProcessException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
        // TODO Auto-generated constructor stub
    }

    public InitProcessException(Throwable pThrowable) {
        super(pThrowable);
        // TODO Auto-generated constructor stub
    }
}
