package com.artcom.y60.gom;

public class GomEntryNotFoundException extends GomException {

	private static final long serialVersionUID = 8590933029300763147L;

	public GomEntryNotFoundException() {
        super();
    }

    public GomEntryNotFoundException(String pDetailMessage, Throwable pThrowable) {
        super(pDetailMessage, pThrowable);
    }

    public GomEntryNotFoundException(String pDetailMessage) {
        super(pDetailMessage);
    }

    public GomEntryNotFoundException(Throwable pThrowable) {
        super(pThrowable);
    }

}
