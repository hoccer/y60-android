package com.artcom.y60.gom;

public class GomProxyException extends GomException {

	private static final long serialVersionUID = 4365641133189765265L;

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
